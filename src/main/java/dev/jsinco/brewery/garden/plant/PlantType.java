package dev.jsinco.brewery.garden.plant;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.jsinco.brewery.garden.Garden;
import dev.jsinco.brewery.garden.structure.PlantStructure;
import dev.jsinco.brewery.garden.utility.FileUtil;
import dev.jsinco.brewery.garden.utility.TimeUtil;
import dev.thorinwasher.schem.Schematic;
import dev.thorinwasher.schem.SchematicReadException;
import dev.thorinwasher.schem.SchematicReader;
import net.kyori.adventure.key.Key;
import org.bukkit.*;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3d;
import org.joml.Vector3i;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Logger;


public record PlantType(String displayName, String skinBase64, int stages,
                        Map<String, List<Schematic>> structures, NamespacedKey key, int growthTime,
                        FruitPlacement fruitPlacement, Material seedMaterial) implements Keyed {

    // Forever constant UUID so that all plant ItemStacks are stackable. AKA. Don't change me!
    private static final UUID CONSTANT_UUID = UUID.fromString("f714a407-f7c9-425c-958d-c9914aeac05c");
    private static final List<Matrix3d> ALLOWED_TRANSFORMATIONS = compileAllowedTransformations();
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

    public PlayerProfile getPlayerProfile() {
        PlayerProfile profile = Bukkit.createProfile(CONSTANT_UUID);
        profile.getProperties().add(new ProfileProperty("textures", skinBase64));
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

    public static List<PlantType> readPlantTypes() {
        File plants = new File(Garden.getInstance().getDataFolder(), "plants");
        if (!plants.exists()) {
            return List.of();
        }
        ImmutableList.Builder<PlantType> builder = new ImmutableList.Builder<>();
        for (File file : plants.listFiles()) {
            readPlantType(file.getName().replace(".json", ""), FileUtil.readJsonFromFile(file)).ifPresent(builder::add);
        }
        return builder.build();
    }

    private static Optional<PlantType> readPlantType(String name, JsonElement jsonElement) {
        if (!Key.parseableValue(name)) {
            Logger.getLogger("Garden").warning("Could not read plant type, file name has to be a valid namespaced key");
            return Optional.empty();
        }
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        String displayName = jsonObject.get("display_name").getAsString();
        String skinBase64 = jsonObject.get("texture_base64").getAsString();
        int growthStages = jsonObject.get("growth_stages").getAsInt();
        int growthTime = TimeUtil.parseTime(jsonObject.get("approximate_growth_time").getAsString());
        FruitPlacement fruitPlacement = FruitPlacement.valueOf(jsonObject.get("fruit_placement").getAsString().toUpperCase(Locale.ROOT));
        try {
            return Optional.of(new PlantType(
                    displayName,
                    skinBase64,
                    growthStages,
                    findStructures(name, growthStages),
                    Garden.key(name),
                    growthTime,
                    fruitPlacement,
                    Registry.MATERIAL.get(NamespacedKey.fromString(jsonObject.get("seed_material").getAsString()))
            ));
        } catch (SchematicReadException e) {
            Logger.getLogger("Garden").warning("Could not read plant type, outdated version: " + name);
            e.printStackTrace();
            return Optional.empty();
        }
    }

    private static Map<String, List<Schematic>> findStructures(String name, int growthStages) {
        JsonArray tracks = FileUtil.readJsonFromFile("structures/" + name + "/tracks.json").getAsJsonArray();
        ImmutableMap.Builder<String, List<Schematic>> builder = ImmutableMap.builder();
        for (JsonElement typeJson : tracks) {
            String track = typeJson.getAsString();
            ImmutableList.Builder<Schematic> schematics = new ImmutableList.Builder<>();
            for (int i = 0; i < growthStages; i++) {
                try (InputStream inputStream = new FileInputStream(new File(Garden.getInstance().getDataFolder(), "/structures/" + name + "/" + track + "_" + i + ".schem"))) {
                    schematics.add(new SchematicReader().read(inputStream));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            builder.put(track, schematics.build());
        }
        return builder.build();
    }

    public String getRandomTrack() {
        List<String> tracks = new ArrayList<>(structures.keySet());
        return tracks.get(RANDOM.nextInt(tracks.size()));
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
}
