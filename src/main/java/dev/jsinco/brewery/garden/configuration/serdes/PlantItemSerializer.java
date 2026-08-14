package dev.jsinco.brewery.garden.configuration.serdes;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import dev.jsinco.brewery.garden.plant.item.FruitPlacementData;
import dev.jsinco.brewery.garden.plant.item.IntegrationBased;
import dev.jsinco.brewery.garden.plant.item.PlantItem;
import dev.jsinco.brewery.garden.plant.item.PlayerHeadBased;
import dev.jsinco.brewery.garden.plant.item.extra.ExtraItemData;
import dev.jsinco.brewery.garden.utility.CachedValue;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.awt.Color;
import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

@NullMarked
public class PlantItemSerializer implements TypeSerializer<PlantItem> {
    public static final UUID CONSTANT_UUID = UUID.fromString("f714a407-f7c9-425c-958d-c9914aeac05c");

    @Override
    public PlantItem deserialize(Type type, ConfigurationNode node) throws SerializationException {
        if (!node.isMap()) {
            throw new SerializationException("Expected a map type for plant item");
        }
        Component displayName = node.node("display-name").get(Component.class);
        if (displayName == null) {
            throw new SerializationException("Expected a display name");
        }
        List<Component> lore = node.node("lore").getList(Component.class, List.of());
        Color color = node.node("color").get(Color.class);
        ExtraItemData extraItemData = node.node("data").get(ExtraItemData.class, (Supplier<ExtraItemData>) () -> new ExtraItemData(List.of()));
        FruitPlacementData fruitPlacementData = node.node("fruit-placement").get(FruitPlacementData.class);
        if (node.hasChild("material")) {
            float placedScale = node.node("placed-scale").get(Float.class, 1F);
            String material = node.node("material").getString();
            if (material == null) {
                throw new SerializationException("Unknown material, expected a string");
            }
            if (fruitPlacementData == null) {
                fruitPlacementData = new FruitPlacementData.MaterialPlacement(material, 2, extraItemData::apply, placedScale);
            }
            return new IntegrationBased(material, displayName, lore, color, extraItemData::apply, fruitPlacementData);
        } else if (node.hasChild("head-texture-base64")) {
            String textureBase64 = node.node("head-texture-base64").getString();
            if (textureBase64 == null) {
                throw new SerializationException("Unknown texture, expected a string");
            }
            CachedValue<PlayerProfile> cachedProfile = new CachedValue<>(() -> {
                PlayerProfile profile = Bukkit.createProfile(CONSTANT_UUID);
                profile.getProperties().add(new ProfileProperty("textures", textureBase64));
                return profile;
            });
            if (fruitPlacementData == null) {
                fruitPlacementData = new FruitPlacementData.HeadPlacement(cachedProfile);
            }
            return new PlayerHeadBased(cachedProfile, displayName, lore, color, extraItemData::apply, fruitPlacementData);
        } else {
            throw new SerializationException("Expected either 'material' or 'head-texture-base64' key");
        }
    }

    @Override
    public void serialize(Type type, @Nullable PlantItem obj, ConfigurationNode node) throws SerializationException {
        throw new UnsupportedOperationException("Plant items can only be deserialized");
    }
}
