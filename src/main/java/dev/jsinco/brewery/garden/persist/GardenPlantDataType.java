package dev.jsinco.brewery.garden.persist;

import dev.jsinco.brewery.garden.constants.PlantType;
import dev.jsinco.brewery.garden.objects.GardenPlant;
import dev.jsinco.brewery.garden.objects.WorldTiedBoundingBox;
import dev.jsinco.brewery.garden.utility.Encoder;
import dev.jsinco.brewery.garden.utility.FileUtil;
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
            preparedStatement.setString(2, plant.getType().key());
            preparedStatement.setInt(3, plant.getAge());
            WorldTiedBoundingBox worldTiedBoundingBox = plant.getRegion();
            preparedStatement.setString(4, worldTiedBoundingBox.toString());
            preparedStatement.setBytes(5, Encoder.asBytes(worldTiedBoundingBox.getWorld().getUID()));
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void update(GardenPlant plant) {
        try (Connection connection = database.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(FileUtil.readInternalResource("/sql/update_plant.sql"));
            preparedStatement.setString(1, plant.getType().key());
            preparedStatement.setInt(2, plant.getAge());
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
                output.add(
                        new GardenPlant(
                                Encoder.asUuid(resultSet.getBytes("id")),
                                PlantType.valueOf(resultSet.getString("plant_type")),
                                WorldTiedBoundingBox.fromString(resultSet.getString("bounding_box"), world),
                                resultSet.getInt("age")
                        )
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return output;
    }
}
