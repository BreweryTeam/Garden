package dev.jsinco.brewery.garden.configuration.serdes;

import dev.jsinco.brewery.garden.plant.item.extra.Consumption;
import dev.jsinco.brewery.garden.plant.item.extra.ExtraItemData;
import dev.jsinco.brewery.garden.plant.item.extra.PdcEntries;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.CustomModelData;
import org.bukkit.Color;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@NullMarked
public class ExtraItemDataSerializer implements TypeSerializer<ExtraItemData> {
    @Override
    public ExtraItemData deserialize(Type type, ConfigurationNode node) throws SerializationException {
        if (!node.isMap()) {
            throw new SerializationException("Unsupported type, expected a map in '%s'".formatted(node.path().toString()));
        }
        List<Consumer<ItemStack>> consumerList = new ArrayList<>();
        for (Map.Entry<Object, ? extends ConfigurationNode> entry : node.childrenMap().entrySet()) {
            String key = entry.getKey().toString();
            switch (key) {
                case "customModelData" -> {
                    final List<Float> customModelDataFloats = entry.getValue().node("floats").getList(Float.class, List.of());
                    final List<Color> customModelDataColors = entry.getValue().node("colors").getList(Color.class, List.of());
                    final List<Boolean> customModelDataFlags = entry.getValue().node("flags").getList(Boolean.class, List.of());
                    CustomModelData customModelData = CustomModelData.customModelData()
                            .addFlags(customModelDataFlags)
                            .addColors(customModelDataColors)
                            .addFloats(customModelDataFloats)
                            .build();
                    consumerList.add(itemStack -> itemStack.setData(DataComponentTypes.CUSTOM_MODEL_DATA, customModelData));
                }
                case "pdc" -> {
                    PdcEntries pdcEntries = entry.getValue().get(PdcEntries.class);
                    if (pdcEntries == null) {
                        throw new SerializationException("Unknown entry in '%s.pdc'".formatted(node.path().toString()));
                    }
                    consumerList.add(item -> {
                        item.editPersistentDataContainer(pdc -> {
                            pdcEntries.pdcEntries().forEach(pdcEntry -> pdcEntry.apply(pdc));
                        });
                    });
                }
                case "consumption" -> {
                    Consumption consumption = entry.getValue().get(Consumption.class);
                    if (consumption == null) {
                        throw new SerializationException("Unknown entry in '%s.consumption'".formatted(node.path().toString()));
                    }
                    consumerList.add(consumption::apply);
                }
                case "itemModel" -> {
                    NamespacedKey itemModel = entry.getValue().get(NamespacedKey.class);
                    if (itemModel == null) {
                        throw new SerializationException("Unknown entry in '%s.itemModel'".formatted(node.path().toString()));
                    }
                    consumerList.add(item -> item.setData(DataComponentTypes.ITEM_MODEL, itemModel));
                }
                case null, default ->
                        throw new SerializationException("Unknown entry in %s".formatted(node.path().toString()));
            }
        }
        return new ExtraItemData(consumerList);
    }

    @Override
    public void serialize(Type type, @Nullable ExtraItemData obj, ConfigurationNode node) throws SerializationException {
        throw new UnsupportedOperationException("Only deserialization is supported");
    }
}
