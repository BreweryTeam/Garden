package dev.jsinco.brewery.garden.plant.item;

import com.destroystokyo.paper.profile.PlayerProfile;
import dev.jsinco.brewery.garden.integration.imported.IntegrationItemResolver;
import dev.jsinco.brewery.garden.plant.PlacedFruitDisplays;
import dev.jsinco.brewery.garden.plant.PlantType;
import dev.jsinco.brewery.garden.utility.CachedValue;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockType;
import org.bukkit.block.Skull;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import static dev.jsinco.brewery.garden.plant.item.PlantItem.PLANT_TYPE_KEY;

public sealed interface FruitPlacementData {

    Optional<PlacedFruitDisplays> place(Block relative, BlockFace facing, UUID owningPlant, PlantType plantType);

    record HeadPlacement(CachedValue<PlayerProfile> profile) implements FruitPlacementData {
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
    }

    record MaterialPlacement(String materialKey, int displayCount,
                             Consumer<ItemStack> extraModifications, float placedScale) implements FruitPlacementData {

        @Override
        public Optional<PlacedFruitDisplays> place(Block relative, BlockFace facing, UUID owningPlant, PlantType plantType) {
            return IntegrationItemResolver.resolve(materialKey)
                    .map(item -> {
                        extraModifications.accept(item);
                        return PlacedFruitDisplays.generate(item, facing, relative, plantType.getKey(), owningPlant, placedScale, displayCount);
                    });
        }
    }
}
