package dev.jsinco.brewery.garden.commands.argument;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.jsinco.brewery.garden.constants.GenericPlantType;
import dev.jsinco.brewery.garden.constants.PlantType;
import dev.jsinco.brewery.garden.constants.PlantTypeSeeds;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class GenericPlantTypeArgument implements CustomArgumentType.Converted<GenericPlantType, String> {
    private static final DynamicCommandExceptionType ERROR_ILLEGAL_ARGUMENT = new DynamicCommandExceptionType(invalidArgument ->
            MessageComponentSerializer.message().serialize(MiniMessage.miniMessage().deserialize("Illegal argument <argument>", Placeholder.unparsed("argument", invalidArgument.toString())))
    );
    private final Map<String, GenericPlantType> items = new HashMap<>();

    {
        for (var plant : PlantType.values()) {
            items.put(plant.name().toLowerCase(), plant);
        }
        for (var seed : PlantTypeSeeds.values()) {
            items.put(seed.name().toLowerCase(), seed);
        }
    }

    @Override
    public GenericPlantType convert(String string) throws CommandSyntaxException {
        GenericPlantType plantType = items.get(string);
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
