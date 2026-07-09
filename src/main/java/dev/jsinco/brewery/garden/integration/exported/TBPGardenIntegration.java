package dev.jsinco.brewery.garden.integration.exported;

import dev.jsinco.brewery.api.ingredient.Ingredient;
import dev.jsinco.brewery.bukkit.api.TheBrewingProjectApi;
import dev.jsinco.brewery.bukkit.api.integration.IntegrationTypes;
import dev.jsinco.brewery.bukkit.api.integration.ItemIntegration;
import dev.jsinco.brewery.garden.Garden;
import dev.jsinco.brewery.garden.MutableGardenRegistry;
import dev.jsinco.brewery.garden.plant.PlantType;
import dev.jsinco.brewery.garden.plant.item.PlantItem;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class TBPGardenIntegration implements ItemIntegration {
    @Override
    public Optional<ItemStack> createItem(String key) {
        if (!key.contains("_seeds") && !key.contains("_fruit") || !Key.parseableValue(key)) {
            return Optional.empty();
        }
        String plantTypeKey = key.replaceAll("_seeds|_fruit", "");
        PlantType plantType = MutableGardenRegistry.PLANT_TYPE.get(Garden.key(plantTypeKey));
        if (plantType == null) {
            return Optional.empty();
        }
        if (key.contains("_seeds")) {
            return plantType.seedItem().item(PlantItem.PlantItemType.SEEDS, plantType);
        } else {
            return plantType.fruitItem().item(PlantItem.PlantItemType.FRUIT, plantType);
        }
    }

    @Override
    public boolean isIngredient(String key) {
        return get(key).isPresent();
    }

    @Override
    public @Nullable Component displayName(String key) {
        return get(key)
                .map(PlantItem::displayName)
                .orElse(null);
    }

    private Optional<PlantItem> get(String key) {
        if (!key.contains("_seeds") && !key.contains("_fruit") || !Key.parseableValue(key)) {
            return Optional.empty();
        }
        String plantTypeKey = key.replaceAll("_seeds|_fruit", "");
        return Optional.ofNullable(MutableGardenRegistry.PLANT_TYPE.get(Garden.key(plantTypeKey)))
                .map(type -> key.contains("_seeds") ? type.seedItem() : type.fruitItem());
    }

    @Override
    public @Nullable String getItemId(ItemStack itemStack) {
        Key plantItemKey = PlantItem.plantItemKey(itemStack);
        if (plantItemKey != null) {
            return Garden.minimized(plantItemKey.key());
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

    public static void loadIfPossible() {
        try {
            if (!Bukkit.getServicesManager().isProvidedFor(TheBrewingProjectApi.class)) {
                return;
            }
            RegisteredServiceProvider<TheBrewingProjectApi> provider = Bukkit.getServicesManager().getRegistration(TheBrewingProjectApi.class);
            if (provider != null) {
                provider.getProvider().getIntegrationManager().register(IntegrationTypes.ITEM, new TBPGardenIntegration());
            }
        } catch (NoClassDefFoundError ignored) {
        }
    }
}
