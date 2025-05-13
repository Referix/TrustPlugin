package org.referix.database;


import org.bukkit.configuration.file.FileConfiguration;
import org.referix.trustPlugin.TrustPlugin;

public class DatabaseFactory {
    public static DatabaseProvider createDatabase() {
        FileConfiguration config = TrustPlugin.getInstance().getConfig();
        String type = config.getString("database.type", "sqlite").toLowerCase();

        if (type.equals("mysql")) {
            String host = config.getString("database.mysql.host");
            int port = config.getInt("database.mysql.port");
            String database = config.getString("database.mysql.database");
            String user = config.getString("database.mysql.user");
            String password = config.getString("database.mysql.password");
            return new MySQLDatabaseProvider(host, port, database, user, password);
        }

        String path = TrustPlugin.getInstance().getDataFolder() + "/" + config.getString("database.sqlite.file", "database.db");
        return new SQLiteDatabaseProvider(path);
    }
}

