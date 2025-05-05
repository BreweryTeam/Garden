package dev.jsinco.brewery.garden.utility;

import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

public class MessageUtil {

    private MessageUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static void sendMessage(CommandSender sender, Component message) {
        sender.sendMessage(message);
    }

    public static void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(message);
    }
}
