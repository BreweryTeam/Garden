package dev.jsinco.brewery.garden.configuration.worldgen;

import dev.jsinco.brewery.garden.Garden;
import dev.jsinco.brewery.garden.MutableGardenRegistry;
import dev.jsinco.brewery.garden.plant.PlantType;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.Optional;

@ConfigSerializable
public record PlantFeaturePlacementConfig(PlantType plant, int rarityModifier) {

    public static Optional<PlantFeaturePlacementConfig> of(String plantName, int rarityModifier) {
        PlantType plantType = MutableGardenRegistry.PLANT_TYPE.get(Garden.key(plantName));
        if (plantType == null) {
            return Optional.empty();
        }
        return Optional.of(new PlantFeaturePlacementConfig(
                plantType,
                rarityModifier
        ));
    }
}
