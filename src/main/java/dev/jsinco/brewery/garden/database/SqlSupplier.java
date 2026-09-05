package dev.jsinco.brewery.garden.database;

import java.sql.SQLException;

@FunctionalInterface
public interface SqlSupplier<T> {

    public T get() throws SQLException;
}
