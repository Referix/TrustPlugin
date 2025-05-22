package org.referix.database;


import org.jetbrains.annotations.Nullable;
import org.referix.database.pojo.SafeZoneDB;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.List;

public interface DatabaseProvider {
    void connect();
    void close();
    void createTable(DatabaseTable table);
    void updatePlayerCommand(UUID playerId, int line);
    <T> void insertDataAsync(DatabaseTable table, T object, Runnable callback);
    void updatePlayerTrust(UUID playerId, double newTrust);
    <T> void searchData(DatabaseTable table, String condition, Class<T> clazz, Consumer<List<T>> callback);
    <T> void searchDataRaw(String sql, Object[] params, Class<T> clazz, Consumer<List<T>> callback);
    void countRows(String sql, Consumer<Integer> callback);
    void countRows(String sql, Object[] params, Consumer<Integer> callback);
    void deleteById(DatabaseTable table, Object id);
    void updateSafeZone(SafeZoneDB safeZone, Runnable callback);
}

