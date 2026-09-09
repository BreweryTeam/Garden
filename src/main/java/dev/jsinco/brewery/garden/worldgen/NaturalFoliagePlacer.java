package dev.jsinco.brewery.garden.worldgen;

import dev.jsinco.brewery.garden.MutableGardenRegistry;
import dev.jsinco.brewery.garden.plant.PlantType;
import dev.wyck.biome.Biome;
import dev.wyck.biome.BiomeGenerationSettings;
import dev.wyck.biome.Biomes;
import dev.wyck.keys.ResourceKey;
import dev.wyck.worldgen.Decoration;
import dev.wyck.worldgen.HeightmapType;
import dev.wyck.worldgen.blockpredicates.BlockPredicate;
import dev.wyck.worldgen.feature.ConfiguredFeature;
import dev.wyck.worldgen.feature.custom.CustomFeature;
import dev.wyck.worldgen.placement.PlacedFeature;
import dev.wyck.worldgen.placement.PlacementModifier;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.util.BlockVector;

public class NaturalFoliagePlacer {


    public static void registerFoliage() {
        insertPlantFeature(Biomes.TAIGA, newPlantFeature(ResourceKey.of("garden", "blueberry")));
        insertPlantFeature(Biomes.FOREST, newPlantFeature(ResourceKey.of("garden", "apple")));
        insertPlantFeature(Biomes.FLOWER_FOREST, newPlantFeature(ResourceKey.of("garden", "cherry")));
        insertPlantFeature(Biomes.SAVANNA, newPlantFeature(ResourceKey.of("garden", "grape")));
        insertPlantFeature(Biomes.TAIGA, newPlantFeature(ResourceKey.of("garden", "cranberry")));
        insertPlantFeature(Biomes.SPARSE_JUNGLE, newPlantFeature(ResourceKey.of("garden", "lemon")));
        insertPlantFeature(Biomes.SPARSE_JUNGLE, newPlantFeature(ResourceKey.of("garden", "lime")));
        insertPlantFeature(Biomes.SPARSE_JUNGLE, newPlantFeature(ResourceKey.of("garden", "orange")));
        insertPlantFeature(Biomes.SAVANNA, newPlantFeature(ResourceKey.of("garden", "peach")));
        insertPlantFeature(Biomes.BIRCH_FOREST, newPlantFeature(ResourceKey.of("garden", "raspberry")));
        insertPlantFeature(Biomes.OLD_GROWTH_BIRCH_FOREST, newPlantFeature(ResourceKey.of("garden", "strawberry")));
    }

    private static void insertPlantFeature(Biome biome, PlacedFeature feature) {
        Biome wrapped = biome.wrap();
        BiomeGenerationSettings generationSettings = wrapped.generationSettings().toBuilder()
                .feature(Decoration.VEGETAL_DECORATION, feature)
                .build();
        Biomes.TAIGA.wrap()
                .toBuilder()
                .generationSettings(generationSettings)
                .modify();
    }

    private static PlacedFeature newPlantFeature(ResourceKey plantKey) {
        CustomFeature<PlantType> plantFeature = new PlantFeature(plantKey).register();
        return PlacedFeature.of(
                ConfiguredFeature.of(plantFeature, MutableGardenRegistry.PLANT_TYPE.get(NamespacedKey.fromString(plantKey.asString())))
                        .toBuilder()
                        .resourceKey(plantKey)
                        .build(),
                PlacementModifier.rarityFilter(3),
                PlacementModifier.inSquare(),
                PlacementModifier.heightmap(HeightmapType.MOTION_BLOCKING),
                PlacementModifier.biomeFilter(),
                PlacementModifier.blockPredicateFilter(BlockPredicate.anyOf()
                        .predicate(BlockPredicate.matchingBlockTag()
                                .tag(Tag.DIRT)
                                .offset(new BlockVector(0, -1, 0))
                                .build()
                        ).predicate(BlockPredicate.matchingBlocks()
                                .blocks(Material.GRASS_BLOCK, Material.MYCELIUM, Material.PODZOL)
                                .offset(new BlockVector(0, -1, 0))
                                .build()
                        ).build()

                )
        );
    }
}
