package dev.jsinco.brewery.garden.configuration.serdes;

import io.papermc.paper.datacomponent.item.consumable.ConsumeEffect;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.set.RegistrySet;
import org.bukkit.NamespacedKey;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;

@NullMarked
public class ConsumeEffectSerializer implements TypeSerializer<ConsumeEffect> {
    @Override
    public ConsumeEffect deserialize(Type type, ConfigurationNode node) throws SerializationException {
        return switch (node.node("type").getString()) {
            case "teleport" -> ConsumeEffect.teleportRandomlyEffect(node.node("diameter").getFloat());
            case "playSound" -> ConsumeEffect.playSoundConsumeEffect(node.node("soundKey").get(NamespacedKey.class));
            case "potionEffects" ->
                    ConsumeEffect.applyStatusEffects(node.node("effects").getList(PotionEffect.class), node.node("probability").getFloat(1F));
            case "removeEffects" ->
                    ConsumeEffect.removeEffects(RegistrySet.keySetFromValues(RegistryKey.MOB_EFFECT, node.node("effectTypes").getList(PotionEffectType.class)));
            case null, default ->
                    throw new SerializationException("Unknown or undefined type in '%s'".formatted(node.path().toString()));
        };
    }

    @Override
    public void serialize(Type type, @Nullable ConsumeEffect obj, ConfigurationNode node) throws SerializationException {
        throw new UnsupportedOperationException("Can only deserialize");
    }
}
