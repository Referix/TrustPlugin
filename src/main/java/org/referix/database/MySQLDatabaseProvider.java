package org.referix.database;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import org.referix.database.pojo.SafeZoneDB;
import org.referix.trustPlugin.TrustPlugin;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;
import java.util.function.Consumer;

public class MySQLDatabaseProvider implements DatabaseProvider {
    private final String host, database, username, password;
    private final int port;
    private Connection connection;
    private boolean debug = TrustPlugin.getInstance().isDebugEnabled();

    public MySQLDatabaseProvider(String host, int port, String database, String username, String password) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    @Override
    public void connect() {
        try {
            if (connection != null && !connection.isClosed()) return;
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
            connection = DriverManager.getConnection(url, username, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void createTable(DatabaseTable table, TableCreateCallback callback) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try (Statement stmt = connection.createStatement()) {
                    // Використовуємо готовий рядок CREATE TABLE із правильними кавичками та
                    // без зайвих дужок/сʼєднань
                    String sql = table.getCreateTableSQL();
                    stmt.execute(sql);

                    // Після успіху викликаємо callback у головному потоці
                    Bukkit.getScheduler().runTask(
                            TrustPlugin.getInstance(),
                            callback::onSuccess
                    );
                } catch (SQLException e) {
                    // При помилці викликаємо помилковий callback у головному потоці
                    Bukkit.getScheduler().runTask(
                            TrustPlugin.getInstance(),
                            () -> callback.onError(e)
                    );
                }
            }
        }.runTaskAsynchronously(TrustPlugin.getInstance());
    }


    @Override
    public <T> void insertDataAsync(DatabaseTable table, T object, Runnable callback) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    Class<?> clazz = object.getClass();
                    Field[] fields = clazz.getDeclaredFields();

                    StringBuilder columns = new StringBuilder();
                    StringBuilder placeholders = new StringBuilder();
                    List<Object> values = new ArrayList<>();

                    for (Field field : fields) {
                        field.setAccessible(true);
                        if ("id".equals(field.getName()) || "log_timestamp".equals(field.getName())) continue;

                        columns.append("`").append(field.getName()).append("`,");
                        placeholders.append("?,");

                        Object value;
                        if (field.getType() == Timestamp.class) {
                            value = System.currentTimeMillis();
                            TrustPlugin.getInstance().debug("[DEBUG] Inserting timestamp (as millis): " + value);
                        } else if (field.getType() == Location.class) {
                            Location location = (Location) field.get(object);
                            if (location != null) {
                                value = location.getWorld().getName() + ":" + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
                            } else {
                                value = null;
                            }
                        } else if (field.getType() == UUID.class) {
                            UUID uuid = (UUID) field.get(object);
                            value = uuid != null ? uuid.toString() : null;
                        } else {
                            value = field.get(object);
                        }

                        TrustPlugin.getInstance().debug("[DEBUG] Field: " + field.getName() + " = " + value + " (" + (value != null ? value.getClass().getSimpleName() : "null") + ")");
                        values.add(value);
                    }

                    columns.setLength(columns.length() - 1);
                    placeholders.setLength(placeholders.length() - 1);

                    String sql = "INSERT INTO " + table.getTableName() + " (" + columns + ") VALUES (" + placeholders + ")";
                    TrustPlugin.getInstance().debug("[DEBUG] SQL: " + sql);

