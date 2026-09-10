package dev.jsinco.brewery.garden.configuration.worldgen;

import com.google.common.base.Preconditions;
import dev.jsinco.brewery.garden.Garden;
import dev.jsinco.brewery.garden.MutableGardenRegistry;
import dev.jsinco.brewery.garden.plant.PlantType;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public record PlantFeaturePlacementConfig(PlantType plant, int rarityModifier) {

    public static PlantFeaturePlacementConfig of(String plantName, int rarityModifier) {
        PlantType plantType = MutableGardenRegistry.PLANT_TYPE.get(Garden.key(plantName));
        Preconditions.checkArgument(plantType != null, "Unknown plant type: " + plantName);
        return new PlantFeaturePlacementConfig(
                plantType,
                rarityModifier
        );
    }
}
