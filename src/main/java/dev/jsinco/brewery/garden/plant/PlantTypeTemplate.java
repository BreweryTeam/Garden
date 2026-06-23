package dev.jsinco.brewery.garden.plant;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import dev.jsinco.brewery.garden.Garden;
import dev.jsinco.brewery.garden.utility.TimeUtil;
import dev.thorinwasher.schem.Schematic;
import dev.thorinwasher.schem.SchematicReader;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.kyori.adventure.key.Key;
import org.bukkit.Material;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@Accessors(fluent = true)
@NullMarked
@ConfigSerializable
@SuppressWarnings({"unused", "NotNullFieldNotInitialized"})
public final class PlantTypeTemplate {

    private static final String SCHEM_EXTENSION = ".schem";
    private static final String RANDOM_TRACK = "*";
    private static final String PLANT_FILE = "plant.yml";
    private static final Path PLANTS_DIRECTORY = Garden.getInstance().getDataPath().resolve("plants");
    private static final Logger LOGGER = Logger.getLogger("Garden");
    private static final Pattern STAGE_NUMBER = Pattern.compile("(\\d+)(?=\\D*\\.schem$)");

    private transient String name;
    private transient File directory;

    private @Nullable String track;
    private String displayName;
    private String textureBase64;
    private @Nullable Integer growthStages; // TODO: remove me? if someone doesnt want a stage just remove the schem?
    private String approximateGrowthTime;
    private FruitPlacement fruitPlacement;
    private Material seedMaterial;
    private @Nullable Boolean bearFruits;


    public Optional<PlantType> asPlantType() {
        if (!Key.parseableValue(name)) {
            LOGGER.warning("Could not read plant type, file name has to be a valid namespaced key: " + name);
            return Optional.empty();
        }

        String resolvedTrack = resolveTrack();
        if (resolvedTrack == null) {
            LOGGER.warning("Could not read plant type, no usable track folder found: " + name);
            return Optional.empty();
        }

        // TODO: Could be optimized to only provide the track that is actually used

        Map<String, List<Schematic>> tracks = this.tracks();
        int stages = this.stagesOrFallback(tracks, resolvedTrack);
        return Optional.of(new PlantType(
            Garden.key(name),
            resolvedTrack,
            this.growthTime(),
            tracks,
            this
        ));
    }


    public int stagesOrFallback(Map<String, List<Schematic>> tracks, String resolvedTrack) {
        if (growthStages != null) return growthStages;
        return tracks.get(resolvedTrack).size();
    }

    public int growthTime() {
        return TimeUtil.parseTime(approximateGrowthTime);
    }

    private @Nullable String resolveTrack() {
        File[] children = directory.listFiles();
        if (children == null) {
            return null;
        }
        if (track != null && !RANDOM_TRACK.equals(track)) {
            File trackDirectory = new File(directory, track);
            return trackDirectory.isDirectory() ? track : null;
        }
        List<String> trackNames = new ArrayList<>();
        for (File child : children) {
            if (child.isDirectory()) {
                trackNames.add(child.getName());
            }
        }
        if (trackNames.isEmpty()) {
            return null;
        }
        return trackNames.get(ThreadLocalRandom.current().nextInt(trackNames.size()));
    }

    public Map<String, List<Schematic>> tracks() {
        File[] children = directory.listFiles();
        if (children == null) {
            return Map.of();
        }
        ImmutableMap.Builder<String, List<Schematic>> builder = ImmutableMap.builder();
        for (File trackDirectory : children) {
            if (!trackDirectory.isDirectory()) {
                continue;
            }
            String track = trackDirectory.getName();
            File[] schematicFiles = trackDirectory.listFiles();
            if (schematicFiles == null) {
                continue;
            }
            TreeMap<Integer, Schematic> ordered = new TreeMap<>();
            for (File schematicFile : schematicFiles) {
                if (!schematicFile.getName().endsWith(SCHEM_EXTENSION)) {
                    continue;
                }
                int stage = extractStageNumber(schematicFile.getName());
                if (stage < 0) {
                    continue;
                }
                try (InputStream inputStream = new FileInputStream(schematicFile)) {
                    ordered.put(stage, new SchematicReader().read(inputStream));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            builder.put(track, ImmutableList.copyOf(ordered.values()));
        }
        return builder.build();
    }


    private static int extractStageNumber(String fileName) {
        Matcher matcher = STAGE_NUMBER.matcher(fileName);
        if (!matcher.find()) {
            return -1;
        }
        return Integer.parseInt(matcher.group(1));
    }


    private static Optional<PlantTypeTemplate> fromDirectory(File plantDirectory) {
        File plantFile = new File(plantDirectory, PLANT_FILE);
        if (!plantFile.isFile()) {
            LOGGER.warning("Could not read plant type, missing '" + PLANT_FILE + "' in " + plantDirectory.getName());
            return Optional.empty();
        }

        YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
            .file(plantFile)
            .nodeStyle(NodeStyle.BLOCK)
            .indent(2)
            .build();
        try {
            CommentedConfigurationNode root = loader.load();
            PlantTypeTemplate template = root.get(PlantTypeTemplate.class);
            if (template == null) {
                LOGGER.warning("Could not read plant type, " + PLANT_FILE + " deserialized to null: " + plantDirectory.getName());
                return Optional.empty();
            }
            template.name = plantDirectory.getName();
            template.directory = plantDirectory;
            return Optional.of(template);
        } catch (ConfigurateException e) {
            LOGGER.log(Level.WARNING, "Could not read plant type, failed to load " + PLANT_FILE + ": " + plantDirectory.getName(), e);
            return Optional.empty();
        }
    }

    public static List<PlantTypeTemplate> resolvePlantTypes() {
        File[] directories = PLANTS_DIRECTORY.toFile().listFiles();
        if (directories == null) {
            return List.of();
        }
        ImmutableList.Builder<PlantTypeTemplate> builder = new ImmutableList.Builder<>();
        for (File directory : directories) {
            fromDirectory(directory).ifPresent(builder::add);
        }
        return builder.build();
    }
}