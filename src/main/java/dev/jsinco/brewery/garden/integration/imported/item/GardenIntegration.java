package dev.jsinco.brewery.garden.integration.imported.item;

import dev.jsinco.brewery.garden.Garden;
import dev.jsinco.brewery.garden.MutableGardenRegistry;
import dev.jsinco.brewery.garden.api.integration.ItemIntegration;
import dev.jsinco.brewery.garden.plant.PlantType;
import dev.jsinco.brewery.garden.plant.item.PlantItem;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class GardenIntegration implements ItemIntegration {
    @Override
    public Optional<ItemStack> toBukkit(String identifier) {
        String plantTypeKey = identifier.replaceAll("_seeds|_fruit", "");
        PlantType plantType = MutableGardenRegistry.PLANT_TYPE.get(Garden.key(plantTypeKey));
        if (plantType == null) {
            return Optional.empty();
        }
        if (identifier.endsWith("_seeds")) {
            return plantType.seedItem().item(PlantItem.PlantItemType.SEEDS, plantType);
        }
        return plantType.fruitItem().item(PlantItem.PlantItemType.FRUIT, plantType);
    }

    @Override
    public String namespace() {
        return "garden";
    }

    @Override
    public void initialize() {

    }

    @Override
    public CompletableFuture<Void> validationReady() {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public boolean isValid(String identifier) {
        String plantTypeKey = identifier.replaceAll("_seeds|_fruit", "");
        return MutableGardenRegistry.PLANT_TYPE.get(Garden.key(plantTypeKey)) != null;
    }
}
