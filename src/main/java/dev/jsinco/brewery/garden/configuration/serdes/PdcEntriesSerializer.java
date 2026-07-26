package dev.jsinco.brewery.garden.configuration.serdes;

import dev.jsinco.brewery.garden.plant.item.extra.PdcEntries;
import dev.jsinco.brewery.garden.plant.item.extra.PdcEntry;
import dev.jsinco.brewery.garden.plant.item.extra.PdcPrimitive;
import dev.jsinco.brewery.garden.utility.Logger;
import org.bukkit.NamespacedKey;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@NullMarked
public class PdcEntriesSerializer implements TypeSerializer<PdcEntries> {
    @Override
    public PdcEntries deserialize(Type type, ConfigurationNode node) throws SerializationException {
        if (!node.isMap()) {
            throw new SerializationException("Expected a map type");
        }
        List<PdcEntry> entries = new ArrayList<>();
        Set<NamespacedKey> alreadyTaken = new HashSet<>();
        for (Map.Entry<Object, ? extends ConfigurationNode> entry : node.childrenMap().entrySet()) {
            NamespacedKey namespacedKey = NamespacedKey.fromString(entry.getKey().toString());
            if (namespacedKey == null) {
                Logger.logWarn("Invalid namespaced key '%s' in '%s' - Invalid format".formatted(entry.getKey().toString(), node.path().toString()));
                continue;
            }
            if (alreadyTaken.contains(namespacedKey)) {
                Logger.logWarn("Duplicate pdc key '%s' in '%s'".formatted(namespacedKey.asMinimalString(), node.path().toString()));
                continue;
            }
            alreadyTaken.add(namespacedKey);
            PdcPrimitive primitive = entry.getValue().get(PdcPrimitive.class);
            entries.add(new PdcEntry(namespacedKey, primitive));
        }
        return new PdcEntries(entries);
    }

    @Override
    public void serialize(Type type, @Nullable PdcEntries obj, ConfigurationNode node) throws SerializationException {
        throw new UnsupportedOperationException("Can only deserialize");
    }
}
