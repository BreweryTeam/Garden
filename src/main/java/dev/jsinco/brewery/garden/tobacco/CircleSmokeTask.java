package dev.jsinco.brewery.garden.tobacco;

import dev.jsinco.brewery.garden.utility.EazingUtil;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class CircleSmokeTask {

    private static final int SMOKE_TICKS = 2;
    private static final int SMOKE_TRAIL_TICKS = 20 * 4;
    private static final double MAX_SMOKE_DISTANCE = 4.0;
    private static final double SMOKE_FLOATING_POINT = 5.0;
    private static final double MAX_SMOKE_WIND = 2D;
    private static final float SMOKE_SIZE = 4F;
    private static final int SMOKE_CIRCLE_POINTS = 16;
    private int time = 0;
    private int hitPoint = SMOKE_TRAIL_TICKS;
    private final Player player;
    private final List<Vector> firstSmokeDirections;
    private final List<Vector> finalSmokeDirections;
    private final Location[] playerTrace = new Location[SMOKE_TICKS];

    public CircleSmokeTask(Player player) {
        this.player = player;
        this.firstSmokeDirections = compileSmokeDirections();
        this.finalSmokeDirections = compileSmokeDirections();
    }

    public void tick(ScheduledTask task) {
        if (time++ > SMOKE_TICKS + SMOKE_TRAIL_TICKS || player.isDead()) {
            task.cancel();
            return;
        }
        if (time <= SMOKE_TICKS) {
            playerTrace[time - 1] = player.getEyeLocation();
        }
        double globalProgress = (double) Math.clamp(time - SMOKE_TRAIL_TICKS / 2, 0, SMOKE_TICKS) / (SMOKE_TICKS);
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
                eye = EazingUtil.lerp(random.nextDouble(), eye1, eye2);
            }
            Vector xDirection = eye.getDirection().clone().crossProduct(new Vector(0, 1, 0))
                    .normalize();
            Vector zDirection = xDirection.clone().crossProduct(eye.getDirection());
            double offsetRadians = random.nextDouble(Math.PI * 2);
            for (int i = 0; i < SMOKE_CIRCLE_POINTS; i++) {
                double radians = Math.PI * 2 * i / SMOKE_CIRCLE_POINTS;
                Vector firstDirection = xDirection.clone()
                        .multiply(Math.cos(offsetRadians + radians))
                        .add(zDirection.clone()
                                .multiply(Math.sin(offsetRadians + radians))
                        ).add(eye.getDirection().clone().multiply(4))
                        .normalize();
                Vector inDirection = smokeOffset(progress, globalProgress, firstDirection
                        .multiply(MAX_SMOKE_DISTANCE)
                );
                Vector extraY = new Vector(0, EazingUtil.easeInOutCubic(progress) * SMOKE_FLOATING_POINT, 0);
                Location finalLocation = eye.clone().add(inDirection)
                        .add(extraY);
                if (!finalLocation.isChunkLoaded()) {
                    continue;
                }
                if (TobaccoPipe.smokeIntersects(finalLocation)) {
                    hitPoint = Math.max(0, trailTicks - 1);
                    continue;
                }
                World world = finalLocation.getWorld();
                world.spawnParticle(Particle.DUST, finalLocation, 1, new Particle.DustOptions(Color.WHITE, (float) EazingUtil.lerp(progress, 0.1, SMOKE_SIZE)));
            }
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
            output.add(EazingUtil.lerp(globalProgress,
                    firstSmokeDirectionsCopy.get(i),
                    finalSmokeDirectionsCopy.get(i)
            ));
        }
        double lastProgress = EazingUtil.easeInOutCubic(
                progress * amount - currentSmokeDirection
        );
        Vector vector = EazingUtil.lerp(globalProgress, firstSmokeDirectionsCopy.get(currentSmokeDirection), finalSmokeDirectionsCopy.get(currentSmokeDirection));
        return EazingUtil.lerp(lastProgress,
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
