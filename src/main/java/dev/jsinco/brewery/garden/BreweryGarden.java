package dev.jsinco.brewery.garden;

import com.dre.brewery.recipe.PluginItem;
import dev.jsinco.brewery.garden.commands.GardenCommand;
import dev.jsinco.brewery.garden.configuration.BreweryGardenConfig;
import dev.jsinco.brewery.garden.constants.PlantType;
import dev.jsinco.brewery.garden.constants.PlantTypeSeeds;
import dev.jsinco.brewery.garden.events.EventListeners;
import dev.jsinco.brewery.garden.integration.BreweryGardenIngredient;
import dev.jsinco.brewery.garden.objects.GardenPlant;
import dev.jsinco.brewery.garden.persist.Database;
import dev.jsinco.brewery.garden.persist.GardenPlantDataType;
import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.yaml.bukkit.YamlBukkitConfigurer;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BreweryGarden extends JavaPlugin {

    // TODO:
    //  I'd like to swap to a schematic based system for plants in this addon eventually.
    //  Having fruit trees would be a really nice feature. Additionally, I want to expand the config
    //  of this addon eventually.

    @Getter
    private static BreweryGarden instance;
    @Getter
    private static GardenRegistry gardenRegistry;
    private static int taskID;
    @Getter
    private BreweryGardenConfig pluginConfiguration;
    private Database database;
    @Getter
    private GardenPlantDataType gardenPlantDataType;

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
        gardenRegistry = new GardenRegistry();
        gardenPlantDataType = new GardenPlantDataType(database);
        try {
            PluginItem.registerForConfig(this.getName(), BreweryGardenIngredient::new);
        } catch (NoClassDefFoundError ignored) {

        }
        this.pluginConfiguration = compileConfig();
        Bukkit.getPluginManager().registerEvents(new EventListeners(gardenRegistry, gardenPlantDataType), this);
        taskID = Bukkit.getScheduler().runTaskTimer(this, new PlantGrowthRunnable(gardenRegistry), 1L, 6000L).getTaskId(); // 5 minutes
        this.registerPlantRecipes();
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, GardenCommand::register);
    }

    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTask(taskID);
    }

    public void reload() {
        this.pluginConfiguration = compileConfig();
    }

    private BreweryGardenConfig compileConfig() {
        return ConfigManager.create(BreweryGardenConfig.class, it -> {
            it.withConfigurer(new YamlBukkitConfigurer());
            it.withBindFile(new File(this.getDataFolder(), "config.yml"));
            it.saveDefaults();
            it.load(true);
        });
    }

    private void registerPlantRecipes() {
        for (PlantTypeSeeds plantTypeSeeds : PlantTypeSeeds.values()) {
            PlantType plantType = plantTypeSeeds.getParent();
            NamespacedKey namespacedKey = new NamespacedKey(this, plantType.name());
            if (Bukkit.getRecipe(namespacedKey) != null) {
                Bukkit.removeRecipe(namespacedKey);
            }

            ShapelessRecipe recipe = new ShapelessRecipe(namespacedKey, plantTypeSeeds.getItemStack(4));
            recipe.addIngredient(plantType.getItemStack(1));
            Bukkit.addRecipe(recipe);
        }
    }


    public static class PlantGrowthRunnable implements Runnable {

        private final GardenRegistry gardenRegistry;
        private final Random random = new Random();

        public PlantGrowthRunnable(GardenRegistry gardenRegistry) {
            this.gardenRegistry = gardenRegistry;
        }

        @Override
        public void run() {
            GardenPlantDataType gardenPlantDataType1 = BreweryGarden.getInstance().getGardenPlantDataType();
            List<GardenPlant> toRemove = new ArrayList<>(); // dont concurrently modify
            gardenRegistry.getGardenPlants().forEach(gardenPlant -> {
                if (!gardenPlant.isValid()) {
                    toRemove.add(gardenPlant);
                } else if (random.nextInt(100) > 20) {
                    gardenPlant.incrementGrowthStage(1);
                    gardenPlantDataType1.update(gardenPlant);
                }

                if (gardenPlant.isFullyGrown()) {
                    if (gardenPlant.isValid()) {
                        gardenPlant.place();
                    } else {
                        toRemove.add(gardenPlant);
                    }
                }
            });
            toRemove.forEach(gardenRegistry::unregisterPlant);
            toRemove.forEach(gardenPlantDataType1::remove);
        }
    }
}