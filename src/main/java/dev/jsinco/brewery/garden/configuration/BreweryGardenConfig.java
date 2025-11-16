package dev.jsinco.brewery.garden.configuration;

import com.google.common.collect.ImmutableMap;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.Header;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.Tag;

import java.util.List;
import java.util.Map;

@SuppressWarnings("FieldMayBeFinal")
@Header({
        "Welcome to the configuration file for the Garden addon!",
        "The settings below is all you're able to customize. If you'd like to request a feature",
        "or report a bug, join our Discord or make an issue on our Github! https://brewery.lumamc.net/",
        "To add a BreweryGarden item (plant or seeds), use 'garden:berry' or 'garden:berry_seeds'."
})
@Getter
public class BreweryGardenConfig extends OkaeriConfig {

    @Comment({"How likely it is for a seed to spawn from a broken 'validSeedDropBlocks' block.",
            "Use an integer from 1 to 100."})
    private int seedSpawnChance = 15;

    @Comment({"The integer which determines if a plant is fully grown (has a plant sprouted on it).",
            "A plant's growth stage has an 80% chance to increase by '1' every 5 minutes. Making '4' equal one full Minecraft day, or 20 minutes."})
    private int fullyGrown = 4;

    @Comment("A list of materials which a seed may drop from.")
    private List<Material> validSeedDropBlocks = List.of(Material.SHORT_GRASS, Material.TALL_GRASS);

    @Comment("A list of materials which a seed may be planted on.")
    private List<Material> plantableBlocks = List.of(Material.GRASS_BLOCK, Material.DIRT, Material.COARSE_DIRT, Material.PODZOL);

    @Comment("A list of worlds where the Garden is disabled.")
    private List<String> blacklistedWorlds = List.of("resource", "resource_nether");

    @Comment("A list of tags of materials generated through Garden that will drop when broken")
    private List<Tag<Material>> dropsDefaultItems = List.of(Tag.LOGS);

    @Comment("A map of tags of materials generated through Garden with custom drops")
    private Map<Tag<Material>, Material> dropOverride = new ImmutableMap.Builder<Tag<Material>, Material>()
            .put(Tag.WOODEN_STAIRS, Material.STICK)
            .put(Tag.WOODEN_TRAPDOORS, Material.STICK)
            .build();

    @Comment("A list of materials that can be used to farm fruits from garden plants.")
    private List<Material> shearTools = List.of(Material.SHEARS);

    @Comment("Allow breaking garden plants without shearing tools.")
    private boolean allowBreakWithoutShearTools = true;

    @Comment({
            "The minimum distance a water source block must be from a planted seed in order for it to grow and bloom.",
            "Set to less than 1 to disable water requirement."
    })
    private int minimumWaterDistance = 0;
}
