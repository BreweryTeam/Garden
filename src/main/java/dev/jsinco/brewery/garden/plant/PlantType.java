package dev.jsinco.brewery.garden.plant;

import com.google.common.collect.ImmutableList;
import dev.jsinco.brewery.garden.plant.item.PlantItem;
import dev.jsinco.brewery.garden.structure.PlantStructure;
import dev.thorinwasher.schem.Schematic;
import org.bukkit.Keyed;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.joml.Matrix3d;
import org.joml.Vector3i;
import org.jspecify.annotations.NullMarked;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

@NullMarked
public record PlantType(
        NamespacedKey key,
        int growthTime,
        Map<String, List<Schematic>> structures,
        int maxStages,
        FruitPlacement fruitPlacement,
        PlantItem seedItem,
        PlantItem fruitItem,
        boolean bearFruits
) implements Keyed {
    public static final List<Matrix3d> ALLOWED_TRANSFORMATIONS = compileAllowedTransformations();
    private static final Random RANDOM = new Random();


    private static List<Matrix3d> compileAllowedTransformations() {
        ImmutableList.Builder<Matrix3d> builder = new ImmutableList.Builder<>();
        Matrix3d identity = new Matrix3d();
        for (int i = 0; i < 4; i++) {
            builder.add(round(identity.rotateY(Math.PI / 2 * i, new Matrix3d())));
        }
        identity.negateZ();
        for (int i = 0; i < 4; i++) {
            builder.add(round(identity.rotateY(Math.PI / 2 * i, new Matrix3d())));
        }
        return builder.build();
    }

    private static Matrix3d round(Matrix3d input) {
        double[] array = input.get(new double[9]);
        for (int i = 0; i < array.length; i++) {
            array[i] = Math.round(array[i]);
        }
        return new Matrix3d(array[0], array[1], array[2], array[3], array[4], array[5], array[6], array[7], array[8]);
    }

    public PlantStructure newStructure(Location bottomLocation, int age, String track) {
        Schematic schematic = structures.getOrDefault(track, List.of()).get(age);
        Matrix3d transformation = ALLOWED_TRANSFORMATIONS.get(RANDOM.nextInt(ALLOWED_TRANSFORMATIONS.size()));
        Vector3i size = schematic.size(transformation);
        Vector3i offset = new Vector3i(size.x() / 2, 0, size.z() / 2);

        return new PlantStructure(schematic, bottomLocation.getBlockX(), bottomLocation.getBlockY(), bottomLocation.getBlockZ(),
                transformation, bottomLocation.getWorld().getUID(), offset);
    }

    @Override
    public NamespacedKey getKey() {
        return key();
    }

    public Optional<PlantStructure> getStructure(Location origin, int age, String trackName, Matrix3d transformation) {
        List<Schematic> track = structures.get(trackName);
        if (track == null || track.size() <= age) {
            return Optional.empty();
        }
        Schematic schematic = track.get(age);
        Vector3i size = schematic.size(transformation);
        Vector3i offset = new Vector3i(size.x() / 2, 0, size.z() / 2);
        return Optional.of(
                new PlantStructure(schematic, origin.getBlockX(), origin.getBlockY(), origin.getBlockZ(), transformation, origin.getWorld().getUID(), offset)
        );
    }

    public static List<PlantType> readPlantTypes() {
        return PlantTypeTemplate.resolvePlantTypes()
                .stream()
                .map(it -> it.asPlantType().orElse(null))
                .filter(Objects::nonNull)
                .toList();
    }
}
