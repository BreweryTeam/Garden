package dev.jsinco.brewery.garden.integration.imported.item;

import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.api.events.NexoItemsLoadedEvent;
import com.nexomc.nexo.items.ItemBuilder;
import dev.jsinco.brewery.garden.api.integration.ItemIntegration;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class NexoIntegration implements ItemIntegration, Listener {

    private final CompletableFuture<Void> initializedFuture = new CompletableFuture<>();
    private final Plugin garden;

    public NexoIntegration(Plugin garden) {
        this.garden = garden;
    }

    @Override
    public @NonNull CompletableFuture<Void> validationReady() {
        return initializedFuture;
    }

    @Override
    public boolean isValid(String identifier) {
        return NexoItems.itemFromId(identifier) != null;
    }

    @Override
    public Optional<ItemStack> toBukkit(String material) {
        ItemBuilder itemBuilder = NexoItems.itemFromId(material);
        if (itemBuilder == null) {
            return Optional.empty();
        }
        return Optional.of(itemBuilder.build());
    }

    @Override
    public String id() {
        return "nexo";
    }

    @Override
    public void initialize() {
        Bukkit.getPluginManager().registerEvents(this, garden);
    }

    @EventHandler
    public void onNexoItemsLoaded(NexoItemsLoadedEvent event) {
        initializedFuture.completeAsync(() -> null);
    }
}
