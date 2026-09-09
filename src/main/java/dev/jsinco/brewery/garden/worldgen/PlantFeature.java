package dev.jsinco.brewery.garden.worldgen;

import dev.jsinco.brewery.garden.Garden;
import dev.jsinco.brewery.garden.MutableGardenRegistry;
import dev.jsinco.brewery.garden.plant.GardenPlant;
import dev.jsinco.brewery.garden.plant.PlantType;
import dev.jsinco.brewery.garden.structure.PlantStructure;
import dev.thorinwasher.schem.Schematic;
import dev.wyck.keys.ResourceKey;
import dev.wyck.worldgen.feature.custom.CustomFeature;
import dev.wyck.worldgen.feature.custom.PlacementContext;
import org.bukkit.util.BlockVector;
import org.joml.Matrix3d;
import org.joml.Vector3i;
import org.jspecify.annotations.NullMarked;

import java.util.List;
import java.util.UUID;

@NullMarked
public class PlantFeature extends CustomFeature<PlantType> {
    public PlantFeature(ResourceKey resourceKey) {
        super(() -> MutableGardenRegistry.PLANT_TYPE.values().stream().findFirst().orElseThrow(), resourceKey);
    }

    @Override
    public boolean place(PlacementContext<PlantType> context) {
        PlantType plantType = context.config();
        List<String> structuresTracks = List.copyOf(plantType.structures().keySet());
        if (structuresTracks.isEmpty()) {
            return false;
        }
        String track = structuresTracks.get(context.random().nextInt(structuresTracks.size()));
        List<Schematic> stages = plantType.structures().get(track);
        if (stages.isEmpty()) {
            return false;
        }
        int age = context.random().nextInt(stages.size());
        Schematic structure = stages.get(age);
        Matrix3d transformation = PlantType.ALLOWED_TRANSFORMATIONS.get(context.random().nextInt(PlantType.ALLOWED_TRANSFORMATIONS.size()));
        BlockVector origin = context.origin();
        Vector3i offset = new Vector3i(structure.size().x() / 2, 0, structure.size().z() / 2);
        structure.apply(transformation, (vector3i, blockData) -> {
            if (blockData.getMaterial().isAir()) {
                return;
            }
            Vector3i pos = vector3i.sub(offset, new Vector3i()).add(origin.getBlockX(), origin.getBlockY(), origin.getBlockZ());
            context.setBlock(new BlockVector(pos.x, pos.y, pos.z), blockData);
        });
        PlantStructure plantStructure = new PlantStructure(
                structure,
                origin.getBlockX(),
                origin.getBlockY(),
                origin.getBlockZ(),
                transformation,
                context.worldContext().uuid(),
                offset
        );
        GardenPlant gardenPlant = new GardenPlant(
                UUID.randomUUID(),
                plantType,
                plantStructure,
                track,
                age,
                0
        );
        Garden.getGardenRegistry().registerPlant(gardenPlant);
        Garden.getInstance().getGardenPlantDataType().insert(gardenPlant);
        return true;
    }
}
