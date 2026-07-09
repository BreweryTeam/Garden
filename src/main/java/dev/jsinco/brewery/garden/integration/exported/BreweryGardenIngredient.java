package dev.jsinco.brewery.garden.integration.exported;

import com.dre.brewery.recipe.PluginItem;
import dev.jsinco.brewery.garden.Garden;
import dev.jsinco.brewery.garden.plant.item.PlantItem;
import net.kyori.adventure.key.Key;
import org.bukkit.inventory.ItemStack;

public final class BreweryGardenIngredient extends PluginItem {
    @Override
    public boolean matches(ItemStack itemStack) {
        Key plantItem = PlantItem.plantItemKey(itemStack);
        if (plantItem != null) {
            return Garden.minimized(plantItem).equalsIgnoreCase(this.getItemId())
                    || Garden.minimized(plantItem).replace("_fruit", "").equalsIgnoreCase(this.getItemId());
        }
        return false;
    }
}