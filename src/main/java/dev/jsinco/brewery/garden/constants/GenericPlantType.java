package dev.jsinco.brewery.garden.constants;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;

@Getter
public abstract class GenericPlantType {

    protected final Component name;

    public GenericPlantType(Component name) {
        this.name = name;
    }

    public abstract ItemStack getItemStack(int amount);

    @Override
    public abstract String toString();

    public abstract String name();

    public abstract String key();
}
