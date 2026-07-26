package dev.jsinco.brewery.garden.plant.item.extra;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public sealed interface PdcPrimitive {

    void apply(PersistentDataContainer pdc, NamespacedKey key);

    record BooleanPrimitive(boolean value) implements PdcPrimitive {

        @Override
        public void apply(PersistentDataContainer pdc, NamespacedKey key) {
            pdc.set(key, PersistentDataType.BOOLEAN, value);
        }
    }

    record BytePrimitive(byte value) implements PdcPrimitive {

        @Override
        public void apply(PersistentDataContainer pdc, NamespacedKey key) {
            pdc.set(key, PersistentDataType.BYTE, value);
        }
    }

    record ShortPrimitive(short value) implements PdcPrimitive {

        @Override
        public void apply(PersistentDataContainer pdc, NamespacedKey key) {
            pdc.set(key, PersistentDataType.SHORT, value);
        }
    }

    record IntegerPrimitive(int value) implements PdcPrimitive {

        @Override
        public void apply(PersistentDataContainer pdc, NamespacedKey key) {
            pdc.set(key, PersistentDataType.INTEGER, value);
        }
    }

    record LongPrimitive(long value) implements PdcPrimitive {

        @Override
        public void apply(PersistentDataContainer pdc, NamespacedKey key) {
            pdc.set(key, PersistentDataType.LONG, value);
        }
    }

    record FloatPrimitive(float value) implements PdcPrimitive {

        @Override
        public void apply(PersistentDataContainer pdc, NamespacedKey key) {
            pdc.set(key, PersistentDataType.FLOAT, value);
        }
    }

    record DoublePrimitive(double value) implements PdcPrimitive {

        @Override
        public void apply(PersistentDataContainer pdc, NamespacedKey key) {
            pdc.set(key, PersistentDataType.DOUBLE, value);
        }
    }

    record StringPrimitive(String value) implements PdcPrimitive {

        @Override
        public void apply(PersistentDataContainer pdc, NamespacedKey key) {
            pdc.set(key, PersistentDataType.STRING, value);
        }
    }
}