                    try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                        for (int i = 0; i < values.size(); i++) {
                            pstmt.setObject(i + 1, values.get(i));
                        }
                        pstmt.executeUpdate();
                    }
                } catch (SQLException | IllegalAccessException e) {
                    e.printStackTrace();
                }

                if (callback != null) {
                    Bukkit.getScheduler().runTask(TrustPlugin.getInstance(), callback);
                }
            }
        }.runTaskAsynchronously(TrustPlugin.getInstance());
    }



    public void updateSafeZone(SafeZoneDB safeZone, Runnable callback) {
        new BukkitRunnable() {
            @Override
            public void run() {
                String sql = "UPDATE safe_zone SET server_id = ?, start_chunk_x = ?, end_chunk_x = ?, start_chunk_z = ?, end_chunk_z = ? WHERE player_id = ?";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setString(1, safeZone.server_id);
                    stmt.setInt(2, safeZone.start_chunk_x);
                    stmt.setInt(3, safeZone.end_chunk_x);
                    stmt.setInt(4, safeZone.start_chunk_z);
                    stmt.setInt(5, safeZone.end_chunk_z);
                    stmt.setString(6, safeZone.player_id);
                    stmt.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                // Запускаємо колбек в основному потоці Bukkit
                Bukkit.getScheduler().runTask(TrustPlugin.getInstance(), callback);
            }
        }.runTask(TrustPlugin.getInstance());
    }




    @Override
    public void updatePlayerCommand(UUID playerId, int line){
        new BukkitRunnable() {
            @Override
            public void run() {
                String sql = "UPDATE player_lines SET line = ? WHERE player_id = ?";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setInt(1, line);
                    stmt.setString(2, playerId.toString());
                    stmt.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(TrustPlugin.getInstance());
    }


    @Override
    public void updatePlayerTrust(UUID playerId, double newTrust) {
        new BukkitRunnable() {
            @Override
            public void run() {
                String sql = "UPDATE player_trusts SET score = ? WHERE player_id = ?";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setDouble(1, newTrust);
                    stmt.setString(2, playerId.toString());
                    stmt.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(TrustPlugin.getInstance());
    }

    @Override
    public <T> void searchSinhronizeData(DatabaseTable table, String condition, Class<T> clazz, Consumer<List<T>> callback) {
        new BukkitRunnable() {
            @Override
            public void run() {
                List<T> results = new ArrayList<>();
                String sql = "SELECT * FROM " + table.getTableName() + (condition != null ? " WHERE " + condition : "");

                try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                    Field[] fields = clazz.getDeclaredFields();

                    while (rs.next()) {
                        T obj = clazz.getDeclaredConstructor().newInstance();
                        for (Field field : fields) {
                            field.setAccessible(true);
                            try {
                                Object value = rs.getObject(field.getName());
                                if (field.getType() == Timestamp.class && value instanceof String) {
                                    field.set(obj, Timestamp.valueOf((String) value));
                                } else if (field.getType() == UUID.class && value instanceof String) {
                                    field.set(obj, UUID.fromString((String) value));
                                } else {
                                    field.set(obj, value);
                                }
                            } catch (SQLException | IllegalArgumentException ignored) {
                            }
                        }
                        results.add(obj);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Bukkit.getScheduler().runTask(TrustPlugin.getInstance(), () -> callback.accept(results));
            }
        }.runTask(TrustPlugin.getInstance());
    }

    @Override
    public <T> void searchData(DatabaseTable table, String condition, Class<T> clazz, Consumer<List<T>> callback) {
        new BukkitRunnable() {
            @Override
            public void run() {
                List<T> results = new ArrayList<>();
                String sql = "SELECT * FROM " + table.getTableName() + (condition != null ? " WHERE " + condition : "");

                try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                    Field[] fields = clazz.getDeclaredFields();

                    while (rs.next()) {
                        T obj = clazz.getDeclaredConstructor().newInstance();
                        for (Field field : fields) {
                            field.setAccessible(true);
                            try {
                                Object value = rs.getObject(field.getName());
                                if (field.getType() == Timestamp.class && value instanceof String) {
                                    field.set(obj, Timestamp.valueOf((String) value));
                                } else if (field.getType() == UUID.class && value instanceof String) {
                                    field.set(obj, UUID.fromString((String) value));
                                } else {
                                    field.set(obj, value);
                                }
                            } catch (SQLException | IllegalArgumentException ignored) {
                            }
                        }
                        results.add(obj);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Bukkit.getScheduler().runTask(TrustPlugin.getInstance(), () -> callback.accept(results));
            }
        }.runTaskAsynchronously(TrustPlugin.getInstance());
    }

    @Override
    public <T> void searchDataRaw(String sql, Object[] params, Class<T> clazz, Consumer<List<T>> callback) {
        new BukkitRunnable() {
            @Override
            public void run() {
                List<T> results = new ArrayList<>();
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    for (int i = 0; i < params.length; i++) {
                        stmt.setObject(i + 1, params[i]);
                    }

                    ResultSet rs = stmt.executeQuery();
                    Field[] fields = clazz.getDeclaredFields();

                    while (rs.next()) {
                        T obj = clazz.getDeclaredConstructor().newInstance();
                        for (Field field : fields) {
                            field.setAccessible(true);
                            try {
                                Object value = rs.getObject(field.getName());
                                if (field.getType() == Timestamp.class && value instanceof String) {
                                    field.set(obj, Timestamp.valueOf((String) value));
                                } else if (field.getType() == UUID.class && value instanceof String) {
                                    field.set(obj, UUID.fromString((String) value));
                                } else {
                                    field.set(obj, value);
                                }
                            } catch (SQLException | IllegalArgumentException ignored) {
                            }
                        }
                        results.add(obj);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Bukkit.getScheduler().runTask(TrustPlugin.getInstance(), () -> callback.accept(results));
            }
        }.runTaskAsynchronously(TrustPlugin.getInstance());
    }

    @Override
    public void countRows(String sql, Consumer<Integer> callback) {
        new BukkitRunnable() {
            @Override
            public void run() {
                int count = 0;
                try (PreparedStatement stmt = connection.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        count = rs.getInt(1);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                int finalCount = count;
                Bukkit.getScheduler().runTask(TrustPlugin.getInstance(), () -> callback.accept(finalCount));
            }
        }.runTaskAsynchronously(TrustPlugin.getInstance());
    }

    @Override
    public void countRows(String sql, Object[] params, Consumer<Integer> callback) {
        new BukkitRunnable() {
            @Override
            public void run() {
                int count = 0;
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    for (int i = 0; i < params.length; i++) {
                        stmt.setObject(i + 1, params[i]);
                    }

                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            count = rs.getInt(1);
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                int finalCount = count;
                Bukkit.getScheduler().runTask(TrustPlugin.getInstance(), () -> callback.accept(finalCount));
            }
        }.runTaskAsynchronously(TrustPlugin.getInstance());
    }

    @Override
    public void deleteById(DatabaseTable table, Object id, Runnable onComplete) {
        new BukkitRunnable() {
            @Override
            public void run() {
                String sql = "DELETE FROM " + table.getTableName() + " WHERE id = ?";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setObject(1, id);
                    int affectedRows = stmt.executeUpdate();

                    if (affectedRows > 0) {
                        TrustPlugin.getInstance().debug("[DEBUG] Видалено запис із таблиці " + table.getTableName() + ", ID: " + id);
                    } else {
                        TrustPlugin.getInstance().debug("[DEBUG] Не знайдено запису для видалення у таблиці " + table.getTableName() + ", ID: " + id);
                    }

                    if (onComplete != null) {
                        Bukkit.getScheduler().runTask(TrustPlugin.getInstance(), onComplete);
                    }

                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(TrustPlugin.getInstance());
    }


    @Override
    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}

