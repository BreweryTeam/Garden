package dev.jsinco.brewery.garden.objects;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.BoundingBox;

import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
public class WorldTiedBoundingBox extends BoundingBox {

    private World world;

    public WorldTiedBoundingBox(World world, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        super(minX, minY, minZ, maxX, maxY, maxZ);
        this.world = world;
    }

    public List<Block> getBlocks() {
        List<Block> bL = new LinkedList<>();
        for (int x = (int) this.getMinX(); x <= (int) this.getMaxX(); ++x) {
            for (int y = (int) this.getMinY(); y <= (int) this.getMaxY(); ++y) {
                for (int z = (int) this.getMinZ(); z <= (int) this.getMaxZ(); ++z) {
                    bL.add(this.world.getBlockAt(x, y, z));
                }
            }
        }
        return bL;
    }

    @Override
    public String toString() {
        return String.join(",",
                String.valueOf(getMinX()),
                String.valueOf(getMinY()),
                String.valueOf(getMinZ()),
                String.valueOf(getMaxX()),
                String.valueOf(getMaxY()),
                String.valueOf(getMaxZ())
        );
    }


    public static WorldTiedBoundingBox of(Location location, Location location2) {
        return new WorldTiedBoundingBox(location.getWorld(), location.getX(), location.getY(), location.getZ(), location2.getX(), location2.getY(), location2.getZ());
    }

    public static WorldTiedBoundingBox fromString(String data, World world) {
        String[] split = data.split(",");
        double x1 = Double.parseDouble(split[0]);
        double y1 = Double.parseDouble(split[1]);
        double z1 = Double.parseDouble(split[2]);
        double x2 = Double.parseDouble(split[3]);
        double y2 = Double.parseDouble(split[4]);
        double z2 = Double.parseDouble(split[5]);
        return new WorldTiedBoundingBox(world, x1, y1, z1, x2, y2, z2);
    }
}
