package dev.jsinco.brewery.garden.persist;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.jsinco.brewery.garden.utility.FileUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Database {

    private static final int DATABASE_VERSION = 0;
    private HikariDataSource hikariDataSource;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public Database() {

    }

    public void init(File dataFolder) throws IOException, SQLException {
        HikariConfig config = getHikariConfigForSqlite(dataFolder);
        config.setConnectionInitSql("PRAGMA foreign_keys = ON;");
        this.hikariDataSource = new HikariDataSource(config);
        executeMultiple("/sql/create_all_tables.sql");
        try (Connection connection = getConnection()) {
            ResultSet statement = connection.prepareStatement(FileUtil.readInternalResource("/sql/get_version.sql"))
                    .executeQuery();
            if (statement.next()) {
                int version = statement.getInt("version");
                if (version < DATABASE_VERSION) {
                    // migrate
                    updateVersion(connection);
                } else if (version > DATABASE_VERSION) {
                    throw new IllegalStateException("Can not downgrade the plugin!");
                }
            } else {
                updateVersion(connection);
            }
        }
    }

    private void updateVersion(Connection connection) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(FileUtil.readInternalResource("/sql/set_version.sql"))) {
            preparedStatement.setInt(1, DATABASE_VERSION);
            preparedStatement.execute();
        }
    }

    public Connection getConnection() throws SQLException {
        return hikariDataSource.getConnection();
    }

    private static @NotNull HikariConfig getHikariConfigForSqlite(File dataFolder) throws IOException {
        File databaseFile = new File(dataFolder, "brewery.db");
        if (!databaseFile.exists() && !databaseFile.getParentFile().mkdirs() && !databaseFile.createNewFile()) {
            throw new IOException("Could not create file or dirs");
        }
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setPoolName("SQLiteConnectionPool");
        hikariConfig.setDriverClassName("org.sqlite.JDBC");
        hikariConfig.setJdbcUrl("jdbc:sqlite:" + databaseFile);
        return hikariConfig;
    }

    private void executeMultiple(String resourceString) throws SQLException {
        try (Connection connection = hikariDataSource.getConnection()) {
            for (String statement : FileUtil.readInternalResource(resourceString).split(";")) {
                connection.prepareStatement(statement + ";").execute();
            }
        }
    }

}
