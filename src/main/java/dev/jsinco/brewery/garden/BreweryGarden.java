package dev.jsinco.brewery.garden;

import dev.jsinco.brewery.garden.commands.AddonCommandManager;
import dev.jsinco.brewery.garden.configuration.BreweryGardenConfig;
import dev.jsinco.brewery.garden.constants.PlantType;
import dev.jsinco.brewery.garden.constants.PlantTypeSeeds;
import dev.jsinco.brewery.garden.events.EventListeners;
import dev.jsinco.brewery.garden.objects.GardenPlant;
import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.yaml.bukkit.YamlBukkitConfigurer;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class BreweryGarden extends JavaPlugin {

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

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        gardenRegistry = new GardenRegistry();
        this.pluginConfiguration = compileConfig();
        Bukkit.getPluginManager().registerEvents(new EventListeners(gardenRegistry), this);
        AddonCommandManager commandManager = new AddonCommandManager();
        this.getCommand("garden").setExecutor(commandManager);
        this.getCommand("garden").setTabCompleter(commandManager);
        taskID = Bukkit.getScheduler().runTaskTimer(this, new PlantGrowthRunnable(gardenRegistry), 1L, 6000L).getTaskId(); // 5 minutes
        this.registerPlantRecipes();
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
            List<GardenPlant> toRemove = new ArrayList<>(); // dont concurrently modify
            gardenRegistry.getGardenPlants().forEach(gardenPlant -> {
                if (!gardenPlant.isValid()) {
                    toRemove.add(gardenPlant);
                } else if (random.nextInt(100) > 20) {
                    gardenPlant.incrementGrowthStage(1);
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
        }
    }
}