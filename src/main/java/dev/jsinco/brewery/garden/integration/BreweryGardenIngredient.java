package dev.jsinco.brewery.garden.integration;

import com.dre.brewery.recipe.PluginItem;
import dev.jsinco.brewery.garden.constants.GenericPlantType;
import dev.jsinco.brewery.garden.constants.PlantType;
import dev.jsinco.brewery.garden.constants.PlantTypeSeeds;
import org.bukkit.inventory.ItemStack;

public final class BreweryGardenIngredient extends PluginItem {
    @Override
    public boolean matches(ItemStack itemStack) {
        GenericPlantType genericPlantType = PlantType.getPlantType(itemStack);
        if (genericPlantType == null) {
            genericPlantType = PlantTypeSeeds.getPlantSeedsType(itemStack);
        }

        if (genericPlantType != null) {
            return genericPlantType.name().equalsIgnoreCase(this.getItemId());
        }
        return false;
    }
}