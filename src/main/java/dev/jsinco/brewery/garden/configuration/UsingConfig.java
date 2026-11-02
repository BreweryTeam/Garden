package dev.jsinco.brewery.garden.configuration;

import dev.jsinco.brewery.garden.Garden;
import dev.jsinco.brewery.garden.configuration.serdes.LocaleSerializer;
import dev.jsinco.brewery.garden.configuration.serdes.TagSerializer;
import dev.jsinco.brewery.garden.utility.Lazy;
import io.leangen.geantyref.GenericTypeReflector;
import lombok.Getter;
import org.bukkit.Tag;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

@ConfigSerializable
@Getter
public class UsingConfig {

    List<String> pipeUsable = List.of("dried_tobacco");

    private static final String HEADER = "This is the item use configuration file for Garden.\n" +
            "For documentation, visit: https://docs.breweryteam.dev/docs/garden";


    public static final Lazy.Variable<UsingConfig> CONFIG = Lazy.memorized(() -> {
        YamlConfigurationLoader loader = createLoader();
        try {
            CommentedConfigurationNode root = loader.load();
            UsingConfig loaded = root.get(UsingConfig.class, new UsingConfig());

            root.set(UsingConfig.class, loaded);
            loader.save(root);
            return loaded;
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
    });

    public static UsingConfig instance() {
        return CONFIG.get();
    }

    private static YamlConfigurationLoader createLoader() {
        Path file = Garden.getInstance().getDataPath().resolve("using.yml");
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
    }
}
