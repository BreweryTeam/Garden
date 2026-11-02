package dev.jsinco.brewery.garden.configuration.crafting;

import dev.jsinco.brewery.garden.integration.imported.IntegrationItemResolver;
import dev.jsinco.brewery.garden.utility.Logger;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ConfigSerializable
public class FurnaceCraftingRecipeConfig {

    CraftingOutputItem outputItem;

    FurnaceRecipeConfig furnaceRecipe;

    public List<Recipe> toRecipes(NamespacedKey key) {
        Optional<ItemStack> itemResult = outputItem.toItem(key);
        if (itemResult.isEmpty()) {
            return List.of();
        }
        List<Recipe> output = new ArrayList<>();
        if (furnaceRecipe != null) {
            IntegrationItemResolver.resolve(furnaceRecipe.inputItem)
                    .map(RecipeChoice.ExactChoice::new)
                    .map(choice -> new FurnaceRecipe(key,
                            itemResult.get(),
                            choice,
                            furnaceRecipe.experience,
                            furnaceRecipe.cookTime
                    )).ifPresentOrElse(output::add, () ->
                            Logger.logWarn("Unknown item '%s'".formatted(furnaceRecipe.inputItem))
                    );
        }
        return output;
    }
}
