package dev.jsinco.brewery.garden;

import com.dre.brewery.recipe.PluginItem;
import com.google.common.base.Preconditions;
import dev.jsinco.brewery.garden.commands.GardenCommand;
import dev.jsinco.brewery.garden.configuration.BreweryGardenConfig;
import dev.jsinco.brewery.garden.configuration.SerdesGarden;
import dev.jsinco.brewery.garden.integration.BreweryGardenIngredient;
import dev.jsinco.brewery.garden.integration.TBPGardenIntegration;
import dev.jsinco.brewery.garden.listener.BlockEventListener;
import dev.jsinco.brewery.garden.listener.EventListeners;
import dev.jsinco.brewery.garden.persist.Database;
import dev.jsinco.brewery.garden.persist.GardenPlantDataType;
import dev.jsinco.brewery.garden.plant.GardenPlant;
import dev.jsinco.brewery.garden.plant.GrowthManager;
import dev.jsinco.brewery.garden.plant.PlantType;
import dev.thorinwasher.blockutil.api.BlockUtilAPI;
import dev.thorinwasher.blockutil.api.event.BlockDisableDropEvent;
import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.yaml.bukkit.YamlBukkitConfigurer;
import eu.okaeri.configs.yaml.bukkit.serdes.SerdesBukkit;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import lombok.Getter;
import net.kyori.adventure.key.Key;
import org.bukkit.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Garden extends JavaPlugin {

    @Getter
    private static Garden instance;
    @Getter
    private static PlantRegistry gardenRegistry;
    @Getter
    private BreweryGardenConfig pluginConfiguration;
    private Database database;
    @Getter
    private GardenPlantDataType gardenPlantDataType;
    @Getter
    private BlockUtilAPI blockUtil;
    private boolean loadSuccess = false;

    @Override
    public void onLoad() {
        instance = this;
        savePlantResources();
        try {
            PluginItem.registerForConfig(this.getName(), BreweryGardenIngredient::new);
        } catch (NoClassDefFoundError ignored) {
        }
        try {
            TBPGardenIntegration.loadIfPossible();
        } catch (NoClassDefFoundError ignored) {
        }
        this.loadSuccess = true;
    }

    @Override
    public void onEnable() {
        Preconditions.checkState(loadSuccess, "Failed on load, see logs.");
        this.database = new Database();
        try {
            database.init(this.getDataFolder());
        } catch (IOException | SQLException e) {
            throw new RuntimeException(e);
        }
        gardenRegistry = new PlantRegistry();
        gardenPlantDataType = new GardenPlantDataType(database);
        this.blockUtil = new BlockUtilAPI.Builder()
                .withConnectionSupplier(() -> {
                    try {
                        return database.getConnection();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                })
                .withDropEventHandler(this::handleBlockDrops)
                .withPluginOwner(this)
                .build();
        this.pluginConfiguration = compileConfig();
        for (World world : Bukkit.getWorlds()) {
            List<GardenPlant> gardenPlants = gardenPlantDataType.fetch(world).join();
            gardenPlants.forEach(gardenRegistry::registerPlant);
        }
        Bukkit.getPluginManager().registerEvents(new EventListeners(gardenRegistry, gardenPlantDataType), this);
        Bukkit.getPluginManager().registerEvents(new BlockEventListener(gardenRegistry, gardenPlantDataType), this);
        this.registerPlantRecipes();
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, GardenCommand::register);
        Bukkit.getScheduler().runTaskTimer(this, new GrowthManager(gardenRegistry, gardenPlantDataType)::tick, 0, 200);
    }

    private void savePlantResources() {
        try (InputStream inputStream = Garden.class.getResourceAsStream("/plants.zip")) {
            if (inputStream == null) {
                throw new IOException("Could not find internal resource: /plants.zip");
            }
            Set<String> alreadySavedNames = readAlreadySaved();
            List<String> savedNames = new ArrayList<>();
            try (ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
                ZipEntry entry = zipInputStream.getNextEntry();
                while (entry != null) {
                    ZipEntry current = entry;
                    if (current.isDirectory()) {
                        entry = zipInputStream.getNextEntry();
                        continue;
                    }
                    File destination = new File(this.getDataFolder(), current.getName());
                    if (destination.exists() || alreadySavedNames.contains(destination.toString())) {
                        entry = zipInputStream.getNextEntry();
                        continue;
                    }
                    File destinationFolder = destination.getParentFile();
                    if (!destinationFolder.exists() && !destination.getParentFile().mkdirs()) {
                        throw new IOException("Could not make dirs at: " + destinationFolder);
                    }
                    if (!destination.createNewFile()) {
                        throw new IOException("could not make file: " + destination);
                    }
                    try (OutputStream outputStream = new FileOutputStream(destination)) {
                        zipInputStream.transferTo(outputStream);
                    }
                    savedNames.add(destination.toString());
                    entry = zipInputStream.getNextEntry();
                }
            }
            writeSaved(savedNames);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeSaved(List<String> savedNames) throws IOException {
        File file = new File(getDataFolder(), "internal/saved_resources.txt");
        if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
            throw new IOException("Unabled to create new folder: " + file.getParentFile());
        }
        if (savedNames.isEmpty()) {
            return;
        }
        if (!file.exists() && !file.createNewFile()) {
            throw new IOException("Unabled to create new file: " + file);
        }
        try (OutputStreamWriter outputStreamWriter = new FileWriter(file, StandardCharsets.UTF_8, true)) {
            try (BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter)) {
                for (String line : savedNames) {
                    bufferedWriter.write(line);
                    bufferedWriter.newLine();
                }
            }
        }
    }

    private Set<String> readAlreadySaved() throws IOException {
        File file = new File(getDataFolder(), "internal/saved_resources.txt");
        if (!file.exists()) {
            return Set.of();
        }
        try (InputStreamReader inputStream = new FileReader(file, StandardCharsets.UTF_8)) {
            try (BufferedReader reader = new BufferedReader(inputStream)) {
                Set<String> output = new HashSet<>();
                String line = reader.readLine();
                while (line != null) {
                    output.add(line);
                    line = reader.readLine();
                }
                return output;
            }
        }
    }

    private void handleBlockDrops(BlockDisableDropEvent blockDisableDropEvent) {
        Material material = blockDisableDropEvent.getBlock().getType();
        if (pluginConfiguration.getDropsDefaultItems().stream().anyMatch(tag -> tag.isTagged(material))) {
            blockDisableDropEvent.setDisableDrops(false);
            return;
        }
        for (Map.Entry<Tag<Material>, Material> entry : pluginConfiguration.getDropOverride().entrySet()) {
            if (entry.getKey().isTagged(material)) {
                blockDisableDropEvent.setDropOverride(List.of(new ItemStack(entry.getValue())));
                return;
            }
        }
    }

    public void reload() {
        gardenRegistry.clear();
        this.pluginConfiguration = compileConfig();
        MutableGardenRegistry.plantType.newBacking(PlantType.readPlantTypes());
        for (World world : Bukkit.getWorlds()) {
            List<GardenPlant> gardenPlants = gardenPlantDataType.fetch(world).join();
            gardenPlants.forEach(gardenRegistry::registerPlant);
        }
    }

    private BreweryGardenConfig compileConfig() {
        return ConfigManager.create(BreweryGardenConfig.class, it -> {
            it.withConfigurer(new YamlBukkitConfigurer(), new SerdesBukkit(), new SerdesGarden());
            it.withBindFile(new File(this.getDataFolder(), "config.yml"));
            it.saveDefaults();
            it.load(true);
        });
    }

    private void registerPlantRecipes() {
        for (PlantType plantType : MutableGardenRegistry.plantType.values()) {
            NamespacedKey namespacedKey = plantType.key();
            if (Bukkit.getRecipe(namespacedKey) != null) {
                Bukkit.removeRecipe(namespacedKey);
            }

            ShapelessRecipe recipe = new ShapelessRecipe(namespacedKey, plantType.newSeeds().newItem(4));
            recipe.addIngredient(plantType.newFruit().newItem(1));
            Bukkit.addRecipe(recipe);
        }
    }

    public static NamespacedKey key(String key) {
        if (!Key.parseableValue(key)) {
            return null;
        }
        return new NamespacedKey("garden", key);
    }

}