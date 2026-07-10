package dev.jsinco.brewery.garden.configuration.serdes;

import net.kyori.adventure.text.format.NamedTextColor;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.awt.Color;
import java.lang.reflect.Type;
import java.util.HexFormat;

@NullMarked
public class ColorSerializer implements TypeSerializer<Color> {
    private static final HexFormat EXPLICIT_HEX_FORMAT = HexFormat.of().withPrefix("#");

    @Override
    public Color deserialize(Type type, ConfigurationNode node) throws SerializationException {
        String serializedColor = node.getString();
        if (serializedColor == null) {
            throw new SerializationException("Expected a string for color.");
        }
        if (serializedColor.startsWith("#")) {
            return new Color(HexFormat.fromHexDigits(serializedColor, 1, 7));
        }
        for (NamedTextColor textColor : NamedTextColor.NAMES.values()) {
            if (textColor.toString().equalsIgnoreCase(serializedColor)) {
                return new Color(textColor.value());
            }
        }
        return new Color(HexFormat.fromHexDigits(serializedColor));
    }

    @Override
    public void serialize(Type type, @Nullable Color obj, ConfigurationNode node) throws SerializationException {
        int rgb = obj.getRGB() & 0xFFFFFF;
        for (NamedTextColor textColor : NamedTextColor.NAMES.values()) {
            if ((textColor.value() & 0xFFFFFF) == rgb) {
                node.set(textColor.toString());
                return;
            }
        }
        node.set(EXPLICIT_HEX_FORMAT.toHexDigits(rgb, 6));
    }
}
