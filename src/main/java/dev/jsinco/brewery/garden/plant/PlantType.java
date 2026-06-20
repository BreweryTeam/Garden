package dev.jsinco.brewery.garden.plant;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.google.common.collect.ImmutableList;
import dev.jsinco.brewery.garden.structure.PlantStructure;
import dev.thorinwasher.schem.Schematic;
import lombok.experimental.Delegate;
import org.bukkit.*;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3d;
import org.joml.Vector3i;

import java.util.*;

public record PlantType(
    // Stuff that needs to be cached
    NamespacedKey key,
    String track,
    int growthTime,
    Map<String, List<Schematic>> structures,

    // Straight from the file
    @Delegate PlantTypeTemplate template
) implements Keyed {

    // Forever constant UUID so that all plant ItemStacks are stackable. AKA. Don't change me!
    private static final UUID CONSTANT_UUID = UUID.fromString("f714a407-f7c9-425c-958d-c9914aeac05c");
    private static final List<Matrix3d> ALLOWED_TRANSFORMATIONS = compileAllowedTransformations();
    private static final Random RANDOM = new Random();

    // Explicit accessor overrides

    public String track() {
        return this.track;
    }

    public int stages() {
        return template.stagesOrFallback(template.tracks(), this.track);
    }

    public int growthTime() {
        return this.growthTime;
    }

    public boolean bearFruits() {
        return Boolean.TRUE.equals(template.bearFruits());
    }

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

    public PlayerProfile getPlayerProfile() {
        PlayerProfile profile = Bukkit.createProfile(CONSTANT_UUID);
        profile.getProperties().add(new ProfileProperty("textures", template.textureBase64()));
        return profile;
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
    public @NotNull NamespacedKey getKey() {
        return key();
    }

    public Seeds newSeeds() {
        return new Seeds(
                this.key().getKey() + "_seeds",
                this
        );
    }

    public Fruit newFruit() {
        return new Fruit(
                this.key().getKey() + "_fruit",
                this
        );
    }

    public PlantStructure getStructure(Location origin, int age, String track, Matrix3d transformation) {
        Schematic schematic = structures.get(track).get(age);
        Vector3i size = schematic.size(transformation);
        Vector3i offset = new Vector3i(size.x() / 2, 0, size.z() / 2);
        return new PlantStructure(schematic, origin.getBlockX(), origin.getBlockY(), origin.getBlockZ(), transformation, origin.getWorld().getUID(), offset);
    }

    public static List<PlantType> readPlantTypes() {
        return PlantTypeTemplate.resolvePlantTypes()
            .stream()
            .map(it -> it.asPlantType().orElse(null))
            .filter(Objects::nonNull)
            .toList();
    }
}
