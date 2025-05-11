package org.referix.utils;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataCache {
    private final Map<UUID, String> playerTrustData = new ConcurrentHashMap<>();

    public void set(UUID uuid, String data) {
        playerTrustData.put(uuid, data);
    }

    public String get(UUID uuid) {
        return playerTrustData.getOrDefault(uuid, "N/A");
    }
}

