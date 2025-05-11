package org.referix.database.pojo;

import java.util.UUID;

public class PlayerTrustDB {

    private UUID player_id;
    private double score;

    public PlayerTrustDB() {}

    public PlayerTrustDB(UUID playerId, double score) {
        this.player_id = playerId;
        this.score = score;
    }

    public UUID getPlayerId() {
        return player_id;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "PlayerTrustDB{" +
                "playerId=" + player_id +
                ", score=" + score +
                '}';
    }
}
