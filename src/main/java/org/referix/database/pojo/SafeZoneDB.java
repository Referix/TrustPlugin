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


    public void setStart_chunk_x(int start_chunk_x) {
        this.start_chunk_x = start_chunk_x;
    }

    public void setEnd_chunk_x(int end_chunk_x) {
        this.end_chunk_x = end_chunk_x;
    }

    public void setStart_chunk_z(int start_chunk_z) {
        this.start_chunk_z = start_chunk_z;
    }

    public void setEnd_chunk_z(int end_chunk_z) {
        this.end_chunk_z = end_chunk_z;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SafeZoneDB that = (SafeZoneDB) o;
        return start_chunk_x == that.start_chunk_x &&
                end_chunk_x == that.end_chunk_x &&
                start_chunk_z == that.start_chunk_z &&
                end_chunk_z == that.end_chunk_z &&
                Objects.equals(server_id, that.server_id) &&
                Objects.equals(player_id, that.player_id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(server_id, player_id, start_chunk_x, end_chunk_x, start_chunk_z, end_chunk_z);
    }

}

