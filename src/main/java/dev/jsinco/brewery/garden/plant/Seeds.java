package dev.jsinco.brewery.garden.plant;

import dev.jsinco.brewery.garden.GardenRegistry;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.persistence.PersistentDataContainerView;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record Seeds(String simpleName, PlantType plantType) implements PlantItem {

    public static boolean isSeeds(ItemStack item) {
        PersistentDataContainerView persistentDataContainer = item.getPersistentDataContainer();
        return persistentDataContainer.has(ITEM_TYPE_KEY) && persistentDataContainer.get(ITEM_TYPE_KEY, PersistentDataType.STRING).equals(GardenItemType.SEEDS.name());
    }

    @Nullable
    public static Seeds getSeeds(ItemStack item) {
        PersistentDataContainerView containerView = item.getPersistentDataContainer();
        if (!containerView.has(ITEM_TYPE_KEY) || !containerView.has(PLANT_TYPE_KEY) || !containerView.get(ITEM_TYPE_KEY, PersistentDataType.STRING).equals(GardenItemType.SEEDS.name())) {
            return null;
        }
        PlantType type = GardenRegistry.PLANT_TYPE.get(NamespacedKey.fromString(containerView.get(PLANT_TYPE_KEY, PersistentDataType.STRING)));
        return type.newSeeds();
    }

    @Override
    public ItemStack newItem(int amount) {
        ItemStack item = new ItemStack(plantType.seedMaterial(), amount);
        item.setData(DataComponentTypes.ITEM_NAME, MiniMessage.miniMessage().deserialize(plantType.displayName()));

        // TODO: Ask in Paper discord how to use PDC with new ItemMeta API
        ItemMeta meta = item.getItemMeta();
        meta.lore(List.of(Component.text("Rough seeds").color(NamedTextColor.DARK_GRAY)));
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(ITEM_TYPE_KEY, PersistentDataType.STRING, itemType().name());
        container.set(PLANT_TYPE_KEY, PersistentDataType.STRING, plantType().key().toString());
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public GardenItemType itemType() {
        return GardenItemType.SEEDS;
    }
}
