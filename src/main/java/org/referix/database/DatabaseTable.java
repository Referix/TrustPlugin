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
            "id {ID_TYPE}, `target_id` CHAR(36) NOT NULL, `actor_id` CHAR(36) NOT NULL, `change` DOUBLE NOT NULL, `reason` TEXT, `timestamp` BIGINT NOT NULL"
    ),

    PLAYER_LINES(
            "player_lines",
            "id {ID_TYPE}, `player_id` CHAR(36) NOT NULL, `line` INT NOT NULL"
    ),

    SAFE_ZONE(
            "safe_zone",
            "id {ID_TYPE}, `server_id` VARCHAR(64) NOT NULL, `player_id` CHAR(36) NOT NULL, `start_chunk_x` INT NOT NULL, `end_chunk_x` INT NOT NULL, `start_chunk_z` INT NOT NULL, `end_chunk_z` INT NOT NULL"
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

        if (!isMySQL) {
            cols = cols.replace("`", "");
        }

        return cols;
    }

    /**
     * Повертає повний SQL для створення таблиці з урахуванням СУБД та кодування
     */
    public String getCreateTableSQL() {
        FileConfiguration cfg = TrustPlugin.getInstance().getConfig();
        boolean isMySQL = "mysql".equalsIgnoreCase(cfg.getString("database.type", "sqlite"));

        // Підставляємо ID_TYPE
        String idType = isMySQL
                ? "INT PRIMARY KEY AUTO_INCREMENT"
                : "INTEGER PRIMARY KEY AUTOINCREMENT";
        // Підготовка колонок з {ID_TYPE}
        String cols = columns.replace("{ID_TYPE}", idType);
        // Для SQLite прибираємо зворотні лапки в іменах колонок
        if (!isMySQL) {
            cols = cols.replace("`", "");
        }

        // Побудова CREATE TABLE
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE IF NOT EXISTS ");
        if (isMySQL) {
            sb.append('`').append(tableName).append('`');
        } else {
            sb.append(tableName);
        }
        sb.append(" (").append(cols).append(")");

        // Додаємо параметри кодування і мотор для MySQL
        if (isMySQL) {
            sb.append(" ENGINE=InnoDB")
                    .append(" DEFAULT CHARSET=utf8mb4")
                    .append(" COLLATE=utf8mb4_unicode_ci");
        }

        return sb.toString();
    }
}
