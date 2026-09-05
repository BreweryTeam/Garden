package dev.jsinco.brewery.garden.database;

import dev.jsinco.brewery.garden.utility.Encoder;
import dev.jsinco.brewery.garden.utility.vector.Vector2i;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public record RegionSession(Executor executor, SqlSupplier<Connection> connectionSupplier) {
    private static final PreparedStatementStore REGION_STATEMENTS = new PreparedStatementStore("/sql/region");
    private static final PreparedStatementStore REGION_RELATION_STATEMENTS = new PreparedStatementStore("/sql/region_plant_relation");

    public CompletableFuture<Collection<RegionData>> allRegions(UUID worldUuid) {
        return CompletableFuture.supplyAsync(() -> {
            List<RegionData> regions = new ArrayList<>();
            try (Connection connection = connectionSupplier.get()) {
                PreparedStatement preparedStatement = connection.prepareStatement(REGION_STATEMENTS.get("find_regions.sql"));
                preparedStatement.setBytes(1, Encoder.asBytes(worldUuid));
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    UUID regionId = Encoder.asUuid(resultSet.getBytes("region_id"));
                    int x = resultSet.getInt("region_x");
                    int z = resultSet.getInt("region_z");
                    regions.add(new RegionData(regionId, new Vector2i(x, z)));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return regions;
        }, executor);
    }

    public CompletableFuture<Void> removeRegion(UUID worldUuid, Vector2i pos) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = connectionSupplier.get()) {
                PreparedStatement preparedStatement = connection.prepareStatement(REGION_STATEMENTS.get("delete_region.sql"));
                preparedStatement.setInt(1, pos.x());
                preparedStatement.setInt(2, pos.z());
                preparedStatement.setBytes(3, Encoder.asBytes(worldUuid));
                preparedStatement.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }, executor);
    }

    public CompletableFuture<Void> insertRegion(UUID worldUuid, Vector2i pos, UUID regionId) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = connectionSupplier.get()) {
                PreparedStatement preparedStatement = connection.prepareStatement(REGION_STATEMENTS.get("insert_region.sql"));
                preparedStatement.setBytes(1, Encoder.asBytes(regionId));
                preparedStatement.setInt(2, pos.x());
                preparedStatement.setInt(3, pos.z());
                preparedStatement.setBytes(4, Encoder.asBytes(worldUuid));
                preparedStatement.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }, executor);
    }

    public CompletableFuture<Set<UUID>> findRelatedPlants(UUID regionId) {
        return CompletableFuture.supplyAsync(() -> {
            Set<UUID> plants = new HashSet<>();
            try (Connection connection = connectionSupplier.get()) {
                PreparedStatement preparedStatement = connection.prepareStatement(REGION_RELATION_STATEMENTS.get("find_region_relations.sql"));
                preparedStatement.setBytes(1, Encoder.asBytes(regionId));
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    plants.add(Encoder.asUuid(resultSet.getBytes("plant_id")));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return plants;
        }, executor);
    }

    public CompletableFuture<Void> insertPlantRegionRelation(UUID regionId, UUID plantId) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = connectionSupplier.get()) {
                PreparedStatement preparedStatement = connection.prepareStatement(REGION_STATEMENTS.get("insert_region.sql"));
                preparedStatement.setBytes(1, Encoder.asBytes(regionId));
                preparedStatement.setBytes(2,  Encoder.asBytes(plantId));
                preparedStatement.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }, executor);
    }
}
