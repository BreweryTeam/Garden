package dev.jsinco.brewery.garden.worldgen;

import dev.jsinco.brewery.garden.Garden;
import dev.jsinco.brewery.garden.MutableGardenRegistry;
import dev.jsinco.brewery.garden.configuration.GardenConfig;
import dev.jsinco.brewery.garden.configuration.worldgen.WorldGenConfig;
import dev.jsinco.brewery.garden.persist.PlantPdcType;
import dev.jsinco.brewery.garden.persist.StorageSolution;
import dev.jsinco.brewery.garden.plant.GardenPlant;
import dev.jsinco.brewery.garden.plant.PlantType;
import dev.jsinco.brewery.garden.structure.PlantStructure;
import dev.thorinwasher.schem.Schematic;
import dev.wyck.keys.ResourceKey;
import dev.wyck.worldgen.feature.custom.CustomFeature;
import dev.wyck.worldgen.feature.custom.PlacementContext;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.util.BlockVector;
import org.joml.Matrix3d;
import org.joml.Vector3i;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@NullMarked
public class PlantFeature extends CustomFeature<PlantType> {
    public PlantFeature(ResourceKey resourceKey) {
        super(() -> MutableGardenRegistry.PLANT_TYPE.values().stream().findFirst().orElseThrow(), resourceKey);
    }

    @Override
    public boolean place(PlacementContext<PlantType> context) {
        if (WorldGenConfig.instance().disabledInWorld(context.worldContext().bukkitName())) {
            return false;
        }
        BlockVector origin = context.origin();
        Material under = context.getBlock(new BlockVector(origin.getBlockX(), origin.getBlockY() - 1, origin.getBlockZ())).getMaterial();
        if (GardenConfig.instance().plantableBlocks().stream().noneMatch(under::equals)) {
            return false;
        }
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
        Vector3i size = structure.size(transformation);
        Vector3i offset = new Vector3i(size.x() / 2, 0, size.z() / 2);
        PlantStructure plantStructure = new PlantStructure(
                structure,
                origin.getBlockX(),
                origin.getBlockY(),
                origin.getBlockZ(),
                transformation,
                context.worldContext().uuid(),
                offset
        );
        List<BlockVector> pastedBlocks = new ArrayList<>();
        plantStructure.paste((vector, blockData) -> {
            context.setBlock(vector, blockData);
            pastedBlocks.add(vector);
        }, context::getBlock);
        if (pastedBlocks.isEmpty()) {
            return false;
        }
        GardenPlant gardenPlant = new GardenPlant(
                UUID.randomUUID(),
                plantType,
                plantStructure,
                track,
                age,
                0,
                GardenConfig.instance().storageSolution()
        );
        if (GardenConfig.instance().storageSolution() == StorageSolution.SQLITE) {
            Garden.getInstance().getGardenPlantDataType().insert(gardenPlant);
        } else {
            Bukkit.getWorld(context.worldContext().uuid())
                    .getChunkAtAsync(origin.getBlockX() >> 4, origin.getBlockZ() >> 4)
                    .thenAccept(chunk -> {
                        PersistentDataContainer pdc = chunk
                                .getPersistentDataContainer();
                        List<GardenPlant> plants;
                        try {
                            plants = pdc
                                    .get(PlantPdcType.PLANT_KEY, PlantPdcType.PLANT_LIST_TYPE);
                        } catch (IllegalArgumentException e) {
                            plants = null;
                        }
                        List<GardenPlant> gardenPlants;
                        if (plants == null) {
                            gardenPlants = new ArrayList<>();
                        } else {
                            gardenPlants = new ArrayList<>(plants);
                        }
                        gardenPlants.add(gardenPlant);
                        Garden.getGardenRegistry().registerPlant(gardenPlant);
                        pdc.set(PlantPdcType.PLANT_KEY, PlantPdcType.PLANT_LIST_TYPE, gardenPlants);
                    });
        }
        return true;
    }
}
