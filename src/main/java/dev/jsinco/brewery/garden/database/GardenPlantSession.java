package dev.jsinco.brewery.garden.database;

import dev.jsinco.brewery.garden.Garden;
import dev.jsinco.brewery.garden.MutableGardenRegistry;
import dev.jsinco.brewery.garden.plant.GardenPlant;
import dev.jsinco.brewery.garden.plant.PlantType;
import dev.jsinco.brewery.garden.structure.PlantStructure;
import dev.jsinco.brewery.garden.utility.Encoder;
import dev.jsinco.brewery.garden.utility.Logger;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.joml.Matrix3d;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public record GardenPlantSession(Executor executor, SqlSupplier<Connection> connectionSupplier) {

    private static final PreparedStatementStore PREPARED_STATEMENT_STORE = new PreparedStatementStore("/sql/plant");

    public CompletableFuture<Void> insert(GardenPlant plant) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = connectionSupplier.get()) {
                PreparedStatement preparedStatement = connection.prepareStatement(PREPARED_STATEMENT_STORE.get("insert_plant.sql"));
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
                preparedStatement.setInt(10, plant.getExpectedFruits());
                preparedStatement.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }, executor);
    }

    public CompletableFuture<Void> update(GardenPlant plant) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = connectionSupplier.get()) {
                PreparedStatement preparedStatement = connection.prepareStatement(PREPARED_STATEMENT_STORE.get("update_plant.sql"));
                preparedStatement.setInt(1, plant.getAge());
                PlantStructure structure = plant.getStructure();
                preparedStatement.setString(2, Encoder.serializeTransformation(structure.transformation()));
                preparedStatement.setInt(3, plant.getExpectedFruits());
                preparedStatement.setBytes(4, Encoder.asBytes(plant.getId()));
                preparedStatement.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }, executor);
    }

    public CompletableFuture<Void> remove(GardenPlant plant) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = connectionSupplier.get()) {
                PreparedStatement preparedStatement = connection.prepareStatement(PREPARED_STATEMENT_STORE.get("remove_plant.sql"));
                preparedStatement.setBytes(1, Encoder.asBytes(plant.getId()));
                preparedStatement.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }, executor);
    }

    public CompletableFuture<GardenPlant> findFromPlantId(World world, UUID plantId) {
        return CompletableFuture.supplyAsync(() -> {
            Set<String> reportedInvalidTracks = new HashSet<>();
            try (Connection connection = connectionSupplier.get()) {
                PreparedStatement preparedStatement = connection.prepareStatement(PREPARED_STATEMENT_STORE.get("find_plant.sql"));
                preparedStatement.setBytes(1, Encoder.asBytes(plantId));
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    PlantType plantType = MutableGardenRegistry.PLANT_TYPE.get(NamespacedKey.fromString(resultSet.getString("plant_type")));
                    Location origin = new Location(world, resultSet.getInt("origin_x"), resultSet.getInt("origin_y"), resultSet.getInt("origin_z"));
                    if (plantType == null) {
                        Garden.getInstance().getLogger().warning("Could not read plant at: " + origin);
                        Garden.getInstance().getLogger().warning("Unknown plant type: " + resultSet.getString("plant_type"));
                        continue;
                    }
                    int age = resultSet.getInt("age");
                    String track = resultSet.getString("track");
                    Matrix3d transformation = Encoder.deserializeTransformation(resultSet.getString("transformation"));
                    Optional<PlantStructure> structure = plantType.getStructure(origin, age, track, transformation);
                    if (structure.isEmpty()) {
                        String trackIdentifier = "%s.%s".formatted(Garden.minimized(plantType.key()), track);
                        if (!reportedInvalidTracks.contains(trackIdentifier)) {
                            Logger.logWarn("Invalid plant at %s - Undefined track '%s'. Ignoring remaining issues of same type...".formatted(origin, trackIdentifier));
                            reportedInvalidTracks.add(trackIdentifier);
                        }
                        continue;
                    }
                    return new GardenPlant(
                            Encoder.asUuid(resultSet.getBytes("id")),
                            plantType,
                            structure.get(),
                            track,
                            age,
                            resultSet.getInt("fruits")
                    );
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }, executor);
    }
}
