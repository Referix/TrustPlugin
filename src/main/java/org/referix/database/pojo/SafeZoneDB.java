package org.referix.database.pojo;


import java.util.Objects;

public class SafeZoneDB {

    public int id;

    public String server_id;

    public String player_id;
    public int start_chunk_x;
    public int end_chunk_x;
    public int start_chunk_z;
    public int end_chunk_z;
    public SafeZoneDB() {
        // Потрібен для рефлексії
    }

    // Опціонально: зручний конструктор

    public SafeZoneDB(String serverId, String playerId, int x1, int x2, int z1, int z2) {
        this.server_id = serverId;
        this.player_id = playerId;
        this.start_chunk_x = x1;
        this.end_chunk_x = x2;
        this.start_chunk_z = z1;
        this.end_chunk_z = z2;
    }



    // Геттери і сеттери
    public int getId() {
    return id;
    }

    public String getServer_id() {
        return server_id;
    }

    public String getPlayer_id() {
        return player_id;
    }

    public int getStart_chunk_x() {
        return start_chunk_x;
    }

    public int getEnd_chunk_x() {
        return end_chunk_x;
    }

    public int getStart_chunk_z() {
        return start_chunk_z;
    }

    public int getEnd_chunk_z() {
        return end_chunk_z;
    }
}

