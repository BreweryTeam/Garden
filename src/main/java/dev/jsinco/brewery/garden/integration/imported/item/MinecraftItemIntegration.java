package dev.jsinco.brewery.garden.integration.imported.item;

import dev.jsinco.brewery.garden.api.integration.ItemIntegration;
import net.kyori.adventure.key.Key;
import org.bukkit.Registry;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class MinecraftItemIntegration implements ItemIntegration {

    @Override
    public Optional<ItemStack> toBukkit(String identifier) {
        if (!Key.parseable(identifier)) {
            return Optional.empty();
        }
        Key key = Key.key(identifier);
        return Optional.ofNullable(Registry.ITEM.get(key))
                .map(ItemType::createItemStack);
    }

    @Override
    public String id() {
        return "minecraft";
    }

    @Override
    public void initialize() {
        // NO-OP
    }

    @Override
    public CompletableFuture<Void> validationReady() {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public boolean isValid(String identifier) {
        if (!Key.parseable(identifier)) {
            return false;
        }
        Key key = Key.key(identifier);
        return Optional.ofNullable(Registry.ITEM.get(key))
                .isPresent();
    }
}
