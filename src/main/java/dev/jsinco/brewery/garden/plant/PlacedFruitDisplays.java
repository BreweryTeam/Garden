package dev.jsinco.brewery.garden.plant;

import dev.jsinco.brewery.garden.Garden;
import dev.jsinco.brewery.garden.plant.item.PlantItem;
import dev.jsinco.brewery.garden.utility.Encoder;
import net.kyori.adventure.key.Key;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.List;
import java.util.UUID;

public record PlacedFruitDisplays(List<ItemDisplay> itemDisplays, Entity interactionBox, Key plantType) {

    public static final NamespacedKey INTERACTION_ENTITY_DATA = Garden.key("linked_entities");
    public static final NamespacedKey OWNING_PLANT = Garden.key("owner");


    public static PlacedFruitDisplays generate(ItemStack itemSource, BlockFace relative, Block block, Key plantType, UUID owner, float placedScale) {
        Location center = block.getLocation().toCenterLocation()
                .subtract(relative.getDirection().multiply(0.25));
        ItemDisplay itemDisplay1 = center.getWorld().spawn(center, ItemDisplay.class, entity -> {
            entity.setTransformation(new Transformation(
                    new Vector3f(),
                    new AxisAngle4f((float) (Math.PI / 4), 0, 1, 0),
                    new Vector3f(placedScale, placedScale, placedScale),
                    new AxisAngle4f()
            ));
            entity.setPersistent(false);
            entity.setItemStack(itemSource);
        });
        ItemDisplay itemDisplay2 = center.getWorld().spawn(center, ItemDisplay.class, entity -> {
            entity.setTransformation(new Transformation(
                    new Vector3f(),
                    new AxisAngle4f((float) -(Math.PI / 4), 0, 1, 0),
                    new Vector3f(placedScale, placedScale, placedScale),
                    new AxisAngle4f()
            ));
            entity.setPersistent(false);
            entity.setItemStack(itemSource);
        });
        Entity interaction = center.getWorld().spawn(center, Bat.class, bat -> {
            bat.setAI(false);
            bat.setPersistent(false);
            bat.setInvisible(true);
            bat.setInvulnerable(true);
            PersistentDataContainer pdc = bat.getPersistentDataContainer();
            pdc.set(
                    INTERACTION_ENTITY_DATA,
                    PersistentDataType.LIST.listTypeFrom(PersistentDataType.BYTE_ARRAY),
                    List.of(Encoder.asBytes(itemDisplay1.getUniqueId()), Encoder.asBytes(itemDisplay2.getUniqueId()))
            );
            pdc.set(
                    PlantItem.PLANT_TYPE_KEY,
                    PersistentDataType.STRING,
                    plantType.asMinimalString()
            );
            pdc.set(OWNING_PLANT, PersistentDataType.BYTE_ARRAY, Encoder.asBytes(owner));
        });
        return new PlacedFruitDisplays(List.of(itemDisplay1, itemDisplay2), interaction, plantType);
    }


    public boolean hasDepopulated() {
        return itemDisplays.stream().anyMatch(Entity::isDead) || interactionBox.isDead();
    }

    public void remove() {
        itemDisplays.forEach(Entity::remove);
        interactionBox.remove();
    }
}
