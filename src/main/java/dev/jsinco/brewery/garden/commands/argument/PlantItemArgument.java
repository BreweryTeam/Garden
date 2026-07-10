package dev.jsinco.brewery.garden.commands.argument;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.jsinco.brewery.garden.Garden;
import dev.jsinco.brewery.garden.MutableGardenRegistry;
import dev.jsinco.brewery.garden.plant.item.PlantItem;
import dev.jsinco.brewery.garden.plant.item.PlantItemContainer;
import dev.jsinco.brewery.garden.utility.MessageUtil;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("UnstableApiUsage")
public class PlantItemArgument implements CustomArgumentType.Converted<PlantItemContainer, String> {
    public static final SimpleCommandExceptionType ERROR_INVALID_ITEM = new SimpleCommandExceptionType(
            MessageUtil.brigadierTranslatable("garden.command.invalid-item")
    );

    @Override
    public PlantItemContainer convert(String string) throws CommandSyntaxException {
        PlantItemContainer plantType = compileItems().get(string);
        if (plantType == null) {
            throw ERROR_INVALID_ITEM.create();
        }
        return plantType;
    }

    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.string();
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(@NonNull CommandContext<S> context, SuggestionsBuilder builder) {
        compileItems().keySet().stream()
                .filter(itemName -> itemName.startsWith(builder.getRemainingLowerCase()))
                .forEach(builder::suggest);
        return builder.buildFuture();
    }

    private Map<String, PlantItemContainer> compileItems() {
        Map<String, PlantItemContainer> output = new HashMap<>();
        for (var plant : MutableGardenRegistry.PLANT_TYPE.values()) {
            PlantItem seeds = plant.seedItem();
            output.put(Garden.minimized(PlantItem.key(plant, PlantItem.PlantItemType.SEEDS)), new PlantItemContainer(seeds, plant, PlantItem.PlantItemType.SEEDS));
            PlantItem fruit = plant.fruitItem();
            output.put(Garden.minimized(PlantItem.key(plant, PlantItem.PlantItemType.FRUIT)), new PlantItemContainer(fruit, plant, PlantItem.PlantItemType.FRUIT));
        }
        return output;
    }
}
