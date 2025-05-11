package org.referix.database;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;
import org.referix.trustPlugin.TrustPlugin;

import java.io.File;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class DatabaseManager {
    private Connection connection;

    public DatabaseManager(String dbPath) {
        try {
            Class.forName("org.sqlite.JDBC");

            File dbFile = new File(dbPath);
            if (!dbFile.exists()) {
                dbFile.getParentFile().mkdirs();
                dbFile.createNewFile();
            }

            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createTable(DatabaseTable table) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("CREATE TABLE IF NOT EXISTS " + table.getTableName() + " (" + table.getColumns() + ")");
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(TrustPlugin.getInstance());
    }

    public <T> void insertDataAsync(DatabaseTable table, T object) {
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
                        if ("id".equals(field.getName())) {
                            continue;
                        }
                        // Ігноруємо поле log_timestamp
                        if ("log_timestamp".equals(field.getName())) {
                            continue;  // Пропускаємо це поле
                        }
                        columns.append(field.getName()).append(",");
                        placeholders.append("?,");

                        if (field.getType() == Timestamp.class) {
                            values.add(new Timestamp(System.currentTimeMillis())); // Записуємо поточний час, якщо поле - це Timestamp
                        }
                        if (field.getType() == Location.class) {
                            Location location = (Location) field.get(object);
                            if (location != null) {
                                // Форматування Location в строку (тільки координати та світ)
                                String locationString = location.getWorld().getName() + ":" + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
                                values.add(locationString);  // Додаємо рядок, що представляє локацію
                            } else {
                                values.add(null); // Якщо Location == null, додаємо null
                            }
                        }
                        else {
                            values.add(field.get(object));
                        }
                    }

                    columns.setLength(columns.length() - 1); // Видалення останньої коми
                    placeholders.setLength(placeholders.length() - 1); // Видалення останньої коми

                    String sql = "INSERT INTO " + table.getTableName() + " (" + columns + ") VALUES (" + placeholders + ")";
                    try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                        for (int i = 0; i < values.size(); i++) {
                            pstmt.setObject(i + 1, values.get(i));
                        }
                        pstmt.executeUpdate();
                    }
                } catch (SQLException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(TrustPlugin.getInstance()); // Запуск асинхронного потоку
    }

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



    public <T> void searchData(DatabaseTable table, String condition, Class<T> clazz, Consumer<List<T>> callback) {
        new BukkitRunnable() {
            @Override
            public void run() {
                List<T> results = new ArrayList<>();
                try (Statement stmt = connection.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT * FROM " + table.getTableName() + (condition != null ? " WHERE " + condition : ""))) {

                    Field[] fields = clazz.getDeclaredFields();

                    while (rs.next()) {
                        T obj = clazz.getDeclaredConstructor().newInstance();
                        for (Field field : fields) {
                            field.setAccessible(true);
                            try {
                                if (field.getType() == Timestamp.class) {
                                    // Якщо поле є типом Timestamp, конвертуємо рядок в Timestamp
                                    String timestampString = rs.getString(field.getName());
                                    if (timestampString != null) {
                                        field.set(obj, Timestamp.valueOf(timestampString));
                                    }
                                }
                                else if (field.getType() == UUID.class) {
                                    String uuidString = rs.getString(field.getName());
                                    if (uuidString != null) {
                                        field.set(obj, UUID.fromString(uuidString));
                                    }
                                }
                                else {

                                        // Для всіх інших типів
                                        field.set(obj, rs.getObject(field.getName()));
                                    }
                            } catch (SQLException e) {
                                System.err.println("Поле " + field.getName() + " не знайдено в результаті запиту.");
                            } catch (IllegalArgumentException e) {
                                System.err.println("Помилка при встановленні значення для поля " + field.getName());
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
                                if (field.getType() == Timestamp.class) {
                                    String timestampString = rs.getString(field.getName());
                                    if (timestampString != null) {
                                        field.set(obj, Timestamp.valueOf(timestampString));
                                    }
                                }
                                else if (field.getType() == UUID.class) {
                                    String uuidString = rs.getString(field.getName());
                                    if (uuidString != null) {
                                        field.set(obj, UUID.fromString(uuidString));
                                    }
                                }
                                else {
                                    field.set(obj, rs.getObject(field.getName()));
                                }
                            } catch (SQLException e) {
                                System.err.println("Поле " + field.getName() + " не знайдено в результаті запиту.");
                            } catch (IllegalArgumentException e) {
                                System.err.println("Помилка при встановленні значення для поля " + field.getName());
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

    public void countRows(String sql, Consumer<Integer> callback) {
        new BukkitRunnable() {
            @Override
            public void run() {
                int count = 0;
                try (PreparedStatement stmt = connection.prepareStatement(sql);
                     ResultSet rs = stmt.executeQuery()) {

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

    public void deleteById(DatabaseTable table, Object id) {
        new BukkitRunnable() {
            @Override
            public void run() {
                String sql = "DELETE FROM " + table.getTableName() + " WHERE id = ?";
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setObject(1, id);
                    stmt.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(TrustPlugin.getInstance());
    }


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