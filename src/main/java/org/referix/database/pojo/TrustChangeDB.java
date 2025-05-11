package org.referix.database.pojo;


import java.util.UUID;

public class TrustChangeDB {

    private int id; // ID в базі, може бути 0 для нового запису
    private UUID target_id;  // кому змінювали довіру
    private UUID actor_id;   // хто змінював
    private double change;  // зміна довіри (+ або -)
    private String reason;  // причина
    private long timestamp; // час у мілісекундах

    public TrustChangeDB() {}

    public TrustChangeDB(UUID targetId, UUID actor_id, double change, String reason, long timestamp) {
        this.target_id = targetId;
        this.actor_id = actor_id;
        this.change = change;
        this.reason = reason;
        this.timestamp = timestamp;
    }


    public int getId() {
        return id;
    }

    public UUID getTarget_id() {
        return target_id;
    }

    public UUID getActor_id() {
        return actor_id;
    }

    public double getChange() {
        return change;
    }

    public String getReason() {
        return reason;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "TrustChangeDB{" +
                "id=" + id +
                ", targetId=" + target_id +
                ", actorId=" + actor_id +
                ", change=" + change +
                ", reason='" + reason + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
