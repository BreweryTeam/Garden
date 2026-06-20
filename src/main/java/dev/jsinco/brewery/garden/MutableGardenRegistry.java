package dev.jsinco.brewery.garden;

import dev.jsinco.brewery.garden.plant.PlantType;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.bukkit.NamespacedKey;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public class MutableGardenRegistry<T extends Keyed> {

    public static final MutableGardenRegistry<PlantType> PLANT_TYPE = new MutableGardenRegistry<>(PlantType.readPlantTypes());

    private Map<Key, T> backing;

    private MutableGardenRegistry(Collection<T> values) {
        backing = values.stream().collect(Collectors.toUnmodifiableMap(Keyed::key, value -> value));
    }

    public @Nullable T get(@NonNull NamespacedKey key) {
        return backing.get(key);
    }

    public Collection<T> values() {
        return backing.values();
    }

    public void newBacking(Collection<T> values) {
        backing = values.stream().collect(Collectors.toUnmodifiableMap(Keyed::key, value -> value));
    }
}
