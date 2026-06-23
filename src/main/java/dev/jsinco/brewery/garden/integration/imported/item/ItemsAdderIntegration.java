package dev.jsinco.brewery.garden.integration.imported.item;

import dev.jsinco.brewery.garden.api.integration.ItemIntegration;
import dev.lone.itemsadder.api.CustomStack;
import dev.lone.itemsadder.api.Events.ItemsAdderLoadDataEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ItemsAdderIntegration implements ItemIntegration, Listener {

    private final CompletableFuture<Void> initializedFuture = new CompletableFuture<>();
    private final Plugin garden;

    public ItemsAdderIntegration(Plugin garden) {
        this.garden = garden;
    }

    @Override
    public @NonNull CompletableFuture<Void> validationReady() {
        return initializedFuture;
    }

    @Override
    public boolean isValid(String identifier) {
        return CustomStack.isInRegistry(identifier);
    }

    @Override
    public Optional<ItemStack> toBukkit(String identifier) {
        return Optional.ofNullable(CustomStack.getInstance(identifier))
                .map(CustomStack::getItemStack);
    }

    @Override
    public String id() {
        return "itemsadder";
    }

    @Override
    public void initialize() {
        Bukkit.getPluginManager().registerEvents(this, garden);
    }


    @EventHandler
    public void onItemsAdderItemsLoad(ItemsAdderLoadDataEvent loadDataEvent) {
        initializedFuture.completeAsync(() -> null);
    }
}
