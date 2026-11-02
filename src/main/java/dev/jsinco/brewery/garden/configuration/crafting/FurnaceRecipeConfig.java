package dev.jsinco.brewery.garden.configuration.crafting;

import lombok.Builder;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public class FurnaceRecipeConfig {

    String inputItem;

    int cookTime;

    float experience;

}
