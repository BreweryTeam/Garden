package dev.jsinco.brewery.garden.plant;

import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record Seeds(String simpleName, PlantType plantType, Material seedMaterial) implements PlantItem {

    public static boolean isSeeds(ItemStack item) {
        return false; //TODO
    }

    @Nullable
    public static Seeds getSeeds(ItemStack item) {
        return null; // TODO
    }

    @Override
    public ItemStack newItem(int amount) {
        ItemStack item = new ItemStack(this.seedMaterial, amount);
        item.setData(DataComponentTypes.ITEM_NAME, MiniMessage.miniMessage().deserialize(plantType.displayName()));

        // TODO: Ask in Paper discord how to use PDC with new ItemMeta API
        ItemMeta meta = item.getItemMeta();
        meta.lore(List.of(Component.text("Rough seeds").color(NamedTextColor.DARK_GRAY)));
        // TODO Persistent data
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public GardenItemType itemType() {
        return GardenItemType.FRUIT;
    }
}
