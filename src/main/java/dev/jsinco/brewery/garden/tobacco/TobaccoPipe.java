package dev.jsinco.brewery.garden.tobacco;

import dev.jsinco.brewery.garden.Garden;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Consumable;
import io.papermc.paper.datacomponent.item.consumable.ItemUseAnimation;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.persistence.PersistentDataType;

public class TobaccoPipe {

    public static NamespacedKey ITEM = Garden.key("item");
    public static NamespacedKey FILL_LEVEL = Garden.key("tobacco/fill_level");
    public static NamespacedKey LIT = Garden.key("tobacco/lit");

    public static ItemStack newPipe() {
        ItemStack itemStack = ItemType.COOKIE.createItemStack();
        itemStack.setData(DataComponentTypes.ITEM_MODEL, ItemType.GOAT_HORN.getKey());
        itemStack.setData(DataComponentTypes.CONSUMABLE, Consumable.consumable()
                .animation(ItemUseAnimation.TOOT_HORN)
                .consumeSeconds(1.5F)
                .hasConsumeParticles(false)
                .sound(Registry.SOUNDS.getKey(Sound.BLOCK_FIRE_AMBIENT))
                .build());
        itemStack.unsetData(DataComponentTypes.FOOD);
        itemStack.setData(DataComponentTypes.CUSTOM_NAME, Component.text("Tobacco Pipe")
                .colorIfAbsent(NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
        );
        itemStack.editPersistentDataContainer(pdc -> pdc.set(ITEM, PersistentDataType.STRING, "tobacco_pipe"));
        return itemStack;
    }

    public static boolean isPipe(ItemStack itemStack) {
        return "tobacco_pipe".equalsIgnoreCase(itemStack.getPersistentDataContainer()
                .get(ITEM, PersistentDataType.STRING)
        );
    }

    public static boolean isLit(ItemStack item) {
        return item.getPersistentDataContainer()
                .get(LIT, PersistentDataType.BOOLEAN) == Boolean.TRUE;
    }
}
