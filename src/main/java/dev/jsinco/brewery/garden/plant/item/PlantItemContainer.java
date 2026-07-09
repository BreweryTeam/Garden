package dev.jsinco.brewery.garden.plant.item;

import dev.jsinco.brewery.garden.plant.PlantType;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public record PlantItemContainer(PlantItem plantItem, PlantType plantType, PlantItem.PlantItemType plantItemType) {

    public Optional<ItemStack> toItem(int amount) {
        return plantItem.item(amount, plantItemType, plantType);
    }
}
