package dev.jsinco.brewery.garden.configuration.serdes;

import net.kyori.adventure.key.Key;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.function.Function;

@NullMarked
public record RegistryBackedValueSerializer<T extends Keyed>(Function<Key, @Nullable T> tLookup,
                                                             String name) implements TypeSerializer<T> {
    @Override
    public T deserialize(Type type, ConfigurationNode node) throws SerializationException {
        NamespacedKey namespacedKey = node.get(NamespacedKey.class);
        if (namespacedKey == null) {
            throw new SerializationException("Expected a valid key");
        }
        T t = tLookup.apply(namespacedKey);
        if (t == null) {
            throw new SerializationException("Unknown key '%s' for %s".formatted(namespacedKey, name));
        }
        return t;
    }

    @Override
    public void serialize(Type type, @Nullable T obj, ConfigurationNode node) throws SerializationException {
        if (obj == null) {
            return;
        }
        node.set(obj.getKey());
    }
}
