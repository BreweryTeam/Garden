package dev.jsinco.brewery.garden.integration;

import com.dre.brewery.recipe.PluginItem;
import dev.jsinco.brewery.garden.plant.Fruit;
import dev.jsinco.brewery.garden.plant.PlantItem;
import dev.jsinco.brewery.garden.plant.Seeds;
import org.bukkit.inventory.ItemStack;

public final class BreweryGardenIngredient extends PluginItem {
    @Override
    public boolean matches(ItemStack itemStack) {
        PlantItem plantItem = Fruit.getFruit(itemStack);
        if (plantItem == null) {
            plantItem = Seeds.getSeeds(itemStack);
        }
        if (plantItem != null) {
            return plantItem.simpleName().equalsIgnoreCase(this.getItemId());
        }
        return false;
    }
}