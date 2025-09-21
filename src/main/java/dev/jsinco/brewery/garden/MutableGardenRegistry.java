package dev.jsinco.brewery.garden;

import dev.jsinco.brewery.garden.plant.PlantType;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public class MutableGardenRegistry<T extends Keyed> {

    public static final MutableGardenRegistry<PlantType> plantType = new MutableGardenRegistry<>(PlantType.readPlantTypes());


    private Map<Key, T> backing;

    private MutableGardenRegistry(Collection<T> values) {
        backing = values.stream().collect(Collectors.toUnmodifiableMap(Keyed::key, value -> value));
    }

    public @Nullable T get(@NotNull NamespacedKey key) {
        return backing.get(key);
    }

    public Collection<T> values() {
        return backing.values();
    }

    public void newBacking(Collection<T> values) {
        backing = values.stream().collect(Collectors.toUnmodifiableMap(Keyed::key, value -> value));
    }
}
