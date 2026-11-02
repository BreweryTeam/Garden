package dev.jsinco.brewery.garden.tobacco;

import dev.jsinco.brewery.garden.Garden;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Consumable;
import io.papermc.paper.datacomponent.item.UseCooldown;
import io.papermc.paper.datacomponent.item.consumable.ItemUseAnimation;
import io.papermc.paper.persistence.PersistentDataContainerView;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.BoundingBox;

import java.util.Locale;

public class TobaccoPipe {

    private static final NamespacedKey ITEM = Garden.key("item");
    private static final NamespacedKey FILL_LEVEL = Garden.key("tobacco/fill_level");
    private static final NamespacedKey LIT = Garden.key("tobacco/lit");
    private static final NamespacedKey COOLDOWN = Garden.key("tobacco/cooldown");

    private static final int MAX_FILL_LEVEL = 15;

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
        itemStack.setData(DataComponentTypes.CUSTOM_NAME,
                GlobalTranslator.render(Component.translatable("garden.pipe.name"), Locale.ENGLISH)
                        .colorIfAbsent(NamedTextColor.WHITE)
                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
        );
        itemStack.setData(DataComponentTypes.USE_COOLDOWN, UseCooldown.useCooldown(8F)
                .cooldownGroup(COOLDOWN)
                .build()
        );
        itemStack.editPersistentDataContainer(pdc -> {
            pdc.set(ITEM, PersistentDataType.STRING, "tobacco_pipe");
        });
        setLit(itemStack, false);
        burn(itemStack);
        return itemStack;
    }

    public static boolean isPipe(ItemStack itemStack) {
        return "tobacco_pipe".equalsIgnoreCase(itemStack.getPersistentDataContainer()
                .get(ITEM, PersistentDataType.STRING)
        );
    }

    public static boolean isLit(ItemStack item) {
        PersistentDataContainerView pdc = item.getPersistentDataContainer();
        return pdc.get(LIT, PersistentDataType.BOOLEAN) == Boolean.TRUE;
    }

    public static void setLit(ItemStack item, boolean lit) {
        item.editPersistentDataContainer(pdc -> {
            pdc.set(LIT, PersistentDataType.BOOLEAN, lit);
        });
    }

    public static boolean isFilled(ItemStack item) {
        PersistentDataContainerView pdc = item.getPersistentDataContainer();
        Integer fillLevel = pdc.get(FILL_LEVEL, PersistentDataType.INTEGER);
        return fillLevel != null && fillLevel == MAX_FILL_LEVEL;
    }

    public static boolean isEmpty(ItemStack item) {
        PersistentDataContainerView pdc = item.getPersistentDataContainer();
        Integer fillLevel = pdc.get(FILL_LEVEL, PersistentDataType.INTEGER);
        return fillLevel != null && fillLevel == 0;
    }

    public static void stuff(ItemStack item) {
        item.editPersistentDataContainer(pdc -> {
            Integer fillLevel = pdc.get(FILL_LEVEL, PersistentDataType.INTEGER);
            pdc.set(FILL_LEVEL, PersistentDataType.INTEGER, fillLevel == null ? 1 : fillLevel + 1);
        });
    }

    public static void burn(ItemStack item) {
        item.editPersistentDataContainer(pdc -> {
            Integer fillLevel = pdc.get(FILL_LEVEL, PersistentDataType.INTEGER);
            pdc.set(FILL_LEVEL, PersistentDataType.INTEGER, fillLevel == null || fillLevel == 0 ? 0 : fillLevel - 1);
        });
    }

    public static boolean smokeIntersects(Location location) {
        double xPosInBlock = location.x() - Math.floor(location.x());
        double yPosInBlock = location.y() - Math.floor(location.y());
        double zPosInBlock = location.z() - Math.floor(location.z());
        BoundingBox smokeBoundingBox = new BoundingBox(
                xPosInBlock - 0.1,
                yPosInBlock - 0.1,
                zPosInBlock - 0.1,
                xPosInBlock + 0.1,
                yPosInBlock + 0.1,
                zPosInBlock + 0.1
        );
        return location.getBlock().getCollisionShape().overlaps(smokeBoundingBox);
    }
}
