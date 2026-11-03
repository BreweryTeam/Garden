package dev.jsinco.brewery.garden.listener;

import dev.jsinco.brewery.garden.Garden;
import dev.jsinco.brewery.garden.configuration.UsingConfig;
import dev.jsinco.brewery.garden.tobacco.CircleSmokeShape;
import dev.jsinco.brewery.garden.tobacco.LineSmokeShape;
import dev.jsinco.brewery.garden.tobacco.SmokeShape;
import dev.jsinco.brewery.garden.tobacco.SmokeTask;
import dev.jsinco.brewery.garden.tobacco.SphereSmokeShape;
import dev.jsinco.brewery.garden.tobacco.TobaccoPipe;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

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
                    !TobaccoPipe.isEmpty(item) &&
                    (!hasTobacco(event.getPlayer().getInventory())
                            || TobaccoPipe.isFilled(item)
                    )
            ) {
                tryBurnStart(event.getPlayer(), item);
            } else if (TobaccoPipe.isFilled(item)) {
                event.getPlayer().sendActionBar(
                        Component.translatable("garden.pipe.filled")
                );
            } else if (decreaseTobacco(event.getPlayer().getInventory())) {
                TobaccoPipe.stuff(item);
                Random random = ThreadLocalRandom.current();
                event.getPlayer().getWorld()
                        .playSound(
                                event.getPlayer(),
                                Sound.BLOCK_CHERRY_LEAVES_PLACE,
                                SoundCategory.PLAYERS,
                                random.nextFloat(0.2F, 0.5F),
                                random.nextFloat(0.1F, 0.2F)
                        );
            } else {
                event.getPlayer().sendActionBar(
                        Component.translatable("garden.pipe.empty")
                );
            }
            event.getPlayer().getScheduler()
                    .run(Garden.getInstance(), ignored -> event.getPlayer().setCooldown(item, 0), null);
            return;
        }
        TobaccoPipe.burn(item);
        Double proficiency = event.getPlayer().getPersistentDataContainer()
                .get(SMOKE_PROFICIENCY, PersistentDataType.DOUBLE);
        double proficiency0 = proficiency == null ? 0 : proficiency;
        double sphereProbability = proficiency0 / (proficiency0 + 10);
        double circleProbability = proficiency0 / (proficiency0 + 100);
        Random random = ThreadLocalRandom.current();
        SmokeShape shape;
        PersistentDataContainer persistentDataContainer = event.getPlayer().getPersistentDataContainer();
        if (random.nextDouble() < circleProbability) {
            persistentDataContainer.set(SMOKE_PROFICIENCY, PersistentDataType.DOUBLE, proficiency0 + 0.1);
            shape = new CircleSmokeShape(1, 2);
        } else if (random.nextDouble() < sphereProbability) {
            persistentDataContainer.set(SMOKE_PROFICIENCY, PersistentDataType.DOUBLE, proficiency0 + 0.05);
            shape = new SphereSmokeShape(0.25);
        } else {
            persistentDataContainer.set(SMOKE_PROFICIENCY, PersistentDataType.DOUBLE, proficiency0 + 0.01);
            shape = new LineSmokeShape();
        }
        event.getPlayer().getScheduler().runAtFixedRate(Garden.getInstance(), new SmokeTask(event.getPlayer(), shape)::tick, null, 1L, 1L);
        if (TobaccoPipe.isEmpty(item)) {
            TobaccoPipe.setLit(item, false);
        }
    }

    @EventHandler
    public void onTobaccoInteract(PlayerInteractEvent event) {
        EquipmentSlot otherHand = event.getHand() == EquipmentSlot.HAND ?
                EquipmentSlot.OFF_HAND : EquipmentSlot.HAND;
        ItemStack otherItem = event.getPlayer().getInventory().getItem(otherHand);
        ItemStack item = event.getItem();
        if (item == null || item.isEmpty() || otherItem.isEmpty()) {
            return;
        }
        if (TobaccoPipe.isPipe(otherItem)) {
            event.setUseItemInHand(Event.Result.DENY);
            event.setUseInteractedBlock(Event.Result.DENY);
        }
    }

    private boolean hasTobacco(Inventory inventory) {
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack itemStack = inventory.getItem(i);
            if (itemStack == null || itemStack.isEmpty()) {
                continue;
            }
            if (UsingConfig.instance().getPipeUsable()
                    .stream().anyMatch(string -> string.equalsIgnoreCase(
                            itemStack.getPersistentDataContainer().get(Garden.ITEM, PersistentDataType.STRING)
                    ))
            ) {
                return true;
            }
        }
        return false;
    }

    private boolean decreaseTobacco(Inventory inventory) {
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack itemStack = inventory.getItem(i);
            if (itemStack == null || itemStack.isEmpty()) {
                continue;
            }
            if (UsingConfig.instance().getPipeUsable()
                    .stream().anyMatch(string -> string.equalsIgnoreCase(
                            itemStack.getPersistentDataContainer().get(Garden.ITEM, PersistentDataType.STRING)
                    ))
            ) {
                itemStack.setAmount(itemStack.getAmount() - 1);
                return true;
            }
        }
        return false;
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
