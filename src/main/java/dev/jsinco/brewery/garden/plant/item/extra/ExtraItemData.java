package dev.jsinco.brewery.garden.plant.item.extra;

import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.function.Consumer;

public record ExtraItemData(List<Consumer<ItemStack>> modifications) {

    public void apply(ItemStack itemStack) {
        modifications.forEach(modification -> modification.accept(itemStack));
    }
}
