package dev.jsinco.brewery.garden.integration.imported.item;

import dev.jsinco.brewery.garden.api.integration.ItemIntegration;
import io.th0rgal.oraxen.api.OraxenItems;
import io.th0rgal.oraxen.api.events.OraxenItemsLoadedEvent;
import io.th0rgal.oraxen.items.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class OraxenIntegration implements ItemIntegration, Listener {

    private final CompletableFuture<Void> initializedFuture = new CompletableFuture<>();
    private final Plugin garden;

    public OraxenIntegration(Plugin garden) {
        this.garden = garden;
    }

    @Override
    public @NonNull CompletableFuture<Void> validationReady() {
        return initializedFuture;
    }

    @Override
    public boolean isValid(String identifier) {
        return OraxenItems.getItemById(identifier) != null;
    }

    @Override
    public Optional<ItemStack> toBukkit(String identifier) {
        return Optional.ofNullable(OraxenItems.getItemById(identifier))
                .map(ItemBuilder::build);
    }

    @Override
    public String id() {
        return "oraxen";
    }

    @Override
    public void initialize() {
        Bukkit.getPluginManager().registerEvents(this, garden);
    }

    @EventHandler
    public void onOraxenItemsLoaded(OraxenItemsLoadedEvent event) {
        initializedFuture.completeAsync(() -> null);
    }

}
