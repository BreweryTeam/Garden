package dev.jsinco.brewery.garden.plant;

import com.google.common.collect.ImmutableList;
import dev.jsinco.brewery.garden.Garden;
import dev.jsinco.brewery.garden.plant.item.PlantItem;
import dev.jsinco.brewery.garden.utility.Encoder;
import net.kyori.adventure.key.Key;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.List;
import java.util.UUID;

public record PlacedFruitDisplays(List<ItemDisplay> itemDisplays, Entity interactionBox, Key plantType) {

    public static final NamespacedKey INTERACTION_ENTITY_DATA = Garden.key("linked_entities");
    public static final NamespacedKey OWNING_PLANT = Garden.key("owner");
    public static final float ONE_OVER_SQRT_2 = (float) (1 / Math.sqrt(2));

    public static PlacedFruitDisplays generate(ItemStack itemSource, BlockFace relative, Block block, Key plantType, UUID owner, float placedScale, int displayCount) {
        Location center = block.getLocation().toCenterLocation()
                .subtract(relative.getDirection().multiply(0.5 - placedScale * 0.25));
        float degreeDiff = (float) (Math.PI / (displayCount));
        ImmutableList.Builder<ItemDisplay> itemDisplaysBuilder = ImmutableList.builder();
        for (int i = 0; i < displayCount; i++) {
            final int iFinal = i;
            itemDisplaysBuilder.add(center.getWorld().spawn(center, ItemDisplay.class, entity -> {
                entity.setTransformation(new Transformation(
                        new Vector3f(),
                        new AxisAngle4f((float) (Math.PI / 4) + degreeDiff * iFinal, 0, 1, 0),
                        new Vector3f(placedScale, placedScale, placedScale),
                        new AxisAngle4f()
                ));
                entity.setPersistent(false);
                entity.setItemStack(itemSource);
            }));
        }
        // Display entities will have
        Vector diff = new Vector(
                0,
                -placedScale / 2,
                0
        );
        List<ItemDisplay> itemDisplays = itemDisplaysBuilder.build();
        Entity interaction = center.getWorld().spawn(center.add(diff), Interaction.class, entity -> {
            entity.setNoPhysics(true);
            entity.setPersistent(false);
            entity.setInvulnerable(true);
            entity.setInteractionWidth(placedScale * ONE_OVER_SQRT_2 * 0.9F);
            entity.setInteractionHeight(placedScale * 0.9F);
            PersistentDataContainer pdc = entity.getPersistentDataContainer();
            pdc.set(
                    INTERACTION_ENTITY_DATA,
                    PersistentDataType.LIST.listTypeFrom(PersistentDataType.BYTE_ARRAY),
                    itemDisplays.stream()
                            .map(ItemDisplay::getUniqueId)
                            .map(Encoder::asBytes)
                            .toList()
            );
            pdc.set(
                    PlantItem.PLANT_TYPE_KEY,
                    PersistentDataType.STRING,
                    plantType.asMinimalString()
            );
            pdc.set(OWNING_PLANT, PersistentDataType.BYTE_ARRAY, Encoder.asBytes(owner));
        });
        return new PlacedFruitDisplays(itemDisplays, interaction, plantType);
    }


    public boolean hasDepopulated() {
        return itemDisplays.stream().anyMatch(Entity::isDead) || interactionBox.isDead();
    }

    public void remove() {
        itemDisplays.forEach(Entity::remove);
        interactionBox.remove();
    }

    private static Vector3f toVector4f(Vector vector) {
        return new Vector3f((float) vector.getX(), (float) vector.getY(), (float) vector.getZ());
    }
}
