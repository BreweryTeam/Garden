package dev.jsinco.brewery.garden.commands.subcomands;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.jsinco.brewery.garden.tobacco.TobaccoPipe;
import dev.jsinco.brewery.garden.utility.MessageUtil;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GiveToolCommand {

    private static final SimpleCommandExceptionType ERROR_ILLEGAL_SENDER = new SimpleCommandExceptionType(
            MessageUtil.brigadierTranslatable("garden.command.illegal-sender")
    );


    public static ArgumentBuilder<CommandSourceStack, ?> command() {
        return Commands.literal("givetool")
                .then(Commands.literal("tobacco_pipe")
                        .executes(ctx -> {
                            giveSender(TobaccoPipe.newPipe(), ctx.getSource().getSender());
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
