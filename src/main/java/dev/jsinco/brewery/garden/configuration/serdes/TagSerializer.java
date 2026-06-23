package dev.jsinco.brewery.garden.configuration.serdes;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;

@NullMarked
public final class TagSerializer implements TypeSerializer<Tag<Material>> {

    public static final TagSerializer INSTANCE = new TagSerializer();

    private static final String TAG_PREFIX = "#";

    @Override
    public Tag<Material> deserialize(Type type, ConfigurationNode node) throws SerializationException {
        String serialized = node.getString();
        if (serialized == null) {
            throw new SerializationException(node, type, "missing tag value");
        }
        if (!serialized.startsWith(TAG_PREFIX)) {
            throw new SerializationException(node, type, "expected tag string to start with '#': " + serialized);
        }
        NamespacedKey key = NamespacedKey.fromString(serialized.substring(TAG_PREFIX.length()));
        if (key == null) {
            throw new SerializationException(node, type, "invalid tag key: " + serialized);
        }
        Tag<Material> tag = Bukkit.getTag(Tag.REGISTRY_BLOCKS, key, Material.class);
        if (tag == null) {
            throw new SerializationException(node, type, "unknown block tag: " + serialized);
        }
        return tag;
    }

    @Override
    public void serialize(Type type, @Nullable Tag<Material> tag, ConfigurationNode node) throws SerializationException {
        if (tag == null) {
            node.raw(null);
            return;
        }
        node.set(TAG_PREFIX + tag.getKey());
    }
}