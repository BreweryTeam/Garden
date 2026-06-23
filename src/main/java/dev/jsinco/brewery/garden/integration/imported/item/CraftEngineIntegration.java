package dev.jsinco.brewery.garden.integration.imported.item;


import dev.jsinco.brewery.garden.api.integration.ItemIntegration;
import net.momirealms.craftengine.bukkit.api.event.CraftEngineReloadEvent;
import net.momirealms.craftengine.bukkit.item.BukkitItem;
import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class CraftEngineIntegration implements ItemIntegration, Listener {

    private final CompletableFuture<Void> initializedFuture = new CompletableFuture<>();
    private final Plugin garden;

    public CraftEngineIntegration(Plugin garden) {
        this.garden = garden;
    }

    @Override
    public String id() {
        return "craftengine";
    }

    @Override
    public void initialize() {
        Bukkit.getPluginManager().registerEvents(this, garden);
    }

    @Override
    public CompletableFuture<Void> validationReady() {
        return initializedFuture;
    }

    @Override
    public Optional<ItemStack> toBukkit(String identifier) {
        return Optional.ofNullable(
                        BukkitCraftEngine.instance()
                                .itemManager()
                                .createWrappedItem(Key.from(identifier), null))
                .map(BukkitItem::getBukkitItem);
    }

    @Override
    public boolean isValid(String identifier) {
        BukkitItemManager bukkitItemManager = BukkitCraftEngine.instance().itemManager();
        String[] split = identifier.split(":", 2);
        if (split.length == 2) {
            return !bukkitItemManager.customItemIdsByTag(Key.fromNamespaceAndPath(split[0], split[1])).isEmpty();
        }
        return !bukkitItemManager.customItemIdsByTag(Key.ce(identifier)).isEmpty();
    }

    @EventHandler
    public void onCraftEngineReload(CraftEngineReloadEvent ignored) {
        initializedFuture.completeAsync(() -> null);
    }
}
