package dev.jsinco.brewery.garden.plant.item.extra;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Consumable;
import io.papermc.paper.datacomponent.item.FoodProperties;
import io.papermc.paper.datacomponent.item.consumable.ConsumeEffect;
import io.papermc.paper.datacomponent.item.consumable.ItemUseAnimation;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.List;

@ConfigSerializable
@NullMarked
public record Consumption(@Nullable @Setting("consumeEffects") List<ConsumeEffect> consumeEffects,
                          @Nullable ItemUseAnimation animation,
                          @Nullable @Setting("consumeSeconds") Float consumeSeconds,
                          @Nullable @Setting("consumeParticles") Boolean consumeParticles,
                          @Nullable NamespacedKey sound, @Nullable @Setting("canAlwaysEat") Boolean canAlwaysEat,
                          @Nullable Integer nutrition, @Nullable Float saturation) {

    public void apply(ItemStack itemStack) {
        Consumable.Builder consumable = Consumable.consumable();
        if (consumeEffects != null) {
            consumable.addEffects(consumeEffects);
        }
        if (animation != null) {
            consumable.animation(animation);
        }
        if (consumeSeconds != null) {
            consumable.consumeSeconds(consumeSeconds);
        }
        if (consumeParticles != null) {
            consumable.hasConsumeParticles(consumeParticles);
        }
        if (sound != null) {
            consumable.sound(sound);
        }
        itemStack.setData(DataComponentTypes.CONSUMABLE, consumable.build());
        FoodProperties.Builder foodProperties = FoodProperties.food();
        if (canAlwaysEat != null) {
            foodProperties.canAlwaysEat(canAlwaysEat);
        }
        if (nutrition != null) {
            foodProperties.nutrition(nutrition);
        }
        if (saturation != null) {
            foodProperties.saturation(saturation);
        }
        itemStack.setData(DataComponentTypes.FOOD, foodProperties.build());
    }
}
