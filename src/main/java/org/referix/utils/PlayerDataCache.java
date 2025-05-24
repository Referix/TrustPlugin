package org.referix.utils;

import org.bukkit.Bukkit;
import org.referix.database.DatabaseProvider;
import org.referix.database.DatabaseTable;
import org.referix.database.pojo.PlayerTrustDB;
import org.referix.trustPlugin.TrustPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataCache {
    private final Map<UUID, String> playerTrustData = new ConcurrentHashMap<>();
    private ConfigManager configManager;
    private boolean loaded = false;

    public PlayerDataCache(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public void loadCacheAsync(DatabaseProvider databaseManager, Runnable callback) {
        databaseManager.searchData(DatabaseTable.PLAYER_TRUSTS, null, PlayerTrustDB.class, list -> {
            for (PlayerTrustDB db : list) {
                playerTrustData.put(db.getPlayerId(), String.valueOf(Math.floor(db.getScore())));
            }
            loaded = true;
            // Виконуємо колбек у головному потоці Bukkit
            Bukkit.getScheduler().runTask(TrustPlugin.getInstance(), callback);
        });
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void set(UUID uuid, String data) {
        playerTrustData.put(uuid, data);
    }

    public double getScore(UUID uuid) {
        return Double.parseDouble(playerTrustData.getOrDefault(uuid, "0"));
    }

    public boolean isTrusted(UUID uuid) {
        double target_score = getScore(uuid);
        double trusted_score = configManager.getDownSafeZone();
        System.out.println("target_score " + target_score + " trusted_score " + trusted_score);
        return target_score >= trusted_score;
    }

    public String get(UUID uuid) {
        return playerTrustData.getOrDefault(uuid, "N/A");
    }
}
