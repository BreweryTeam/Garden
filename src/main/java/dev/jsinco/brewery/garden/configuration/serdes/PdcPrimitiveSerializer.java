package dev.jsinco.brewery.garden.configuration.serdes;

import dev.jsinco.brewery.garden.plant.item.extra.PdcPrimitive;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.Locale;

@NullMarked
public class PdcPrimitiveSerializer implements TypeSerializer<PdcPrimitive> {
    @Override
    public PdcPrimitive deserialize(Type type, ConfigurationNode node) throws SerializationException {
        String dataType = node.node("type").getString();
        if (dataType == null) {
            throw new SerializationException("Unspecified pdc type");
        }
        return switch (dataType.toLowerCase(Locale.ROOT)) {
            case "boolean" -> new PdcPrimitive.BooleanPrimitive(node.node("value").getBoolean());
            case "byte" -> new PdcPrimitive.BytePrimitive(node.node("value").get(Byte.class));
            case "short" -> new PdcPrimitive.ShortPrimitive(node.node("value").get(Short.class));
            case "int" -> new PdcPrimitive.IntegerPrimitive(node.node("value").getInt());
            case "long" -> new PdcPrimitive.LongPrimitive(node.node("value").getLong());
            case "float" -> new PdcPrimitive.FloatPrimitive(node.node("value").getFloat());
            case "double" -> new PdcPrimitive.DoublePrimitive(node.node("value").getDouble());
            case "string" -> new PdcPrimitive.StringPrimitive(node.node("value").getString());
            default -> throw new SerializationException("Unsupported PDC type '%s'".formatted(dataType));
        };
    }

    @Override
    public void serialize(Type type, @Nullable PdcPrimitive obj, ConfigurationNode node) throws SerializationException {
        throw new UnsupportedOperationException("Can only deserialize");
    }
}
