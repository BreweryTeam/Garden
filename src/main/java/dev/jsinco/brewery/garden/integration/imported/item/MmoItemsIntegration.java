package dev.jsinco.brewery.garden.integration.imported.item;

import dev.jsinco.brewery.garden.api.integration.ItemIntegration;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.event.MMOItemsReloadEvent;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class MmoItemsIntegration implements ItemIntegration, Listener {
    private final CompletableFuture<Void> initialized = new CompletableFuture<>();
    private final Plugin garden;

    public MmoItemsIntegration(Plugin garden) {
        this.garden = garden;
    }

    @Override
    public @NonNull CompletableFuture<Void> validationReady() {
        return initialized;
    }

    @Override
    public boolean isValid(String identifier) {
        String[] split = identifier.split(":", 2);
        if (split.length != 2) {
            return false;
        }
        return MMOItems.plugin.getTemplates().getTemplate(Type.get(split[0]), split[1]) != null;
    }

    @Override
    public Optional<ItemStack> toBukkit(String identifier) {
        String[] split = identifier.split(":", 2);
        if (split.length != 2) {
            return Optional.empty();
        }
        return Optional.ofNullable(MMOItems.plugin.getTemplates().getTemplate(
                        Type.get(split[0]),
                        split[1])
                )
                .map(MMOItemTemplate::newBuilder)
                .map(MMOItemBuilder::build)
                .map(MMOItem::newBuilder)
                .map(ItemStackBuilder::build);
    }

    @Override
    public String id() {
        return "mmoitems";
    }

    @Override
    public void initialize() {
        Bukkit.getPluginManager().registerEvents(this, garden);
    }

    @EventHandler
    public void onMmoItemsReload(MMOItemsReloadEvent event) {
        initialized.completeAsync(() -> null);
    }
}
