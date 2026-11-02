package dev.jsinco.brewery.garden.listener;

import dev.jsinco.brewery.garden.Garden;
import dev.jsinco.brewery.garden.tobacco.CircleSmokeTask;
import dev.jsinco.brewery.garden.tobacco.SimpleSmokeTask;
import dev.jsinco.brewery.garden.tobacco.TobaccoPipe;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

public record TobaccoPipeListener(Garden garden) implements Listener {

    private static final NamespacedKey SMOKE_PROFICIENCY = Garden.key("tobacco/proficciency");

    @EventHandler
    public void onTobaccoConsume(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        if (!TobaccoPipe.isPipe(item)) {
            return;
        }
        event.setReplacement(item);
        if (!TobaccoPipe.isLit(item)) {
            EquipmentSlot otherHand = event.getHand() == EquipmentSlot.HAND ?
                    EquipmentSlot.OFF_HAND : EquipmentSlot.HAND;
            ItemStack possibleLighter = event.getPlayer().getInventory().getItem(otherHand);
            if (possibleLighter.getType() == Material.FLINT_AND_STEEL &&
                    !TobaccoPipe.isEmpty(item)
            ) {
                tryBurnStart(event.getPlayer(), item);
            } else if (TobaccoPipe.isFilled(item)) {
                event.getPlayer().sendActionBar(
                        Component.translatable("garden.pipe.filled")
                );
            } else {
                // TODO: Empty inventory of tobacco
                TobaccoPipe.stuff(item);
            }
            event.getPlayer().getScheduler()
                    .run(Garden.getInstance(), ignored -> event.getPlayer().setCooldown(item, 0), null);
            return;
        }
        TobaccoPipe.burn(item);
        Double proficiency = event.getPlayer().getPersistentDataContainer()
                .get(SMOKE_PROFICIENCY, PersistentDataType.DOUBLE);
        double proficiency0 = proficiency == null ? 0 : proficiency;
        double probability = proficiency0 / (proficiency0 + 10);
        boolean circle = ThreadLocalRandom.current().nextDouble() < probability;
        Consumer<ScheduledTask> consumable =
                circle ? new CircleSmokeTask(event.getPlayer())::tick : new SimpleSmokeTask(event.getPlayer())::tick;
        event.getPlayer().getScheduler().runAtFixedRate(Garden.getInstance(), consumable, null, 1L, 1L);
        if (TobaccoPipe.isEmpty(item)) {
            TobaccoPipe.setLit(item, false);
        }
        PersistentDataContainer persistentDataContainer = event.getPlayer().getPersistentDataContainer();
        if (circle) {
            persistentDataContainer.set(SMOKE_PROFICIENCY, PersistentDataType.DOUBLE, proficiency0 + 0.1);
        } else {
            persistentDataContainer.set(SMOKE_PROFICIENCY, PersistentDataType.DOUBLE, proficiency0 + 0.01);
        }
    }

    private void tryBurnStart(Player player, ItemStack item) {
        Random random = ThreadLocalRandom.current();
        player.getWorld()
                .playSound(
                        player,
                        Sound.ITEM_FLINTANDSTEEL_USE,
                        SoundCategory.PLAYERS,
                        random.nextFloat(4, 6),
                        random.nextFloat(0.5F, 1.5F)
                );
        if (random.nextBoolean()) {
            return;
        }
        TobaccoPipe.setLit(item, true);
        player.getWorld()
                .playSound(
                        player,
                        Sound.ITEM_FIRECHARGE_USE,
                        SoundCategory.PLAYERS,
                        random.nextFloat(0.2F, 0.5F),
                        random.nextFloat(0.2F, 0.3F)
                );
    }

}
