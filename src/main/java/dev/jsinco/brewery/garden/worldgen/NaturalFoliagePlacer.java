package dev.jsinco.brewery.garden.worldgen;

import dev.jsinco.brewery.garden.configuration.worldgen.PlantFeaturePlacementConfig;
import dev.jsinco.brewery.garden.configuration.worldgen.WorldGenConfig;
import dev.jsinco.brewery.garden.plant.PlantType;
import dev.wyck.biome.Biome;
import dev.wyck.biome.BiomeGenerationSettings;
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
import org.bukkit.util.BlockVector;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class NaturalFoliagePlacer {


    public static void registerFoliage(WorldGenConfig worldGenConfig) {
        if (!worldGenConfig.enabled) {
            return;
        }
        for (Map.Entry<NamespacedKey, List<PlantFeaturePlacementConfig>> entry : worldGenConfig.biomes.entrySet()) {
            Biome biome = Biome.reference(ResourceKey.of(entry.getKey().asString()));
            insertPlantFeature(biome, entry.getValue()
                    .stream()
                    .map(NaturalFoliagePlacer::newPlantFeature)
                    .toArray(PlacedFeature[]::new)
            );
        }
    }

    private static void insertPlantFeature(Biome biome, PlacedFeature... feature) {
        Biome wrapped = biome.wrap();
        BiomeGenerationSettings.Builder generationSettings = wrapped.generationSettings().toBuilder();
        Arrays.stream(feature).forEach(feature1 -> generationSettings
                .feature(Decoration.VEGETAL_DECORATION, feature1));
        wrapped.toBuilder()
                .generationSettings(generationSettings.build())
                .modify();
    }

    private static PlacedFeature newPlantFeature(PlantFeaturePlacementConfig config) {
        ResourceKey plantKey = ResourceKey.fromString(config.plant().key().asString());
        CustomFeature<PlantType> plantFeature = new PlantFeature(plantKey).register();
        return PlacedFeature.of(
                ConfiguredFeature.of(plantFeature, config.plant())
                        .toBuilder()
                        .resourceKey(plantKey)
                        .build(),
                PlacementModifier.rarityFilter(config.rarityModifier()),
                PlacementModifier.inSquare(),
                PlacementModifier.heightmap(HeightmapType.MOTION_BLOCKING),
                PlacementModifier.biomeFilter(),
                PlacementModifier.blockPredicateFilter(BlockPredicate.matchingBlocks()
                        .blocks(Material.GRASS_BLOCK, Material.MYCELIUM, Material.PODZOL)
                        .offset(new BlockVector(0, -1, 0))
                        .build()
                )
        );
    }
}
