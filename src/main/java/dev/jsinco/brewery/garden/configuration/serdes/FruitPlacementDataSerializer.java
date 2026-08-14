package dev.jsinco.brewery.garden.configuration.serdes;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import dev.jsinco.brewery.garden.plant.item.FruitPlacementData;
import dev.jsinco.brewery.garden.plant.item.extra.ExtraItemData;
import dev.jsinco.brewery.garden.utility.CachedValue;
import org.bukkit.Bukkit;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Supplier;

@NullMarked
public class FruitPlacementDataSerializer implements TypeSerializer<FruitPlacementData> {
    @Override
    public FruitPlacementData deserialize(Type type, ConfigurationNode node) throws SerializationException {
        if (node.hasChild("material")) {
            ExtraItemData extraItemData = node.node("data").get(ExtraItemData.class, (Supplier<ExtraItemData>) () -> new ExtraItemData(List.of()));
            int displayCount = node.node("item-displays").getInt(2);
            float placedScale = node.node("placed-scale").getFloat(1F);
            String material = node.node("material").getString();
            return new FruitPlacementData.MaterialPlacement(
                    material,
                    displayCount,
                    extraItemData::apply,
                    placedScale
            );
        } else if (node.hasChild("head-texture-base64")) {
            String textureBase64 = node.node("head-texture-base64").getString();
            if (textureBase64 == null) {
                throw new SerializationException("Unknown texture, expected a string");
            }
            CachedValue<PlayerProfile> cachedProfile = new CachedValue<>(() -> {
                PlayerProfile profile = Bukkit.createProfile(PlantItemSerializer.CONSTANT_UUID);
                profile.getProperties().add(new ProfileProperty("textures", textureBase64));
                return profile;
            });
            return new FruitPlacementData.HeadPlacement(cachedProfile);
        }
        throw new SerializationException("Empty fruit placement data!");
    }

    @Override
    public void serialize(Type type, @Nullable FruitPlacementData obj, ConfigurationNode node) throws SerializationException {
        throw new UnsupportedOperationException("Can only serialize");
    }
}
