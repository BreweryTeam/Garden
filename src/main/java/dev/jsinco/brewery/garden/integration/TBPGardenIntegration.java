package dev.jsinco.brewery.garden.integration;

import dev.jsinco.brewery.api.ingredient.Ingredient;
import dev.jsinco.brewery.bukkit.api.integration.ItemIntegration;
import dev.jsinco.brewery.garden.Garden;
import dev.jsinco.brewery.garden.GardenRegistry;
import dev.jsinco.brewery.garden.plant.Fruit;
import dev.jsinco.brewery.garden.plant.PlantItem;
import dev.jsinco.brewery.garden.plant.PlantType;
import dev.jsinco.brewery.garden.plant.Seeds;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class TBPGardenIntegration implements ItemIntegration {
    @Override
    public Optional<ItemStack> createItem(String key) {
        return get(key)
                .map(PlantItem::newItem);
    }

    @Override
    public boolean isIngredient(String key) {
        return get(key).isPresent();
    }

    @Override
    public @Nullable Component displayName(String key) {
        return get(key)
                .map(PlantItem::plantType)
                .map(PlantType::displayName)
                .map(MiniMessage.miniMessage()::deserialize)
                .map(component -> component.color(null))
                .orElse(null);
    }

    private Optional<PlantItem> get(String key) {
        if (!key.contains("_seeds") && !key.contains("_fruit") || !Key.parseableValue(key)) {
            return Optional.empty();
        }
        String plantTypeKey = key.replaceAll("_seeds|_fruit", "");
        return Optional.ofNullable(Garden.key(plantTypeKey))
                .flatMap(plant -> Optional.ofNullable(GardenRegistry.PLANT_TYPE.get(plant)))
                .map(type -> key.contains("_seeds") ? type.newSeeds() : type.newFruit());
    }

    @Override
    public @Nullable String getItemId(ItemStack itemStack) {
        if (Fruit.isFruit(itemStack)) {
            return Fruit.getFruit(itemStack).simpleName();
        }
        if (Seeds.isSeeds(itemStack)) {
            return Seeds.getSeeds(itemStack).simpleName();
        }
        return null;
    }

    @Override
    public CompletableFuture<Void> initialized() {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public String getId() {
        return "garden";
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public CompletableFuture<Optional<Ingredient>> createIngredient(String id) {
        if (!id.contains("_fruit") && !id.contains("_seeds")) {
            id = id + "_fruit";
        }
        return ItemIntegration.super.createIngredient(id);
    }
}
