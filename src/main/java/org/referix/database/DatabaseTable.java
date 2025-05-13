package org.referix.database;

import org.referix.trustPlugin.TrustPlugin;
import org.bukkit.configuration.file.FileConfiguration;

public enum DatabaseTable {

    PLAYER_TRUSTS(
            "player_trusts",
            "id {ID_TYPE}, `player_id` CHAR(36) NOT NULL, `score` DOUBLE NOT NULL"
    ),

    TRUST_CHANGES(
            "trust_changes",
            "id {ID_TYPE}, " +
                    "`target_id` CHAR(36) NOT NULL, " +
                    "`actor_id` CHAR(36) NOT NULL, " +
                    "`change` DOUBLE NOT NULL, " +
                    "`reason` TEXT, " +
                    "`timestamp` BIGINT NOT NULL"
    );

    private final String tableName;
    private final String columns;

    DatabaseTable(String tableName, String columns) {
        this.tableName = tableName;
        this.columns = columns;
    }

    public String getTableName() {
        return tableName;
    }

    public String getColumns() {
        FileConfiguration cfg = TrustPlugin.getInstance().getConfig();
        boolean isMySQL = "mysql".equalsIgnoreCase(cfg.getString("database.type", "sqlite"));
        String idType = isMySQL
                ? "INT PRIMARY KEY AUTO_INCREMENT"
                : "INTEGER PRIMARY KEY AUTOINCREMENT";
        String cols = columns.replace("{ID_TYPE}", idType);
        // для SQLite прибираємо зворотні лапки
        if (!isMySQL) {
            cols = cols.replace("`", "");
        }
        return cols;
    }
}
