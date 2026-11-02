package dev.jsinco.brewery.garden.configuration.crafting;

import dev.jsinco.brewery.garden.Garden;
import dev.jsinco.brewery.garden.MutableGardenRegistry;
import dev.jsinco.brewery.garden.configuration.serdes.ComponentSerializer;
import dev.jsinco.brewery.garden.configuration.serdes.ExtraItemDataSerializer;
import dev.jsinco.brewery.garden.configuration.serdes.NamespacedKeySerializer;
import dev.jsinco.brewery.garden.configuration.serdes.RegistryBackedValueSerializer;
import dev.jsinco.brewery.garden.plant.PlantType;
import dev.jsinco.brewery.garden.plant.item.extra.ExtraItemData;
import dev.jsinco.brewery.garden.utility.Lazy;
import dev.jsinco.brewery.garden.utility.Logger;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.nio.file.Path;
import java.util.Map;

@ConfigSerializable
public class CraftingConfig {

    Map<NamespacedKey, FurnaceCraftingRecipeConfig> furnace;

    public static final Lazy.Variable<CraftingConfig> CONFIG = Lazy.memorized(() -> {
        YamlConfigurationLoader loader = createLoader();
        CraftingConfig defaultConfig = new CraftingConfig();
        try {
            CommentedConfigurationNode root = loader.load();
            return root.get(CraftingConfig.class, defaultConfig);
        } catch (ConfigurateException e) {
            Logger.logErr(e.getMessage());
            return defaultConfig;
        }
    });

    public void registerRecipes() {
        furnace.forEach((key, recipe) ->
                recipe.toRecipes(key).forEach(Bukkit::addRecipe)
        );
    }

    public static CraftingConfig instance() {
        return CONFIG.get();
    }

    private static YamlConfigurationLoader createLoader() {
        Path file = Garden.getInstance().getDataPath().resolve("crafting.yml");
        return YamlConfigurationLoader.builder()
                .path(file)
                .nodeStyle(NodeStyle.BLOCK)
                .indent(2)
                .defaultOptions(opts ->
                        opts.serializers(builder ->
                                builder.register(NamespacedKey.class, new NamespacedKeySerializer())
                                        .register(PlantType.class, new RegistryBackedValueSerializer<>(MutableGardenRegistry.PLANT_TYPE::get, "plant type"))
                                        .register(ExtraItemData.class, new ExtraItemDataSerializer())
                                        .register(Component.class, new ComponentSerializer())
                        )
                )
                .build();
    }
}
