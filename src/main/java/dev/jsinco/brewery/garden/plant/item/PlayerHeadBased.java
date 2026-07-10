package dev.jsinco.brewery.garden.plant.item;

import com.destroystokyo.paper.profile.PlayerProfile;
import dev.jsinco.brewery.garden.MutableGardenRegistry;
import dev.jsinco.brewery.garden.plant.PlacedFruitDisplays;
import dev.jsinco.brewery.garden.plant.PlantType;
import dev.jsinco.brewery.garden.utility.CachedValue;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Consumable;
import io.papermc.paper.datacomponent.item.FoodProperties;
import io.papermc.paper.datacomponent.item.ItemLore;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockType;
import org.bukkit.block.Skull;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.awt.Color;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@NullMarked
public record PlayerHeadBased(CachedValue<PlayerProfile> profile, Component displayName,
                              List<Component> lore, @Nullable Color color0) implements PlantItem {
    private static final UUID CONSTANT_UUID = UUID.fromString("f714a407-f7c9-425c-958d-c9914aeac05c");

    @Override
    public void validate(String context) {
    }

    @Override
    public Optional<ItemStack> item(int amount, PlantItemType type, PlantType plantType) {
        ItemStack item = ItemStack.of(Material.PLAYER_HEAD, amount);
        item.setData(DataComponentTypes.CUSTOM_NAME, displayName
                .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                .colorIfAbsent(NamedTextColor.WHITE));
        item.setData(DataComponentTypes.PROFILE, ResolvableProfile.resolvableProfile(profile.get()));
        item.setData(DataComponentTypes.CONSUMABLE, Consumable.consumable().hasConsumeParticles(false).build());
        item.setData(DataComponentTypes.LORE, ItemLore.lore(
                lore.stream()
                        .map(component -> component.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                        .map(component -> component.colorIfAbsent(NamedTextColor.GRAY))
                        .toList()
        ));
        item.setData(DataComponentTypes.FOOD, FoodProperties.food().nutrition(3).saturation(2.0f).build());
        item.editPersistentDataContainer(pdc -> {
            pdc.set(ITEM_TYPE_KEY, PersistentDataType.STRING, type.name());
            pdc.set(PLANT_TYPE_KEY, PersistentDataType.STRING, plantType.key().toString());
        });
        return Optional.of(item);
    }

    @Override
    public Optional<PlacedFruitDisplays> place(Block relative, BlockFace facing, UUID owningPlant, PlantType plantType) {
        Skull skull;
        if (facing == BlockFace.UP || facing == BlockFace.DOWN) {
            skull = (Skull) BlockType.PLAYER_HEAD.createBlockData().createBlockState();
        } else {
            skull = (Skull) BlockType.PLAYER_WALL_HEAD.createBlockData(wallHead -> wallHead.setFacing(facing)).createBlockState();
        }
        skull.setPlayerProfile(profile.get());
        skull.getPersistentDataContainer().set(PLANT_TYPE_KEY, PersistentDataType.STRING, plantType.key().toString());
        skull.copy(relative.getLocation()).update(true);
        return Optional.empty();
    }

    @Override
    public Optional<Color> color() {
        return Optional.ofNullable(color0);
    }

    @Nullable
    public static PlantType getPlantType(Block block) {
        if (block.getType() != Material.PLAYER_HEAD && block.getType() != Material.PLAYER_WALL_HEAD) return null;
        Skull skull = (Skull) block.getState();
        String key = skull.getPersistentDataContainer().get(PLANT_TYPE_KEY, PersistentDataType.STRING);
        if (key == null) return null;
        return MutableGardenRegistry.PLANT_TYPE.get(NamespacedKey.fromString(key));
    }


}
