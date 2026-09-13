package dev.jsinco.brewery.garden;

import com.dre.brewery.recipe.PluginItem;
import com.google.common.base.Preconditions;
import dev.jsinco.brewery.garden.api.integration.ItemIntegration;
import dev.jsinco.brewery.garden.commands.GardenCommand;
import dev.jsinco.brewery.garden.configuration.GardenConfig;
import dev.jsinco.brewery.garden.configuration.GardenTranslator;
import dev.jsinco.brewery.garden.integration.exported.BreweryGardenIngredient;
import dev.jsinco.brewery.garden.integration.exported.TBPGardenIntegration;
import dev.jsinco.brewery.garden.integration.imported.IntegrationRegistryImpl;
import dev.jsinco.brewery.garden.listener.BlockEventListener;
import dev.jsinco.brewery.garden.listener.EventListeners;
import dev.jsinco.brewery.garden.listener.WorldEventListener;
import dev.jsinco.brewery.garden.persist.Database;
import dev.jsinco.brewery.garden.persist.GardenPlantDataType;
import dev.jsinco.brewery.garden.plant.GardenPlant;
import dev.jsinco.brewery.garden.plant.GrowthManager;
import dev.jsinco.brewery.garden.plant.PlantType;
import dev.jsinco.brewery.garden.plant.item.PlantItem;
import dev.jsinco.brewery.garden.utility.Logger;
import dev.thorinwasher.blockutil.api.BlockUtilAPI;
import dev.thorinwasher.blockutil.api.event.BlockDisableDropEvent;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import lombok.Getter;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class Garden extends JavaPlugin {

    @Getter
    private static Garden instance;
    @Getter
    private static PlantRegistry gardenRegistry;
    private Database database;
    @Getter
    private GardenPlantDataType gardenPlantDataType;
    @Getter
    private BlockUtilAPI blockUtil;
    private GardenTranslator translator;
    private boolean loadSuccess = false;
    private ScheduledTask growthTask;
    @Getter
    private IntegrationRegistryImpl integrationRegistry;
    private WorldEventListener worldEventListener;

    @Override
    public void onLoad() {
        instance = this;
        this.integrationRegistry = new IntegrationRegistryImpl();
        integrationRegistry.registerDefaults(this);
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
        integrationRegistry.itemIntegrations().forEach(ItemIntegration::initialize);
        Preconditions.checkState(loadSuccess, "Failed on load, see logs.");
        this.database = new Database();
        try {
            database.init(this.getDataFolder());
        } catch (IOException | SQLException e) {
            throw new RuntimeException(e);
        }
        gardenRegistry = new PlantRegistry();
        gardenPlantDataType = database.plantDataType();
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

        translator = new GardenTranslator(new File(this.getDataFolder(), "locale"));
        translator.reload();
        GlobalTranslator.translator().addSource(translator);
        this.worldEventListener = new WorldEventListener(gardenRegistry, gardenPlantDataType);
        Bukkit.getWorlds().forEach(worldEventListener::loadWorld);
        Bukkit.getPluginManager().registerEvents(new EventListeners(gardenRegistry, gardenPlantDataType), this);
        Bukkit.getPluginManager().registerEvents(new BlockEventListener(gardenRegistry, gardenPlantDataType), this);
        Bukkit.getPluginManager().registerEvents(worldEventListener, this);
        this.registerPlantRecipes();
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, GardenCommand::register);
        GrowthManager growthManager = new GrowthManager(gardenRegistry, gardenPlantDataType);
        growthTask = Bukkit.getGlobalRegionScheduler().runAtFixedRate(this, t -> growthManager.tick(), 1, 200);
        Bukkit.getGlobalRegionScheduler().runAtFixedRate(this, t -> {
            gardenRegistry.getPlants()
                    .forEach(GardenPlant::tick);
        }, 1, 100);
        if (MutableGardenRegistry.PLANT_TYPE.values().isEmpty()) {
            Logger.logErr("There's no available plant types for garden!");
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        if (growthTask != null) {
            growthTask.cancel();
        }
        GlobalTranslator.translator().removeSource(translator);
        if (database != null) {
            database.close();
        }
    }

    private void savePlantResources() {
        File plantsFolder = getDataPath().resolve("plants").toFile();
        if (plantsFolder.exists()) {
            return;
        }
        URL resource = Garden.class.getResource("/plants");
        if (resource == null) {
            throw new RuntimeException("Could not find internal resource: '/plants' :(");
        }

        try {
            URI uri = resource.toURI();
            if ("jar".equals(uri.getScheme())) {
                try (FileSystem fileSystem = FileSystems.newFileSystem(uri, Map.of())) {
                    copyResourceTree(fileSystem.getPath("/plants"), plantsFolder.toPath());
                }
            } else {
                copyResourceTree(Path.of(uri), plantsFolder.toPath());
            }
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void copyResourceTree(Path source, Path target) throws IOException {
        try (Stream<Path> stream = Files.walk(source)) {
            Iterator<Path> iterator = stream.iterator();
            while (iterator.hasNext()) {
                Path path = iterator.next();
                String relative = source.relativize(path).toString();
                Path destination = target.resolve(relative);
                if (Files.isDirectory(path)) {
                    Files.createDirectories(destination);
                } else {
                    Path parent = destination.getParent();
                    if (parent != null) {
                        Files.createDirectories(parent);
                    }
                    Files.copy(path, destination, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    private void handleBlockDrops(BlockDisableDropEvent blockDisableDropEvent) {
        Material material = blockDisableDropEvent.getBlock().getType();
        if (GardenConfig.instance().dropsDefaultItems().stream().anyMatch(tag -> tag.isTagged(material))) {
            blockDisableDropEvent.setDisableDrops(false);
            return;
        }
        for (Map.Entry<Tag<Material>, Material> entry : GardenConfig.instance().dropOverride().entrySet()) {
            if (entry.getKey().isTagged(material)) {
                blockDisableDropEvent.setDropOverride(List.of(new ItemStack(entry.getValue())));
                return;
            }
        }
    }

    public void reload() {
        GardenConfig.MEMORIZED.reload();
        translator.reload();
        gardenRegistry.clear();
        MutableGardenRegistry.PLANT_TYPE.newBacking(PlantType.readPlantTypes());
        Bukkit.getWorlds().forEach(worldEventListener::loadWorld);
        registerPlantRecipes();
        if (MutableGardenRegistry.PLANT_TYPE.values().isEmpty()) {
            Logger.logErr("There's no available plant types for garden!");
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    private void registerPlantRecipes() {
        CompletableFuture.allOf(integrationRegistry.itemIntegrations()
                .map(ItemIntegration::validationReady)
                .toArray(CompletableFuture<?>[]::new)
        ).thenRunAsync(() -> {
            for (PlantType plantType : MutableGardenRegistry.PLANT_TYPE.values()) {
                NamespacedKey namespacedKey = plantType.key();
                if (Bukkit.getRecipe(namespacedKey) != null) {
                    Bukkit.removeRecipe(namespacedKey);
                }
                Optional<ItemStack> optionalSeeds = plantType.seedItem().item(4, PlantItem.PlantItemType.SEEDS, plantType);
                Optional<ItemStack> optionalFruits = plantType.fruitItem().item(PlantItem.PlantItemType.FRUIT, plantType);
                if (optionalFruits.isEmpty() || optionalSeeds.isEmpty()) {
                    continue;
                }
                ShapelessRecipe recipe = new ShapelessRecipe(namespacedKey, optionalSeeds.get());
                recipe.addIngredient(optionalFruits.get());
                Bukkit.addRecipe(recipe);
            }
            Bukkit.updateRecipes();
        }, runnable -> Bukkit.getGlobalRegionScheduler().run(this, task -> runnable.run()));
    }

    public static NamespacedKey key(String string) {
        String[] split = string.split(":", 2);
        String value;
        String namespace;
        if (split.length == 1) {
            namespace = "garden";
            value = string;
        } else {
            namespace = split[0];
            value = split[1];
        }
        if (!Key.parseableValue(value) || !Key.parseableNamespace(namespace)) {
            throw new IllegalArgumentException("Invalid key: " + string);
        }
        return new NamespacedKey(namespace, value);
    }

    public static String minimized(Key key) {
        if (key.namespace().equalsIgnoreCase("garden")) {
            return key.value();
        }
        return key.asString();
    }

}