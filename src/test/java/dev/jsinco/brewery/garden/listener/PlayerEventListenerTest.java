package dev.jsinco.brewery.garden.listener;

import dev.jsinco.brewery.garden.configuration.GardenConfig;
import dev.jsinco.brewery.garden.structure.PlantStructure;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.joml.Matrix3d;
import org.joml.Vector3i;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockbukkit.mockbukkit.MockBukkitExtension;
import org.mockbukkit.mockbukkit.MockBukkitInject;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockbukkit.mockbukkit.world.WorldMock;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockBukkitExtension.class)
class PlayerEventListenerTest {

    @MockBukkitInject
    ServerMock serverMock;

    @Test
    void checkBlocksRejectsAnEmptyPasteWithoutConstructingAnEvent() {
        WorldMock world = serverMock.addSimpleWorld("world");
        PlayerMock player = serverMock.addPlayer();
        Block clickedBlock = world.getBlockAt(0, 0, 0);
        PlantStructure structureWithMissingWorld = new PlantStructure(
                null,
                0,
                1,
                0,
                new Matrix3d(),
                UUID.randomUUID(),
                new Vector3i()
        );
        PlayerEventListener listeners = new PlayerEventListener(null, new GardenConfig(), null);

        boolean placed = listeners.checkBlocks(
                structureWithMissingWorld,
                new ItemStack(Material.WHEAT_SEEDS),
                clickedBlock,
                player,
                EquipmentSlot.HAND
        );

        assertFalse(placed);
        assertTrue(PlayerEventListener.IGNORED_EVENTS.isEmpty());
    }
}
