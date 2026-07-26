package dev.jsinco.brewery.garden.configuration.serdes;

import dev.jsinco.brewery.garden.utility.Logger;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;

@NullMarked
public class PotionEffectSerializer implements TypeSerializer<PotionEffect> {
    @Override
    public PotionEffect deserialize(Type type, ConfigurationNode node) throws SerializationException {
        String value = node.getString();
        if (value == null) {
            throw new SerializationException("Expected a present string value in '%s'".formatted(node.path().toString()));
        }
        String[] split = value.split(";");
        NamespacedKey potionEffectKey = NamespacedKey.fromString(split[0]);
        if (potionEffectKey == null) {
            throw new SerializationException("Invalid key format in '%s'".formatted(node.path().toString()));
        }
        PotionEffectType potionEffectType = Registry.MOB_EFFECT.get(potionEffectKey);
        if (potionEffectType == null) {
            throw new SerializationException("Unknown potion effect '%s' in '%s'".formatted(potionEffectKey, node.path().toString()));
        }
        int duration;
        if (split.length > 1) {
            try {
                duration = Integer.parseInt(split[1]);
            } catch (IllegalArgumentException e) {
                Logger.logWarn("Unknown duration '%s' in '%s' - Defaulting to 1 second".formatted(split[1], node.path().toString()));
                duration = 20;
            }
        } else {
            duration = 20;
        }
        int amplifier;
        if (split.length > 2) {
            try {
                amplifier = Integer.parseInt(split[2]);
            } catch (IllegalArgumentException e) {
                Logger.logWarn("Unknown amplifier '%s' in '%s' - Defaulting to level 0".formatted(split[2], node.path().toString()));
                amplifier = 0;
            }
        } else {
            amplifier = 0;
        }
        return new PotionEffect(potionEffectType, duration, amplifier);
    }

    @Override
    public void serialize(Type type, @Nullable PotionEffect obj, ConfigurationNode node) throws SerializationException {
        throw new UnsupportedOperationException("Can only deserialize");
    }
}
