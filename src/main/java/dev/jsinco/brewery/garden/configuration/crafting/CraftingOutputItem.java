package dev.jsinco.brewery.garden.configuration.crafting;

import dev.jsinco.brewery.garden.Garden;
import dev.jsinco.brewery.garden.plant.item.extra.ExtraItemData;
import dev.jsinco.brewery.garden.utility.Logger;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.persistence.PersistentDataType;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.Optional;

@ConfigSerializable
public class CraftingOutputItem {

    NamespacedKey itemModel;

    NamespacedKey itemId;

    Component displayName;

    ExtraItemData extraData;

    public Optional<ItemStack> toItem(NamespacedKey itemKey) {
        ItemType itemType = Registry.ITEM.get(itemId);
        if (itemType == null) {
            Logger.logWarn("Unknown crafting item output '%s': Only Minecraft items are supported".formatted(itemId));
            return Optional.empty();
        }
        ItemStack itemStack = itemType.createItemStack();
        itemStack.unsetData(DataComponentTypes.CONSUMABLE);
        itemStack.setData(DataComponentTypes.ITEM_MODEL, itemModel);
        itemStack.setData(DataComponentTypes.CUSTOM_NAME, displayName
                .colorIfAbsent(NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
        );
        extraData.apply(itemStack);
        itemStack.editPersistentDataContainer(pdc ->
                pdc.set(Garden.ITEM, PersistentDataType.STRING, Garden.minimized(itemKey))
        );
        return Optional.of(itemStack);
    }
}
