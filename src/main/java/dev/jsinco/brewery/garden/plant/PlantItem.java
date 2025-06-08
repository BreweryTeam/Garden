package dev.jsinco.brewery.garden.plant;

import dev.jsinco.brewery.garden.BreweryGarden;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public interface PlantItem {
    NamespacedKey PLANT_TYPE_KEY = new NamespacedKey(BreweryGarden.getInstance(), "plant_type");
    NamespacedKey ITEM_TYPE_KEY = new NamespacedKey(BreweryGarden.getInstance(), "item_type");

    String simpleName();

    ItemStack newItem(int amount);

    GardenItemType itemType();

    PlantType plantType();

    enum GardenItemType {
        FRUIT,
        SEEDS
    }
}
