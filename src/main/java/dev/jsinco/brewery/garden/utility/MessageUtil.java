package dev.jsinco.brewery.garden.utility;

import com.mojang.brigadier.Message;
import dev.jsinco.brewery.garden.Garden;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.command.CommandSender;

public class MessageUtil {

    private MessageUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static Message brigadierTranslatable(String translationKey, ComponentLike... arguments) {
        return MessageComponentSerializer.message().serialize(
                GlobalTranslator.render(Component.translatable(translationKey, arguments), Garden.getInstance().getPluginConfiguration().getLanguage())
        );
    }
}
