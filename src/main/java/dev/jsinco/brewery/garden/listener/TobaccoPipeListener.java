package dev.jsinco.brewery.garden.listener;

import dev.jsinco.brewery.garden.Garden;
import dev.jsinco.brewery.garden.tobacco.TobaccoPipe;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public record TobaccoPipeListener(Garden garden) implements Listener {

    @EventHandler
    public void onTobaccoConsume(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        if (!TobaccoPipe.isPipe(item)) {
            return;
        }
        if (!TobaccoPipe.isLit(item)) {

        }
        event.setReplacement(item);
        event.getPlayer().getScheduler().runAtFixedRate(Garden.getInstance(), new SmokeTask(event.getPlayer())::tick, null, 1L, 1L);
    }

    private static class SmokeTask {

        private static final int SMOKE_TICKS = 10;
        private static final int SMOKE_TRAIL_TICKS = 20 * 4;
        private static final double MAX_SMOKE_DISTANCE = 4.0;
        private static final double SMOKE_FLOATING_POINT = 5.0;
        private static final double MAX_SMOKE_WIND = 2D;
        private static final float SMOKE_SIZE = 4F;
        private int time = 0;
        private int hitPoint = SMOKE_TRAIL_TICKS;
        private final Player player;
        private final List<Vector> firstSmokeDirections;
        private final List<Vector> finalSmokeDirections;
        private final Location[] playerTrace = new Location[SMOKE_TICKS];

        public SmokeTask(Player player) {
            this.player = player;
            this.firstSmokeDirections = compileSmokeDirections();
            this.finalSmokeDirections = compileSmokeDirections();
        }

        private void tick(ScheduledTask task) {
            if (time++ > SMOKE_TICKS + SMOKE_TRAIL_TICKS || player.isDead()) {
                task.cancel();
                return;
            }
            if (time <= SMOKE_TICKS) {
                playerTrace[time - 1] = player.getLocation();
            }
            double globalProgress = (double) time / (SMOKE_TICKS + SMOKE_TRAIL_TICKS);
            Random random = ThreadLocalRandom.current();
            for (int smokeTrail = 0; smokeTrail < SMOKE_TICKS; smokeTrail++) {
                int trailTicks = time - smokeTrail - 1;
                if (trailTicks > hitPoint || trailTicks < 0 || (trailTicks + time) % 2 == 1) {
                    continue;
                }
                double progress = (double) trailTicks / SMOKE_TRAIL_TICKS;
                Location eye1 = playerTrace[smokeTrail];
                Location eye2 = playerTrace[Math.min(smokeTrail + 1, playerTrace.length - 1)];
                Location eye;
                if (eye2 == null || eye1.distanceSquared(eye2) > 5 * 5 || eye1.getWorld() != eye2.getWorld()) {
                    eye = eye1;
                } else {
                    eye = lerp(random.nextDouble(), eye1, eye2);
                }
                Vector inDirection = smokeOffset(progress, globalProgress, eye.getDirection()
                        .clone()
                        .multiply(MAX_SMOKE_DISTANCE)
                );
                Vector extraY = new Vector(0, easeInOutCubic(progress) * SMOKE_FLOATING_POINT, 0);
                Location finalLocation = eye.clone().add(inDirection)
                        .add(extraY);
                if (!finalLocation.isChunkLoaded()) {
                    return;
                }
                if (intersects(finalLocation)) {
                    hitPoint = Math.max(0, trailTicks - 1);
                    continue;
                }
                World world = finalLocation.getWorld();
                world.spawnParticle(Particle.DUST, finalLocation, 1, new Particle.DustOptions(Color.WHITE, (float) lerp(progress, 0.1, SMOKE_SIZE)));
            }
        }

        private Vector smokeOffset(double progress, double globalProgress, Vector startDirection) {
            List<Vector> firstSmokeDirectionsCopy = new ArrayList<>(firstSmokeDirections);
            firstSmokeDirectionsCopy.addFirst(startDirection);
            List<Vector> finalSmokeDirectionsCopy = new ArrayList<>(finalSmokeDirections);
            finalSmokeDirectionsCopy.addFirst(startDirection);
            int amount = firstSmokeDirectionsCopy.size() - 1;
            int currentSmokeDirection = (int) Math.floor(progress * amount);
            Vector output = new Vector();
            for (int i = 0; i < currentSmokeDirection; i++) {
                output.add(lerp(globalProgress,
                        firstSmokeDirectionsCopy.get(i),
                        finalSmokeDirectionsCopy.get(i)
                ));
            }
            double lastProgress = easeInOutCubic(
                    progress * amount - currentSmokeDirection
            );
            Vector vector = lerp(globalProgress, firstSmokeDirectionsCopy.get(currentSmokeDirection), finalSmokeDirectionsCopy.get(currentSmokeDirection));
            return lerp(lastProgress,
                    output,
                    output.clone().add(vector)
            );
        }

        private static List<Vector> compileSmokeDirections() {
            List<Vector> output = new ArrayList<>();
            Random random = ThreadLocalRandom.current();
            for (int i = 0; i < 3; i++) {
                double randomRadian = random.nextDouble(Math.PI * 2);
                output.add(new Vector(Math.cos(randomRadian), 0, Math.sin(randomRadian))
                        .multiply(random.nextDouble(MAX_SMOKE_WIND))
                );
            }
            return output;
        }
    }

    private static boolean intersects(Location location) {
        double xPosInBlock = location.x() - Math.floor(location.x());
        double yPosInBlock = location.y() - Math.floor(location.y());
        double zPosInBlock = location.z() - Math.floor(location.z());
        BoundingBox smokeBoundingBox = new BoundingBox(
                xPosInBlock - 0.1,
                yPosInBlock - 0.1,
                zPosInBlock - 0.1,
                xPosInBlock + 0.1,
                yPosInBlock + 0.1,
                zPosInBlock + 0.1
        );
        return location.getBlock().getCollisionShape().overlaps(smokeBoundingBox);
    }

    private static double lerp(double progress, double from, double to) {
        return (1 - progress) * from + progress * to;
    }

    private static Vector lerp(double progress, Vector from, Vector to) {
        return from.clone().multiply(1 - progress)
                .add(to.clone().multiply(progress));
    }

    private static Location lerp(double progress, Location from, Location to) {
        return new Location(
                to.getWorld(),
                lerp(progress, from.getX(), to.getX()),
                lerp(progress, from.getY(), to.getY()),
                lerp(progress, from.getZ(), to.getZ()),
                (float) lerpMod(progress, from.getYaw(), to.getYaw(), 360),
                (float) lerpMod(progress, from.getPitch(), to.getPitch(), 360)
        );
    }

    private static double lerpMod(double progress, double angleFrom, double angleTo, double mod) {
        double angleToMod = Math.abs(angleTo - angleFrom) > Math.abs(angleTo + mod - angleFrom) ?
                angleTo + mod : angleTo;
        return lerp(progress, angleFrom, angleToMod);
    }

    private static double easeInOutCubic(double x) {
        return x < 0.5 ? 4 * x * x * x : 1 - Math.pow(-2 * x + 2, 3) / 2;

    }
}
