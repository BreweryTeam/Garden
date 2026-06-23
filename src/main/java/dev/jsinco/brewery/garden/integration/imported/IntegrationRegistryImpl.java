package dev.jsinco.brewery.garden.integration.imported;

import dev.jsinco.brewery.garden.api.integration.IntegrationRegistry;
import dev.jsinco.brewery.garden.api.integration.ItemIntegration;
import dev.jsinco.brewery.garden.integration.imported.item.CraftEngineIntegration;
import dev.jsinco.brewery.garden.integration.imported.item.ItemsAdderIntegration;
import dev.jsinco.brewery.garden.integration.imported.item.MinecraftItemIntegration;
import dev.jsinco.brewery.garden.integration.imported.item.MmoItemsIntegration;
import dev.jsinco.brewery.garden.integration.imported.item.MythicIntegration;
import dev.jsinco.brewery.garden.integration.imported.item.NexoIntegration;
import dev.jsinco.brewery.garden.integration.imported.item.OraxenIntegration;
import dev.jsinco.brewery.garden.utility.ClassUtil;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class IntegrationRegistryImpl implements IntegrationRegistry {


    Map<String, ItemIntegration> itemIntegrations = new HashMap<>();


    public void registerDefaults(Plugin garden) {
        registerItemIntegration(new MinecraftItemIntegration());
        registerItemIntegration("net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine", () -> new CraftEngineIntegration(garden));
        registerItemIntegration("dev.lone.itemsadder.api.CustomStack", () -> new ItemsAdderIntegration(garden));
        registerItemIntegration("net.Indyuce.mmoitems.MMOItems", () -> new MmoItemsIntegration(garden));
        registerItemIntegration("io.lumine.mythic.bukkit.MythicBukkit", () -> new MythicIntegration(garden));
        registerItemIntegration("com.nexomc.nexo.api.NexoItems", () -> new NexoIntegration(garden));
        registerItemIntegration("io.th0rgal.oraxen.api.OraxenItems", () -> new OraxenIntegration(garden));
    }

    @Override
    public void registerItemIntegration(ItemIntegration itemIntegration) {
        itemIntegrations.put(itemIntegration.id(), itemIntegration);
    }

    public void registerItemIntegration(String classPredicate, Supplier<ItemIntegration> itemIntegrationSupplier) {
        if (ClassUtil.exists(classPredicate)) {
            ItemIntegration itemIntegration = itemIntegrationSupplier.get();
            itemIntegrations.put(itemIntegration.id(), itemIntegration);
        }
    }

    public Optional<ItemIntegration> itemIntegration(String id) {
        return Optional.ofNullable(itemIntegrations.get(id));
    }

    public Stream<ItemIntegration> itemIntegrations() {
        return itemIntegrations.values().stream();
    }
}
