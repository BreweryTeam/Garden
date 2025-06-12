package dev.jsinco.brewery.garden.plant;

import dev.jsinco.brewery.garden.GardenRegistry;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Consumable;
import io.papermc.paper.datacomponent.item.FoodProperties;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import io.papermc.paper.persistence.PersistentDataContainerView;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockType;
import org.bukkit.block.Skull;
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
        persistentDataContainer.set(ITEM_TYPE_KEY, PersistentDataType.STRING, itemType().name());
        persistentDataContainer.set(PLANT_TYPE_KEY, PersistentDataType.STRING, plantType.key().toString());
        persistentDataContainer.set(TBP_TAG, PersistentDataType.STRING, plantType.key().toString());
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
        PersistentDataContainerView view = item.getPersistentDataContainer();
        return view.has(ITEM_TYPE_KEY) && view.get(ITEM_TYPE_KEY, PersistentDataType.STRING).equals(GardenItemType.FRUIT.name());
    }

    @Nullable
    public static Fruit getFruit(ItemStack item) {
        if (!isFruit(item)) {
            return null;
        }
        PersistentDataContainerView view = item.getPersistentDataContainer();
        if (!view.has(PLANT_TYPE_KEY)) {
            return null;
        }
        PlantType type = GardenRegistry.PLANT_TYPE.get(NamespacedKey.fromString(view.get(PLANT_TYPE_KEY, PersistentDataType.STRING)));
        if (type == null) {
            return null;
        }
        return type.newFruit();
    }

    public void placeFruit(Block relative, BlockFace facing) {
        Skull skull;
        if (facing == BlockFace.UP) {
            skull = (Skull) BlockType.PLAYER_HEAD.createBlockData().createBlockState();
        } else {
            skull = (Skull) BlockType.PLAYER_WALL_HEAD.createBlockData(wallHead -> wallHead.setFacing(facing)).createBlockState();
        }
        skull.setPlayerProfile(plantType.getPlayerProfile());
        skull.getPersistentDataContainer().set(PLANT_TYPE_KEY, PersistentDataType.STRING, plantType.key().toString());
        skull.copy(relative.getLocation()).update(true);
    }

    @Nullable
    public static PlantType getPlantType(Block block) {
        if (block.getType() != Material.PLAYER_HEAD && block.getType() != Material.PLAYER_WALL_HEAD) return null;
        Skull skull = (Skull) block.getState();
        String key = skull.getPersistentDataContainer().get(PLANT_TYPE_KEY, PersistentDataType.STRING);
        if (key == null) return null;
        return GardenRegistry.PLANT_TYPE.get(NamespacedKey.fromString(key));
    }
}
