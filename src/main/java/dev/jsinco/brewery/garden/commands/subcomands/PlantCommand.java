package dev.jsinco.brewery.garden.commands.subcomands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.jsinco.brewery.garden.BreweryGarden;
import dev.jsinco.brewery.garden.PlantRegistry;
import dev.jsinco.brewery.garden.plant.GardenPlant;
import dev.jsinco.brewery.garden.utility.MessageUtil;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class PlantCommand {

    private static final SimpleCommandExceptionType ERROR_ILLEGAL_SENDER = new SimpleCommandExceptionType(() ->
            "You have to be a player to use this command!"
    );

    private static final SimpleCommandExceptionType ERROR_NO_PLANT_FOUND = new SimpleCommandExceptionType(() ->
            "Could not find a garden plant!"
    );

    public static ArgumentBuilder<CommandSourceStack, ?> command() {
        return Commands.literal("plant")
                .then(infoCommand())
                .then(setAgeCommand())
                .then(growFruitsCommand())
                .requires(commandSourceStack -> commandSourceStack.getSender().hasPermission("garden.command.plant"));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> growFruitsCommand() {
        return Commands.literal("grow")
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
        PlantRegistry gardenRegistry = BreweryGarden.getGardenRegistry();
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
        Block block = player.getTargetBlockExact(maxDistance);
        if (block == null) {
            throw ERROR_NO_PLANT_FOUND.create();
        }
        MessageUtil.sendMessage(player, "Found a GardenPlant: " + getPlant(player, 32));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> setAgeCommand() {
        return Commands.literal("setage")
                .then(Commands.argument("stage", IntegerArgumentType.integer(1, BreweryGarden.getInstance().getPluginConfiguration().getFullyGrown()))
                        .executes(context -> {
                            if (!(context.getSource().getSender() instanceof Player player)) {
                                throw ERROR_ILLEGAL_SENDER.create();
                            }
                            GardenPlant gardenPlant = getPlant(player, 32);
                            gardenPlant.setGrowthStage(context.getArgument("stage", Integer.class), BreweryGarden.getGardenRegistry(), BreweryGarden.getInstance().getGardenPlantDataType());
                            return 1;
                        }));
    }
}
