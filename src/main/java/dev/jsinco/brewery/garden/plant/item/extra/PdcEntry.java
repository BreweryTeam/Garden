package dev.jsinco.brewery.garden.plant.item.extra;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;

public record PdcEntry(NamespacedKey key, PdcPrimitive value) {

    public void apply(PersistentDataContainer itemStack) {
        value.apply(itemStack, key);
    }
}
