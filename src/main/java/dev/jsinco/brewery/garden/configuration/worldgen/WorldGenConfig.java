package dev.jsinco.brewery.garden.configuration.worldgen;

import com.google.common.collect.ImmutableMap;
import dev.jsinco.brewery.garden.Garden;
import dev.jsinco.brewery.garden.MutableGardenRegistry;
import dev.jsinco.brewery.garden.configuration.GardenConfig;
import dev.jsinco.brewery.garden.configuration.serdes.NamespacedKeySerializer;
import dev.jsinco.brewery.garden.configuration.serdes.RegistryBackedValueSerializer;
import dev.jsinco.brewery.garden.plant.PlantType;
import dev.jsinco.brewery.garden.utility.Lazy;
import org.bukkit.NamespacedKey;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@ConfigSerializable
public class WorldGenConfig {
    public boolean enabled = false;

    public Map<NamespacedKey, List<PlantFeaturePlacementConfig>> biomes = ImmutableMap.<NamespacedKey, List<PlantFeaturePlacementConfig>>builder()
            .put(NamespacedKey.minecraft("taiga"), List.of(
                    PlantFeaturePlacementConfig.of("blueberry", 1),
                    PlantFeaturePlacementConfig.of("cranberry", 1)
            ))
            .put(NamespacedKey.minecraft("forest"), List.of(
                    PlantFeaturePlacementConfig.of("apple", 3)
            ))
            .put(NamespacedKey.minecraft("flower_forest"), List.of(
                    PlantFeaturePlacementConfig.of("cherry", 3)
            ))
            .put(NamespacedKey.minecraft("savanna"), List.of(
                    PlantFeaturePlacementConfig.of("grape", 2),
                    PlantFeaturePlacementConfig.of("peach", 3)
            ))
            .put(NamespacedKey.minecraft("sparse_jungle"), List.of(
                    PlantFeaturePlacementConfig.of("lemon", 3),
                    PlantFeaturePlacementConfig.of("lime", 3),
                    PlantFeaturePlacementConfig.of("orange", 3)
            ))
            .put(NamespacedKey.minecraft("birch_forest"), List.of(
                    PlantFeaturePlacementConfig.of("raspberry", 1)
            ))
            .put(NamespacedKey.minecraft("old_growth_birch_forest"), List.of(
                    PlantFeaturePlacementConfig.of("strawberry", 1)
            ))
            .build();

    private static final String HEADER = """
            This is the world generation configuration file for Garden.
            For documentation, visit: https://docs.breweryteam.dev/docs/garden
            """;

    public static final Lazy.Variable<WorldGenConfig> CONFIG = Lazy.memorized(() -> {
        YamlConfigurationLoader loader = createLoader();
        try {
            CommentedConfigurationNode root = loader.load();
            WorldGenConfig loaded = root.get(WorldGenConfig.class, new WorldGenConfig());

            root.set(WorldGenConfig.class, loaded);
            loader.save(root);
            return loaded;
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
    });

    public static WorldGenConfig instance() {
        return CONFIG.get();
    }

    private static YamlConfigurationLoader createLoader() {
        Path file = Garden.getInstance().getDataPath().resolve("worldgen.yml");
        return YamlConfigurationLoader.builder()
                .path(file)
                .nodeStyle(NodeStyle.BLOCK)
                .indent(2)
                .defaultOptions(opts ->
                        opts.header(HEADER)
                                .serializers(builder ->
                                        builder.register(NamespacedKey.class, new NamespacedKeySerializer())
                                                .register(PlantType.class, new RegistryBackedValueSerializer<>(MutableGardenRegistry.PLANT_TYPE::get, "plant type"))
                                )
                )
                .build();
    }
}
