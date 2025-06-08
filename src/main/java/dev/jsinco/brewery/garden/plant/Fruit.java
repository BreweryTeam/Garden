package dev.jsinco.brewery.garden.plant;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Consumable;
import io.papermc.paper.datacomponent.item.FoodProperties;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record Fruit(String simpleName, PlantType plantType) implements PlantItem {
    private static final NamespacedKey TBP_TAG = new NamespacedKey("brewery", "tag");
    private static final NamespacedKey TBP_SCORE = new NamespacedKey("brewery", "score");
    private static final NamespacedKey TBP_DISPLAY_NAME = new NamespacedKey("brewery", "display_name");

    @Override
    public ItemStack newItem(int amount) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD, amount);
        item.setData(DataComponentTypes.CUSTOM_NAME, MiniMessage.miniMessage().deserialize(plantType.displayName()));
        item.setData(DataComponentTypes.PROFILE, ResolvableProfile.resolvableProfile(plantType.getPlayerProfile()));
        item.setData(DataComponentTypes.CONSUMABLE, Consumable.consumable().hasConsumeParticles(false).build());
        item.setData(DataComponentTypes.FOOD, FoodProperties.food().nutrition(3).saturation(2.0f).build());

        // TODO: Ask in Paper discord how to use PDC with new ItemMeta API
        ItemMeta meta = item.getItemMeta();
        meta.lore(List.of(Component.text("A sweet fruit").color(NamedTextColor.DARK_GRAY)));
        PersistentDataContainer persistentDataContainer = meta.getPersistentDataContainer();
        persistentDataContainer.set(PLANT_TYPE_KEY, PersistentDataType.STRING, plantType.key().toString());
        persistentDataContainer.set(TBP_TAG, PersistentDataType.STRING, "garden:" + plantType.key());
        persistentDataContainer.set(TBP_SCORE, PersistentDataType.DOUBLE, 1D);
        persistentDataContainer.set(TBP_DISPLAY_NAME, PersistentDataType.STRING, plantType.displayName());
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public GardenItemType itemType() {
        return GardenItemType.FRUIT;
    }

    public static boolean isFruit(ItemStack item) {
        return false; //TODO
    }

    @Nullable
    public static Seeds getFruit(ItemStack item) {
        return null; // TODO
    }
}
