package dev.jsinco.brewery.garden.commands.subcomands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.jsinco.brewery.garden.commands.argument.PlantItemArgument;
import dev.jsinco.brewery.garden.plant.PlantItem;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GiveCommand {
    private static final SimpleCommandExceptionType ERROR_ILLEGAL_SENDER = new SimpleCommandExceptionType(() ->
            "You have to specify a player to use this command!"
    );

    public static ArgumentBuilder<CommandSourceStack, ?> command() {
        return Commands.literal("give")
                .then(Commands.argument("item", new PlantItemArgument())
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1, 64))
                                .then(Commands.argument("player", ArgumentTypes.player())
                                        .executes(context -> {
                                            ItemStack itemStack = context.getArgument("item", PlantItem.class).newItem(context.getArgument("amount", Integer.class));
                                            giveSender(itemStack, context.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(context.getSource()).getFirst());
                                            return 1;
                                        })
                                )
                                .executes(context -> {
                                    ItemStack itemStack = context.getArgument("item", PlantItem.class).newItem(context.getArgument("amount", Integer.class));
                                    giveSender(itemStack, context.getSource().getSender());
                                    return 1;
                                })
                        )
                        .executes(context -> {
                            ItemStack itemStack = context.getArgument("item", PlantItem.class).newItem(1);
                            giveSender(itemStack, context.getSource().getSender());
                            return 1;
                        })
                ).requires(commandSourceStack -> commandSourceStack.getSender().hasPermission("garden.command.give"));
    }

    private static void giveSender(ItemStack itemStack, CommandSender sender) throws CommandSyntaxException {
        if (!(sender instanceof Player player)) {
            throw ERROR_ILLEGAL_SENDER.create();
        }
        if (!player.getInventory().addItem(itemStack).isEmpty()) {
            player.getWorld().dropItem(player.getLocation(), itemStack);
        }
    }
}
