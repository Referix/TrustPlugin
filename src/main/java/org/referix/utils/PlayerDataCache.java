package org.referix.utils;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataCache {
    private final Map<UUID, String> playerTrustData = new ConcurrentHashMap<>();
    private ConfigManager configManager;

    public PlayerDataCache(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public void set(UUID uuid, String data) {
        playerTrustData.put(uuid, data);

    }

    public double getScore(UUID uuid) {
        return Double.parseDouble(playerTrustData.getOrDefault(uuid, "0"));
    }

    public boolean isTrusted(UUID uuid) {
        double target_score = getScore(uuid);
        System.out.println("target_score " + target_score + " trusted_score " + configManager.getTrustLineScore());
        double trusted_score = configManager.getTrustLineScore();
        return target_score >= trusted_score;
    }

    public String get(UUID uuid) {
        return playerTrustData.getOrDefault(uuid, "N/A");
    }
}

