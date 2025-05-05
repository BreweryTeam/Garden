package dev.jsinco.brewery.garden.persist;

import dev.jsinco.brewery.garden.BreweryGarden;
import dev.jsinco.brewery.garden.constants.PlantType;
import dev.jsinco.brewery.garden.objects.GardenPlant;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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

    private @NotNull WorldMock world;
    private GardenPlantDataType dataType;


    @BeforeEach
    void setup() {
        @NotNull BreweryGarden garden = MockBukkit.load(BreweryGarden.class);
        this.dataType = garden.getGardenPlantDataType();
        this.world = serverMock.addSimpleWorld("world");
    }

    @ParameterizedTest
    @MethodSource("getPlantTypes")
    void checkPersistence(PlantType plantType) {
        GardenPlant gardenPlant = new GardenPlant(plantType, new Location(world, 1, 0, 0));
        dataType.insert(gardenPlant);
        checkEquals(gardenPlant, dataType.fetch(world).get(0));
        assertEquals(1, dataType.fetch(world).size());
        gardenPlant.incrementGrowthStage(1);
        dataType.update(gardenPlant);
        checkEquals(gardenPlant, dataType.fetch(world).get(0));
        assertEquals(1, dataType.fetch(world).size());
        dataType.remove(gardenPlant);
        assertTrue(dataType.fetch(world).isEmpty());
    }

    void checkEquals(GardenPlant expected, GardenPlant actual) {
        assertEquals(expected.getAge(), actual.getAge());
        assertEquals(expected.getType(), actual.getType());
        assertEquals(expected.getRegion(), actual.getRegion());
        assertEquals(expected.getRegion().getWorld(), actual.getRegion().getWorld());
    }

    public static Stream<Arguments> getPlantTypes() {
        return PlantType.values()
                .stream()
                .map(Arguments::of);
    }

}