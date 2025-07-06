package dev.jsinco.brewery.garden.configuration;

import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;

public class TagSerializer implements ObjectSerializer<Tag<?>> {

    @Override
    public boolean supports(@NonNull Class<? super Tag<?>> type) {
        return Tag.class.isAssignableFrom(type);
    }

    @Override
    public void serialize(@NonNull Tag object, @NonNull SerializationData data, @NonNull GenericsDeclaration generics) {
        data.setValue("#" + object.key());
    }

    @Override
    public Tag<?> deserialize(@NonNull DeserializationData data, @NonNull GenericsDeclaration generics) {
        String serialized = data.getValue(String.class);
        if (!serialized.startsWith("#")) {
            throw new IllegalArgumentException("Expected string to start with '#'");
        }
        return Bukkit.getTag(Tag.REGISTRY_BLOCKS, NamespacedKey.fromString(serialized.substring(1)), Material.class);
    }
}
