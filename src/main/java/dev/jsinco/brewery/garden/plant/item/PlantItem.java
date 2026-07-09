package dev.jsinco.brewery.garden.plant.item;

import dev.jsinco.brewery.garden.Garden;
import dev.jsinco.brewery.garden.MutableGardenRegistry;
import dev.jsinco.brewery.garden.plant.PlacedFruitDisplays;
import dev.jsinco.brewery.garden.plant.PlantType;
import io.papermc.paper.persistence.PersistentDataContainerView;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@NullMarked
public interface PlantItem {
    NamespacedKey PLANT_TYPE_KEY = new NamespacedKey(Garden.getInstance(), "plant_type");
    NamespacedKey ITEM_TYPE_KEY = new NamespacedKey(Garden.getInstance(), "item_type");

    void validate(String context);

    default Optional<ItemStack> item(PlantItemType type, PlantType plantType) {
        return item(1, type, plantType);
    }

    Optional<ItemStack> item(int amount, PlantItemType type, PlantType plantType);

    Optional<PlacedFruitDisplays> place(Block relative, BlockFace facing, UUID owningPlant, PlantType plantType);

    Component displayName();

    static NamespacedKey key(PlantType plantType, PlantItemType itemType) {
        return Garden.key("%s_%s".formatted(Garden.minimized(plantType.key()), itemType.name().toLowerCase(Locale.ROOT)));
    }

    static @Nullable PlantType plantType(ItemStack item) {
        PersistentDataContainerView view = item.getPersistentDataContainer();
        String planttypeString = view.get(PLANT_TYPE_KEY, PersistentDataType.STRING);
        if (planttypeString == null) {
            return null;
        }
        return MutableGardenRegistry.PLANT_TYPE.get(NamespacedKey.fromString(planttypeString));
    }

    @Nullable
    static NamespacedKey plantItemKey(ItemStack item) {
        PersistentDataContainerView view = item.getPersistentDataContainer();
        String planttypeString = view.get(PLANT_TYPE_KEY, PersistentDataType.STRING);
        if (planttypeString == null) {
            return null;
        }
        PlantType type = MutableGardenRegistry.PLANT_TYPE.get(NamespacedKey.fromString(planttypeString));
        if (type == null) {
            return null;
        }
        String itemType = view.get(ITEM_TYPE_KEY, PersistentDataType.STRING);
        if (itemType == null) {
            return null;
        }
        return Garden.key("%s_%s".formatted(Garden.minimized(type.key()), itemType.toLowerCase(Locale.ROOT)));
    }

    static boolean isFruit(ItemStack item) {
        PersistentDataContainerView view = item.getPersistentDataContainer();
        return view.has(ITEM_TYPE_KEY) && view.get(ITEM_TYPE_KEY, PersistentDataType.STRING).equalsIgnoreCase(PlantItemType.FRUIT.name());
    }

    static boolean isSeeds(ItemStack item) {
        PersistentDataContainerView view = item.getPersistentDataContainer();
        return view.has(ITEM_TYPE_KEY) && view.get(ITEM_TYPE_KEY, PersistentDataType.STRING).equalsIgnoreCase(PlantItemType.SEEDS.name());
    }

    enum PlantItemType {
        FRUIT,
        SEEDS
    }
}
