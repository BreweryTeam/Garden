package dev.jsinco.brewery.garden.commands;

import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.jsinco.brewery.garden.BreweryGarden;
import dev.jsinco.brewery.garden.commands.subcomands.GiveCommand;
import dev.jsinco.brewery.garden.commands.subcomands.PlantCommand;
import dev.jsinco.brewery.garden.utility.MessageUtil;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.registrar.ReloadableRegistrarEvent;

public class GardenCommand {


    public static LiteralCommandNode<CommandSourceStack> command() {
        return Commands.literal("garden")
                .then(GiveCommand.command())
                .then(PlantCommand.command())
                .then(Commands.literal("reload")
                        .executes(context -> {
                            BreweryGarden.getInstance().reload();
                            return 1;
                        })
                        .requires(commandSourceStack -> commandSourceStack.getSender().hasPermission("garden.command.reload"))
                )
                .executes((context) -> {
                    MessageUtil.sendMessage(context.getSource().getSender(), "Provide a subcommand.");
                    return 1;
                })
                .build();
    }

    public static void register(ReloadableRegistrarEvent<Commands> commandsReloadableRegistrarEvent) {
        commandsReloadableRegistrarEvent.registrar().register(command());
    }
}
