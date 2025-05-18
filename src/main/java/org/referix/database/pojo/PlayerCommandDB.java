package org.referix.database.pojo;

import java.util.UUID;

public class PlayerCommandDB {
    private UUID player_id;
    private int line;

    public PlayerCommandDB() {}

    public PlayerCommandDB(UUID playerId, int line) {
        player_id = playerId;
        this.line = line;
    }

    public int getLine() {
        return line;
    }

    public UUID getPlayer_id() {
        return player_id;
    }
}
