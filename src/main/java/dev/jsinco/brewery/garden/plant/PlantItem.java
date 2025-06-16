package dev.jsinco.brewery.garden.plant;

import dev.jsinco.brewery.garden.Garden;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public interface PlantItem {
    NamespacedKey PLANT_TYPE_KEY = new NamespacedKey(Garden.getInstance(), "plant_type");
    NamespacedKey ITEM_TYPE_KEY = new NamespacedKey(Garden.getInstance(), "item_type");

    String simpleName();

    ItemStack newItem(int amount);

    GardenItemType itemType();

    PlantType plantType();

    enum GardenItemType {
        FRUIT,
        SEEDS
    }
}
