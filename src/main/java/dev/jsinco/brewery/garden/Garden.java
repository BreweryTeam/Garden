package dev.jsinco.brewery.garden;

import com.dre.brewery.recipe.PluginItem;
import dev.jsinco.brewery.garden.commands.GardenCommand;
import dev.jsinco.brewery.garden.configuration.BreweryGardenConfig;
import dev.jsinco.brewery.garden.configuration.SerdesGarden;
import dev.jsinco.brewery.garden.integration.BreweryGardenIngredient;
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
import org.bukkit.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
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
        try {
            PluginItem.registerForConfig(this.getName(), BreweryGardenIngredient::new);
        } catch (NoClassDefFoundError ignored) {

        }
        this.pluginConfiguration = compileConfig();
        for (World world : Bukkit.getWorlds()) {
            List<GardenPlant> gardenPlants = gardenPlantDataType.fetch(world).join();
            gardenPlants.forEach(gardenRegistry::registerPlant);
        }
        Bukkit.getPluginManager().registerEvents(new EventListeners(gardenRegistry, gardenPlantDataType), this);
        Bukkit.getPluginManager().registerEvents(new BlockEventListener(gardenRegistry, gardenPlantDataType), this);
        this.registerPlantRecipes();
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, GardenCommand::register);
        Bukkit.getAsyncScheduler().runAtFixedRate(this, task -> new GrowthManager(gardenRegistry, gardenPlantDataType).tick(), 1L, 200L, TimeUnit.MILLISECONDS);
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
        this.pluginConfiguration = compileConfig();
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
        for (PlantType plantType : GardenRegistry.PLANT_TYPE.values()) {
            NamespacedKey namespacedKey = plantType.key();
            if (Bukkit.getRecipe(namespacedKey) != null) {
                Bukkit.removeRecipe(namespacedKey);
            }

            ShapelessRecipe recipe = new ShapelessRecipe(namespacedKey, plantType.newSeeds().newItem(4));
            recipe.addIngredient(plantType.newFruit().newItem(1));
            Bukkit.addRecipe(recipe);
        }
    }
}