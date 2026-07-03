package dev.jsinco.brewery.garden.configuration.serdes;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.Locale;

@NullMarked
public final class LocaleSerializer implements TypeSerializer<Locale> {

    public static final LocaleSerializer INSTANCE = new LocaleSerializer();

    @Override
    public Locale deserialize(Type type, ConfigurationNode node) throws SerializationException {
        String serialized = node.getString();
        if (serialized == null) {
            throw new SerializationException(node, type, "missing locale value");
        }
        return Locale.forLanguageTag(serialized);
    }

    @Override
    public void serialize(Type type, @Nullable Locale locale, ConfigurationNode node) throws SerializationException {
        if (locale == null) {
            node.raw(null);
            return;
        }
        node.set(locale.toLanguageTag());
    }
}