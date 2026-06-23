package dev.jsinco.brewery.garden.integration.imported.item;

import dev.jsinco.brewery.garden.api.integration.ItemIntegration;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.adapters.BukkitItemStack;
import io.lumine.mythic.core.items.MythicItem;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class MythicIntegration implements ItemIntegration, Listener {
    private final CompletableFuture<Void> initialized = new CompletableFuture<>();
    private final Plugin garden;

    public MythicIntegration(Plugin garden) {
        this.garden = garden;
    }

    private Optional<MythicItem> getMythicItem(String name) {
        MythicBukkit bukkit = MythicBukkit.inst();
        Optional<MythicItem> result = bukkit.getItemManager().getItem(name);
        if (result.isPresent()) return result;

        return bukkit.getItemManager().getItems().stream()
                .filter(item -> item.getInternalName().equalsIgnoreCase(name))
                .findFirst();

    }

    @Override
    public @NonNull CompletableFuture<Void> validationReady() {
        return initialized;
    }

    @Override
    public boolean isValid(String identifier) {
        return getMythicItem(identifier).isPresent();
    }


    @Override
    public Optional<ItemStack> toBukkit(String identifier) {
        return getMythicItem(identifier)
                .map(item -> item.generateItemStack(1))
                .map(BukkitItemStack.class::cast)
                .map(BukkitItemStack::getItemStack);
    }

    @Override
    public String id() {
        return "mythic";
    }

    @Override
    public void initialize() {
        Bukkit.getAsyncScheduler().runAtFixedRate(
                garden,
                this::checkEnabled,
                0,
                1,
                TimeUnit.SECONDS
        );
    }

    private void checkEnabled(ScheduledTask task) {
        io.lumine.mythic.api.items.ItemManager manager = MythicBukkit.inst().getItemManager();
        if (manager == null) {
            return;
        }
        Collection<MythicItem> items = manager.getItems();
        if (items != null && !items.isEmpty()) {
            initialized.complete(null);
            task.cancel();
        }
    }
}
