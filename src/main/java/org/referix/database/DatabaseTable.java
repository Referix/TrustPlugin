package org.referix.database;

public enum DatabaseTable {

    PLAYER_TRUSTS(
            "player_trusts",
            "id INTEGER PRIMARY KEY AUTOINCREMENT, player_id TEXT, score REAL"
    ),

    TRUST_CHANGES(
            "trust_changes",
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "target_id TEXT NOT NULL, " +
                    "actor_id TEXT NOT NULL, " +
                    "change REAL NOT NULL, " +
                    "reason TEXT, " +
                    "timestamp INTEGER NOT NULL"
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
        return columns;
    }
}
