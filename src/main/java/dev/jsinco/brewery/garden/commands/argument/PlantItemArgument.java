package dev.jsinco.brewery.garden.commands.argument;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.jsinco.brewery.garden.GardenRegistry;
import dev.jsinco.brewery.garden.plant.Fruit;
import dev.jsinco.brewery.garden.plant.PlantItem;
import dev.jsinco.brewery.garden.plant.Seeds;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class PlantItemArgument implements CustomArgumentType.Converted<PlantItem, String> {
    private static final DynamicCommandExceptionType ERROR_ILLEGAL_ARGUMENT = new DynamicCommandExceptionType(invalidArgument ->
            MessageComponentSerializer.message().serialize(MiniMessage.miniMessage().deserialize("Illegal argument <argument>", Placeholder.unparsed("argument", invalidArgument.toString())))
    );
    private final Map<String, PlantItem> items = new HashMap<>();

    {
        for (var plant : GardenRegistry.PLANT_TYPE.values()) {
            Seeds seeds = plant.newSeeds();
            items.put(seeds.simpleName(), seeds);
            Fruit fruit = plant.newFruit();
            items.put(fruit.simpleName(), fruit);
        }
    }

    @Override
    public PlantItem convert(String string) throws CommandSyntaxException {
        PlantItem plantType = items.get(string);
        if (plantType == null) {
            throw ERROR_ILLEGAL_ARGUMENT.create(string);
        }
        return plantType;
    }

    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.string();
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(@NotNull CommandContext<S> context, SuggestionsBuilder builder) {
        items.keySet().stream()
                .filter(itemName -> itemName.startsWith(builder.getRemainingLowerCase()))
                .forEach(builder::suggest);
        return builder.buildFuture();
    }
}
