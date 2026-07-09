package dev.jsinco.brewery.garden.integration.imported;

import dev.jsinco.brewery.garden.Garden;
import dev.jsinco.brewery.garden.utility.Logger;
import net.kyori.adventure.key.Key;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;
import java.util.Optional;

public class IntegrationItemResolver {

    public static Optional<ItemStack> resolve(String key) {
        String[] split = key.split(":", 2);
        String namespace;
        String value;
        if (split.length == 1) {
            namespace = Key.MINECRAFT_NAMESPACE;
            value = split[0].toLowerCase(Locale.ROOT);
        } else {
            namespace = split[0].toLowerCase(Locale.ROOT);
            value = split[1].toLowerCase(Locale.ROOT);
        }
        return Garden.getInstance()
                .getIntegrationRegistry()
                .itemIntegration(namespace)
                .flatMap(itemIntegration -> itemIntegration.toBukkit(value));
    }

    public static void validate(String key, String context) {
        String[] split = key.split(":", 2);
        String namespace;
        String value;
        if (split.length == 1) {
            namespace = Key.MINECRAFT_NAMESPACE;
            value = split[0].toLowerCase(Locale.ROOT);
        } else {
            namespace = split[0].toLowerCase(Locale.ROOT);
            value = split[1].toLowerCase(Locale.ROOT);
        }
        Garden.getInstance()
                .getIntegrationRegistry()
                .itemIntegration(namespace)
                .ifPresentOrElse(itemIntegration -> itemIntegration.validationReady()
                                .thenAccept(ignored -> {
                                    if (!itemIntegration.isValid(value)) {
                                        Logger.logWarn("Unknown item '%s', in %s".formatted(key, context));
                                    }
                                }),
                        () -> Logger.logWarn("Unknown item '%s' in %s - Unknown namespace")
                );
    }
}
