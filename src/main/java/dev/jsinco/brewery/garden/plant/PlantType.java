package dev.jsinco.brewery.garden.plant;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.jsinco.brewery.garden.GardenRegistry;
import dev.jsinco.brewery.garden.structure.PlantStructure;
import dev.jsinco.brewery.garden.utility.FileUtil;
import dev.jsinco.brewery.garden.utility.TimeUtil;
import dev.thorinwasher.schem.Schematic;
import dev.thorinwasher.schem.SchematicReader;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3d;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;


public record PlantType(String displayName, String skinBase64, int stages,
                        Map<String, List<Schematic>> structures, NamespacedKey key, int growthTime,
                        FruitPlacement fruitPlacement, Material seedMaterial) implements Keyed {

    // Forever constant UUID so that all plant ItemStacks are stackable. AKA. Don't change me!
    private static final UUID CONSTANT_UUID = UUID.fromString("f714a407-f7c9-425c-958d-c9914aeac05c");
    private static final NamespacedKey PLANT_TYPE_KEY = new NamespacedKey("garden", "plant");
    private static final List<Matrix3d> ALLOWED_TRANSFORMATIONS = compileAllowedTransformations();
    private static final Random RANDOM = new Random();

    private static List<Matrix3d> compileAllowedTransformations() {
        ImmutableList.Builder<Matrix3d> builder = new ImmutableList.Builder<>();
        Matrix3d identity = new Matrix3d();
        for (int i = 0; i < 4; i++) {
            builder.add(identity.rotateY(Math.PI / 2 * i, new Matrix3d()));
        }
        identity.negateZ();
        for (int i = 0; i < 4; i++) {
            builder.add(identity.rotateY(Math.PI / 2 * i, new Matrix3d()));
        }
        return builder.build();
    }

    public PlayerProfile getPlayerProfile() {
        PlayerProfile profile = Bukkit.createProfile(CONSTANT_UUID);
        profile.getProperties().add(new ProfileProperty("textures", skinBase64));
        return profile;
    }

    public PlantStructure newStructure(Location bottomLocation, int age, String track) {
        Schematic schematic = structures.getOrDefault(track, List.of()).get(age);
        return new PlantStructure(schematic, bottomLocation.getBlockX(), bottomLocation.getBlockY(), bottomLocation.getBlockZ(),
                ALLOWED_TRANSFORMATIONS.get(RANDOM.nextInt(ALLOWED_TRANSFORMATIONS.size())), bottomLocation.getWorld().getUID());
    }

    public static List<PlantType> readPlantTypes() {
        JsonArray items = FileUtil.readJson("/plants/items.json").getAsJsonArray();
        ImmutableList.Builder<PlantType> builder = new ImmutableList.Builder<>();
        for (JsonElement item : items) {
            String name = item.getAsString();
            builder.add(readPlantType(name, FileUtil.readJson("/plants/" + name + ".json")));
        }
        return builder.build();
    }

    private static PlantType readPlantType(String name, JsonElement jsonElement) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        String displayName = jsonObject.get("display_name").getAsString();
        String skinBase64 = jsonObject.get("texture_base64").getAsString();
        int growthStages = jsonObject.get("growth_stages").getAsInt();
        int growthTime = TimeUtil.parseTime(jsonObject.get("approximate_growth_time").getAsString());
        FruitPlacement fruitPlacement = FruitPlacement.valueOf(jsonObject.get("fruit_placement").getAsString().toUpperCase(Locale.ROOT));
        return new PlantType(
                displayName,
                skinBase64,
                growthStages,
                findStructures(name, growthStages),
                new NamespacedKey("garden", name),
                growthTime,
                fruitPlacement,
                Registry.MATERIAL.get(NamespacedKey.fromString(jsonObject.get("seed_material").getAsString()))
        );
    }

    private static Map<String, List<Schematic>> findStructures(String name, int growthStages) {
        JsonArray tracks = FileUtil.readJson("/structures/" + name + "/tracks.json").getAsJsonArray();
        ImmutableMap.Builder<String, List<Schematic>> builder = ImmutableMap.builder();
        for (JsonElement typeJson : tracks) {
            String track = typeJson.getAsString();
            ImmutableList.Builder<Schematic> schematics = new ImmutableList.Builder<>();
            for (int i = 0; i < growthStages; i++) {
                try (InputStream inputStream = FileUtil.class.getResourceAsStream("/structures/" + name + "/" + track + "_" + i + ".schem")) {
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
}
