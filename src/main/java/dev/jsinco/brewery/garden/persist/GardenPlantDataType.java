package dev.jsinco.brewery.garden.persist;

import dev.jsinco.brewery.garden.Garden;
import dev.jsinco.brewery.garden.GardenRegistry;
import dev.jsinco.brewery.garden.plant.GardenPlant;
import dev.jsinco.brewery.garden.plant.PlantType;
import dev.jsinco.brewery.garden.structure.PlantStructure;
import dev.jsinco.brewery.garden.utility.Encoder;
import dev.jsinco.brewery.garden.utility.FileUtil;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.joml.Matrix3d;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class GardenPlantDataType {

    private final Database database;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();


    public GardenPlantDataType(Database database) {
        this.database = database;
    }

    public CompletableFuture<Void> insert(GardenPlant plant) {
        return CompletableFuture.supplyAsync(() -> {
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
            return null;
        });
    }

    public CompletableFuture<Void> update(GardenPlant plant) {
        return CompletableFuture.supplyAsync(() -> {
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
            return null;
        });
    }

    public CompletableFuture<Void> remove(GardenPlant plant) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = database.getConnection()) {
                PreparedStatement preparedStatement = connection.prepareStatement(FileUtil.readInternalResource("/sql/remove_plant.sql"));
                preparedStatement.setBytes(1, Encoder.asBytes(plant.getId()));
                preparedStatement.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    public CompletableFuture<List<GardenPlant>> fetch(World world) {
        return CompletableFuture.supplyAsync(() -> {
            List<GardenPlant> output = new ArrayList<>();
            try (Connection connection = database.getConnection()) {
                PreparedStatement preparedStatement = connection.prepareStatement(FileUtil.readInternalResource("/sql/find_plant.sql"));
                preparedStatement.setBytes(1, Encoder.asBytes(world.getUID()));
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    PlantType plantType = GardenRegistry.PLANT_TYPE.get(NamespacedKey.fromString(resultSet.getString("plant_type")));
                    Location origin = new Location(world, resultSet.getInt("origin_x"), resultSet.getInt("origin_y"), resultSet.getInt("origin_z"));
                    if (plantType == null) {
                        Garden.getInstance().getLogger().warning("Could not read plant at: " + origin);
                        Garden.getInstance().getLogger().warning("Unknown plant type: " + resultSet.getString("plant_type"));
                        continue;
                    }
                    int age = resultSet.getInt("age");
                    String track = resultSet.getString("track");
                    Matrix3d transformation = Encoder.deserializeTransformation(resultSet.getString("transformation"));
                    output.add(
                            new GardenPlant(
                                    Encoder.asUuid(resultSet.getBytes("id")),
                                    plantType,
                                    plantType.getStructure(origin, age, track, transformation),
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
        });
    }
}
