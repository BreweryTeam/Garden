package dev.jsinco.brewery.garden.commands.subcomands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.jsinco.brewery.garden.BreweryGarden;
import dev.jsinco.brewery.garden.PlantRegistry;
import dev.jsinco.brewery.garden.plant.GardenPlant;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;

public class GrowthStageCommand {

    private static final SimpleCommandExceptionType ERROR_ILLEGAL_SENDER = new SimpleCommandExceptionType(() ->
            "You have to be a player to use this command!"
    );

    private static final SimpleCommandExceptionType ERROR_NO_PLANT_FOUND = new SimpleCommandExceptionType(() ->
            "Could not find a garden plant!"
    );

    public static ArgumentBuilder<CommandSourceStack, ?> command() {
        return Commands.literal("setgrowthstage")
                .then(Commands.argument("stage", IntegerArgumentType.integer(1, BreweryGarden.getInstance().getPluginConfiguration().getFullyGrown()))
                        .executes(context -> {
                            if (!(context.getSource().getSender() instanceof Player player)) {
                                throw ERROR_ILLEGAL_SENDER.create();
                            }
                            PlantRegistry gardenRegistry = BreweryGarden.getGardenRegistry();
                            GardenPlant gardenPlant = gardenRegistry.getByLocation(player.getTargetBlockExact(30));

                            if (gardenPlant == null) {
                                throw ERROR_NO_PLANT_FOUND.create();
                            }

                            gardenPlant.setGrowthStage(context.getArgument("stage", Integer.class), BreweryGarden.getGardenRegistry());
                            return 1;
                        }))
                .requires(commandSourceStack -> commandSourceStack.getSender().hasPermission("garden.command.setgrowthstage"));
    }
}
