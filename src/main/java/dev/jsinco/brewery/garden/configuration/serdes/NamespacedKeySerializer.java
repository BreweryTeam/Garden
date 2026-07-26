package dev.jsinco.brewery.garden.configuration.serdes;

import org.bukkit.NamespacedKey;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;

@NullMarked
public class NamespacedKeySerializer implements TypeSerializer<NamespacedKey> {
    @Override
    public NamespacedKey deserialize(Type type, ConfigurationNode node) throws SerializationException {
        String keyString = node.getString();
        if (keyString == null) {
            throw new SerializationException("Unknown key, expected a string type in '%s'".formatted(node.path().toString()));
        }
        NamespacedKey key = NamespacedKey.fromString(keyString);
        if (key == null) {
            throw new SerializationException("Invalid key format in '%s'".formatted(node.path().toString()));
        }
        return key;
    }

    @Override
    public void serialize(Type type, @Nullable NamespacedKey obj, ConfigurationNode node) throws SerializationException {
        if (obj == null) {
            return;
        }
        node.set(obj);
    }
}
