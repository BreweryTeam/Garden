package dev.jsinco.brewery.garden.commands.subcomands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.jsinco.brewery.garden.BreweryGarden;
import dev.jsinco.brewery.garden.GardenRegistry;
import dev.jsinco.brewery.garden.objects.GardenPlant;
import dev.jsinco.brewery.garden.utility.MessageUtil;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;

public class IsPlantCommand {

    private static final SimpleCommandExceptionType ERROR_ILLEGAL_SENDER = new SimpleCommandExceptionType(() ->
            "You have to be a player to use this command!"
    );

    public static ArgumentBuilder<CommandSourceStack, ?> command() {
        return Commands.literal("isplant")
                .then(Commands.argument("distance", IntegerArgumentType.integer(1, 64))
                        .executes(context -> {
                            if (!(context.getSource().getSender() instanceof Player player)) {
                                throw ERROR_ILLEGAL_SENDER.create();
                            }
                            sendPlantMessage(player, context.getArgument("distance", Integer.class));
                            return 1;
                        })
                )
                .executes(context -> {
                    if (!(context.getSource().getSender() instanceof Player player)) {
                        throw ERROR_ILLEGAL_SENDER.create();
                    }
                    sendPlantMessage(player, 32);
                    return 1;
                })
                .requires(commandSourceStack -> commandSourceStack.getSender().hasPermission("garden.command.isplant"));
    }

    private static void sendPlantMessage(Player player, int maxDistance) {
        GardenRegistry gardenRegistry = BreweryGarden.getGardenRegistry();
        GardenPlant gardenPlant = gardenRegistry.getByLocation(player.getTargetBlockExact(maxDistance));
        if (gardenPlant != null) {
            MessageUtil.sendMessage(player, "Found a GardenPlant: " + gardenPlant);
        } else {
            MessageUtil.sendMessage(player, "No GardenPlant found.");
        }
    }
}
