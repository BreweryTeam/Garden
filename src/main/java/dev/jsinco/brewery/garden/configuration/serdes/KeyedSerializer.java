package dev.jsinco.brewery.garden.configuration.serdes;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;

@NullMarked
public record KeyedSerializer<T extends Keyed>(RegistryKey<T> registryKey) implements TypeSerializer<T> {
    @Override
    public T deserialize(Type type, ConfigurationNode node) throws SerializationException {
        NamespacedKey namespacedKey = node.get(NamespacedKey.class);
        if (namespacedKey == null) {
            throw new SerializationException("Empty value in '%s'".formatted(node.path().toString()));
        }
        T t = RegistryAccess.registryAccess().getRegistry(registryKey).get(namespacedKey);
        if (t == null) {
            throw new SerializationException("Unknown key '%s' for type '%s' in %s".formatted(
                    namespacedKey.asMinimalString(), registryKey.key().asMinimalString(), node.path().toString()
            ));
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
