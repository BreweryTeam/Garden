package dev.jsinco.brewery.garden.persist;

import dev.jsinco.brewery.garden.GardenRegistry;
import dev.jsinco.brewery.garden.plant.GardenPlant;
import dev.jsinco.brewery.garden.plant.PlantType;
import dev.jsinco.brewery.garden.structure.PlantStructure;
import dev.jsinco.brewery.garden.utility.Encoder;
import dev.jsinco.brewery.garden.utility.FileUtil;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GardenPlantDataType {

    private final Database database;

    public GardenPlantDataType(Database database) {
        this.database = database;
    }

    public void insert(GardenPlant plant) {
        try (Connection connection = database.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(FileUtil.readInternalResource("/sql/insert_plant.sql"));
            preparedStatement.setBytes(1, Encoder.asBytes(plant.getId()));
            preparedStatement.setString(2, plant.getType().key().toString());
            preparedStatement.setInt(3, plant.getAge());
            PlantStructure structure = plant.getStructure();
            preparedStatement.setInt(4, structure.originX());
            preparedStatement.setInt(5, structure.originY());
            preparedStatement.setInt(6, structure.originZ());
            preparedStatement.setBytes(7, Encoder.asBytes(structure.worldUuid()));
            preparedStatement.setString(8, Encoder.serializeTransformation(structure.transformation()));
            preparedStatement.setString(9, plant.getTrack());
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void update(GardenPlant plant) {
        try (Connection connection = database.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(FileUtil.readInternalResource("/sql/update_plant.sql"));
            preparedStatement.setInt(1, plant.getAge());
            PlantStructure structure = plant.getStructure();
            preparedStatement.setString(2, Encoder.serializeTransformation(structure.transformation()));
            preparedStatement.setBytes(3, Encoder.asBytes(plant.getId()));
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void remove(GardenPlant plant) {
        try (Connection connection = database.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(FileUtil.readInternalResource("/sql/remove_plant.sql"));
            preparedStatement.setBytes(1, Encoder.asBytes(plant.getId()));
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<GardenPlant> fetch(World world) {
        List<GardenPlant> output = new ArrayList<>();
        try (Connection connection = database.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(FileUtil.readInternalResource("/sql/find_plant.sql"));
            preparedStatement.setBytes(1, Encoder.asBytes(world.getUID()));
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                PlantType plantType = GardenRegistry.PLANT_TYPE.get(NamespacedKey.fromString(resultSet.getString("plant_type")));
                Location origin = new Location(world, resultSet.getInt("origin_x"), resultSet.getInt("origin_y"), resultSet.getInt("origin_z"));
                int age = resultSet.getInt("age");
                String track = resultSet.getString("track");
                output.add(
                        new GardenPlant(
                                Encoder.asUuid(resultSet.getBytes("id")),
                                plantType,
                                plantType.newStructure(origin, age, track),
                                track,
                                age,
                                false
                        )
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return output;
    }
}
