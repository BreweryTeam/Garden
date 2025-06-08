package dev.jsinco.brewery.garden.plant;

import dev.jsinco.brewery.garden.BreweryGarden;
import dev.jsinco.brewery.garden.PlantRegistry;
import dev.jsinco.brewery.garden.configuration.BreweryGardenConfig;
import dev.jsinco.brewery.garden.structure.PlantStructure;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.bukkit.Location;

import java.util.UUID;

@Getter
@ToString
@AllArgsConstructor
public class GardenPlant {
    private static final BreweryGardenConfig config = BreweryGarden.getInstance().getPluginConfiguration();

    private final UUID id;
    private final PlantType type;
    private PlantStructure structure;
    private final String track;
    private int age;

    public GardenPlant(PlantType type, Location location) {
        this.id = UUID.randomUUID();
        this.type = type;
        this.age = 0;
        this.track = type.getRandomTrack();
        this.structure = type.newStructure(location, age, track);
    }

    public boolean isFullyGrown() {
        return this.age >= config.getFullyGrown();
    }

    public void incrementGrowthStage(int amount, PlantRegistry registry) {
        this.setGrowthStage(age + amount, registry);
    }

    public void setGrowthStage(int growthStage, PlantRegistry registry) {
        this.age = growthStage;
        this.structure.remove();
        registry.unregisterPlant(this);
        this.structure = type.newStructure(this.structure.origin(), this.age, track);
        registry.registerPlant(this);
    }
}
