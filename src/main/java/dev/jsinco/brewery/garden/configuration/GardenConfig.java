package dev.jsinco.brewery.garden.configuration;

import com.google.common.collect.ImmutableMap;
import dev.jsinco.brewery.garden.Garden;
import dev.jsinco.brewery.garden.configuration.serdes.LocaleSerializer;
import dev.jsinco.brewery.garden.configuration.serdes.TagSerializer;
import dev.jsinco.brewery.garden.utility.Lazy;
import io.leangen.geantyref.GenericTypeReflector;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Getter
@Accessors(fluent = true)
@ConfigSerializable
@SuppressWarnings({"FieldMayBeFinal"})
public final class GardenConfig {

    @Comment("The language to use for garden")
    private Locale language = Locale.US;

    @Comment("Whether some translations should be determined by the client")
    private boolean clientSidedTranslations = false;

    @Comment(
        "How likely it is for a seed to spawn from a broken 'valid-seed-drop-blocks' block.\n" +
        "Use an integer from 1 to 100."
    )
    private int seedSpawnChance = 15;

    @Comment("A list of materials which a seed may drop from.")
    private List<Material> validSeedDropBlocks = List.of(Material.SHORT_GRASS, Material.TALL_GRASS);

    @Comment("A list of materials which a seed may be planted on.")
    private List<Material> plantableBlocks = List.of(Material.GRASS_BLOCK, Material.DIRT, Material.COARSE_DIRT, Material.PODZOL, Material.MYCELIUM, Material.ROOTED_DIRT);

    @Comment("A list of worlds where the BreweryGarden addon is disabled.")
    private List<String> blacklistedWorlds = List.of("resource", "resource_nether");

    @Comment("A list of tags of materials generated through Garden that will drop when broken")
    private List<Tag<Material>> dropsDefaultItems = List.of(Tag.LOGS);

    @Comment("A map of tags of materials generated through Garden with custom drops")
    private Map<Tag<Material>, Material> dropOverride = new ImmutableMap.Builder<Tag<Material>, Material>()
        .put(Tag.WOODEN_STAIRS, Material.STICK)
        .put(Tag.WOODEN_TRAPDOORS, Material.STICK)
        .build();

    @Comment("Let fruits fall to the ground as items after a while")
    private boolean fallFruit = true;

    @Comment("Whether players can use bone meal to advance a garden plants growth")
    private boolean bonemealGrowth = true;

    @Comment("The percent chance that using bone meal on a garden plant advances its growth by one stage (0-100)")
    private int bonemealChance = 25;


    private static final String HEADER = "This is the global configuration file for Garden.\n" +
        "For documentation, visit: https://docs.breweryteam.dev/docs/garden";

    private static final Lazy<YamlConfigurationLoader> LOADER = Lazy.of(() -> {
        Path file = Garden.getInstance().getDataPath().resolve("config.yml");
        return YamlConfigurationLoader.builder()
            .path(file)
            .nodeStyle(NodeStyle.BLOCK)
            .indent(2)
            .defaultOptions(opts ->
                opts.header(HEADER)
                    .serializers(serdes -> serdes
                        .register(typeToken -> Tag.class.isAssignableFrom(GenericTypeReflector.erase(typeToken)), TagSerializer.INSTANCE)
                        .register(Locale.class, LocaleSerializer.INSTANCE)
                    )
            )
            .build();
    });

    public static final Lazy.Memorized<GardenConfig> MEMORIZED = Lazy.memorized(() -> {
        YamlConfigurationLoader loader = LOADER.get();
        try {
            CommentedConfigurationNode root = loader.load();
            GardenConfig loaded = root.get(GardenConfig.class, new GardenConfig());

            root.set(GardenConfig.class, loaded);
            loader.save(root);
            return loaded;
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
    });

    public static GardenConfig instance() {
        return MEMORIZED.get();
    }
}
