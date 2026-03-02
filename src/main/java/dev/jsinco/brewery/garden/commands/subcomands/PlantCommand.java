package dev.jsinco.brewery.garden.commands.subcomands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.jsinco.brewery.garden.Garden;
import dev.jsinco.brewery.garden.PlantRegistry;
import dev.jsinco.brewery.garden.plant.GardenPlant;
import dev.jsinco.brewery.garden.utility.MessageUtil;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.translation.Argument;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class PlantCommand {

    private static final SimpleCommandExceptionType ERROR_ILLEGAL_SENDER = new SimpleCommandExceptionType(
            MessageUtil.brigadierTranslatable("garden.command.illegal-sender")
    );

    private static final SimpleCommandExceptionType ERROR_NO_PLANT_FOUND = new SimpleCommandExceptionType(
            MessageUtil.brigadierTranslatable("garden.command.could-not-find-plant")
    );

    private static final SimpleCommandExceptionType FULLY_GROWN = new SimpleCommandExceptionType(
            MessageUtil.brigadierTranslatable("garden.command.fully-grown")
    );

    public static ArgumentBuilder<CommandSourceStack, ?> command() {
        return Commands.literal("plant")
                .then(infoCommand())
                .then(setAgeCommand())
                .then(growCommand())
                .then(growFruitsCommand())
                .requires(commandSourceStack -> commandSourceStack.getSender().hasPermission("garden.command.plant"));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> growFruitsCommand() {
        return Commands.literal("growfruits")
                .executes(commandContext -> {
                    if (!(commandContext.getSource().getSender() instanceof Player player)) {
                        throw ERROR_ILLEGAL_SENDER.create();
                    }
                    GardenPlant gardenPlant = getPlant(player, 32);
                    gardenPlant.bloom();
                    gardenPlant.placeFruits();
                    return 1;
                });
    }

    private static GardenPlant getPlant(Player player, int distance) throws CommandSyntaxException {
        Block block = player.getTargetBlockExact(distance);
        if (block == null) {
            throw ERROR_NO_PLANT_FOUND.create();
        }
        PlantRegistry gardenRegistry = Garden.getGardenRegistry();
        GardenPlant gardenPlant = gardenRegistry.getByLocation(block);
        if (gardenPlant == null) {
            throw ERROR_NO_PLANT_FOUND.create();
        }
        return gardenPlant;
    }

    private static ArgumentBuilder<CommandSourceStack, ?> infoCommand() {
        return Commands.literal("info")
                .executes(context -> {
                    if (!(context.getSource().getSender() instanceof Player player)) {
                        throw ERROR_ILLEGAL_SENDER.create();
                    }
                    sendPlantMessage(player, 32);
                    return 1;
                });
    }

    private static void sendPlantMessage(Player player, int maxDistance) throws CommandSyntaxException {
        player.sendMessage(Component.translatable("garden.command.found-plant", Argument.tagResolver(Placeholder.unparsed("plant", getPlant(player, maxDistance).toString()))));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> setAgeCommand() {
        return Commands.literal("setage")
                .then(Commands.argument("stage", IntegerArgumentType.integer(1, Garden.getInstance().getPluginConfiguration().getFullyGrown()))
                        .executes(context -> {
                            if (!(context.getSource().getSender() instanceof Player player)) {
                                throw ERROR_ILLEGAL_SENDER.create();
                            }
                            GardenPlant gardenPlant = getPlant(player, 32);
                            int stage = context.getArgument("stage", Integer.class);
                            try {
                                gardenPlant.setGrowthStage(stage, Garden.getGardenRegistry(), Garden.getInstance().getGardenPlantDataType());
                            } catch (ArrayIndexOutOfBoundsException e) {
                                throw new SimpleCommandExceptionType(
                                        MessageUtil.brigadierTranslatable("garden.command.unknown-age",
                                                Argument.numeric("age", stage),
                                                Argument.tagResolver(Placeholder.parsed("plant", gardenPlant.getType().displayName()))
                                        )
                                ).create();
                            }
                            return 1;
                        }));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> growCommand() {
        return Commands.literal("grow")
                .executes(context -> {
                    if (!(context.getSource().getSender() instanceof Player player)) {
                        throw ERROR_ILLEGAL_SENDER.create();
                    }
                    GardenPlant gardenPlant = getPlant(player, 32);
                    if (gardenPlant.isFullyGrown()) {
                        throw FULLY_GROWN.create();
                    }
                    try {
                        gardenPlant.incrementGrowthStage(1, Garden.getGardenRegistry(), Garden.getInstance().getGardenPlantDataType());
                    } catch (ArrayIndexOutOfBoundsException e) {
                        throw FULLY_GROWN.create();
                    }
                    return 1;
                });
    }
}
