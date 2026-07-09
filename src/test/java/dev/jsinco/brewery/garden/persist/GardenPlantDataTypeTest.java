package dev.jsinco.brewery.garden.persist;

import dev.jsinco.brewery.garden.Garden;
import dev.jsinco.brewery.garden.MutableGardenRegistry;
import dev.jsinco.brewery.garden.plant.GardenPlant;
import org.bukkit.Location;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.MockBukkitExtension;
import org.mockbukkit.mockbukkit.MockBukkitInject;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.world.WorldMock;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockBukkitExtension.class)
class GardenPlantDataTypeTest {
    @MockBukkitInject
    ServerMock serverMock;

    private @NonNull WorldMock world;
    private GardenPlantDataType dataType;


    @BeforeEach
    void setup() {
        @NonNull Garden garden = MockBukkit.load(Garden.class);
        this.dataType = garden.getGardenPlantDataType();
        this.world = serverMock.addSimpleWorld("world");
    }

    @Disabled("MockBukkit not supporting newer block properties")
    @TestFactory
    Stream<DynamicTest> checkPersistence() {
        return MutableGardenRegistry.PLANT_TYPE.values()
                .stream()
                .map(plantType -> DynamicTest.dynamicTest(
                        "checkPersistence(" + plantType + ")",
                        () -> {
                            GardenPlant gardenPlant = new GardenPlant(plantType, new Location(world, 1, 0, 0));
                            dataType.insert(gardenPlant);
                            checkEquals(gardenPlant, dataType.fetch(world).join().get(0));
                            assertEquals(1, dataType.fetch(world).join().size());
                            gardenPlant.incrementGrowthStage(1, Garden.getGardenRegistry(), Garden.getInstance().getGardenPlantDataType());
                            dataType.update(gardenPlant);
                            checkEquals(gardenPlant, dataType.fetch(world).join().get(0));
                            assertEquals(1, dataType.fetch(world).join().size());
                            dataType.remove(gardenPlant);
                            assertTrue(dataType.fetch(world).join().isEmpty());
                        }
                ));
    }

    void checkEquals(GardenPlant expected, GardenPlant actual) {
        assertEquals(expected.getAge(), actual.getAge());
        assertEquals(expected.getType(), actual.getType());
        assertEquals(expected.getStructure(), actual.getStructure());
    }
}