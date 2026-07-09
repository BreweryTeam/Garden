package dev.jsinco.brewery.garden.configuration.serdes;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;

@NullMarked
public class ComponentSerializer implements TypeSerializer<Component> {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    @Override
    public Component deserialize(Type type, ConfigurationNode node) throws SerializationException {
        String serialized = node.getString();
        if (serialized == null) {
            throw new SerializationException("Value should be a string scalar!");
        }
        return MINI_MESSAGE.deserialize(serialized);
    }

    @Override
    public void serialize(Type type, @Nullable Component obj, ConfigurationNode node) throws SerializationException {
        if (obj == null) {
            return;
        }
        node.set(MINI_MESSAGE.serialize(obj));
    }
}
