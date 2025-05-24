package org.referix.savezone;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.referix.database.DatabaseProvider;
import org.referix.database.DatabaseTable;
import org.referix.database.pojo.SafeZoneDB;
import org.referix.trustPlugin.TrustPlugin;
import org.referix.utils.ConfigManager;

import java.util.*;

public class SafeZoneManager {

    private final DatabaseProvider databaseManager;

    public List<SafeZoneDB> getSafeZoneDBS() {
        return safeZoneDBS;
    }

    private List<SafeZoneDB> safeZoneDBS = new ArrayList<>();
    private ConfigManager configManager;


    public SafeZoneManager(DatabaseProvider databaseManager, ConfigManager configManager, Runnable onLoadComplete) {
        this.databaseManager = databaseManager;
        this.configManager = configManager;
        loadSafeZones(onLoadComplete);
    }

    private void loadSafeZones(Runnable onComplete) {
        int startX = configManager.defaultSaveZoneStartX();
        int endX = configManager.defaultSaveZoneEndX();
        int startZ = configManager.defaultSaveZoneStartZ();
        int endZ = configManager.defaultSaveZoneEndZ();

        databaseManager.searchData(
                DatabaseTable.SAFE_ZONE,
                "server_id = '" + TrustPlugin.getInstance().getServerID() + "' AND player_id = 'server'",
                SafeZoneDB.class,
                list -> {
                    SafeZoneDB zone = new SafeZoneDB(TrustPlugin.getInstance().getServerID(), "server", startX, endX, startZ, endZ);
                    if (list.isEmpty()) {
                        // Вставляємо дефолтну зону, після чого завантажуємо список
                        databaseManager.insertDataAsync(DatabaseTable.SAFE_ZONE, zone, () -> loadSafeZoneList(onComplete));
                    } else {
                        Bukkit.getLogger().info("UPDATE SAFE ZONE DEFAULT");
                        databaseManager.updateSafeZone(zone, () -> loadSafeZoneList(onComplete));
                    }
                }
        );
    }

    private void loadSafeZoneList(Runnable onComplete) {
        databaseManager.searchData(
                DatabaseTable.SAFE_ZONE,
                "server_id = '" + TrustPlugin.getInstance().getServerID() + "'",
                SafeZoneDB.class,
                list -> {
                    this.safeZoneDBS = list;
                    logSafeZones();
                    onComplete.run();
                }
        );
    }

    private void logSafeZones() {
        TrustPlugin.getInstance().debug("[DEBUG] Завантажено безпечні зони:");
        for (SafeZoneDB zone : safeZoneDBS) {
            int minX = Math.min(zone.start_chunk_x, zone.end_chunk_x);
            int maxX = Math.max(zone.start_chunk_x, zone.end_chunk_x);
            int minZ = Math.min(zone.start_chunk_z, zone.end_chunk_z);
            int maxZ = Math.max(zone.start_chunk_z, zone.end_chunk_z);

            int minBlockX = minX * 16;
            int maxBlockX = maxX * 16 + 15;
            int minBlockZ = minZ * 16;
            int maxBlockZ = maxZ * 16 + 15;

            TrustPlugin.getInstance().debug("  - Чанки: X [" + minX + " → " + maxX + "], Z [" + minZ + " → " + maxZ + "]");
            TrustPlugin.getInstance().debug("  - Блоки: X [" + minBlockX + " → " + maxBlockX + "], Z [" + minBlockZ + " → " + maxBlockZ + "]");
        }
    }




    /**
     * Перевіряє, чи знаходиться координати у безпечній зоні для будь-якого гравця
     */
    public boolean isSafeZone(Location location) {
        int chunkX = location.getBlockX() >> 4;
        int chunkZ = location.getBlockZ() >> 4;
        TrustPlugin.getInstance().debug("SIZE: " + safeZoneDBS.size());

        for (SafeZoneDB zone : safeZoneDBS) {
            int minX = Math.min(zone.start_chunk_x, zone.end_chunk_x);
            int maxX = Math.max(zone.start_chunk_x, zone.end_chunk_x);
            int minZ = Math.min(zone.start_chunk_z, zone.end_chunk_z);
            int maxZ = Math.max(zone.start_chunk_z, zone.end_chunk_z);

            if (chunkX >= minX && chunkX <= maxX &&
                    chunkZ >= minZ && chunkZ <= maxZ) {
                TrustPlugin.getInstance().debug("[DEBUG] Локація потрапила в безпечну зону!");
                return true;
            }
        }

        TrustPlugin.getInstance().debug("[DEBUG] Локація не в безпечній зоні.");
        return false;
    }


    public boolean isPlayerSafeZone(Location location, Player p) {
        int chunkX = location.getBlockX() >> 4;
        int chunkZ = location.getBlockZ() >> 4;
        TrustPlugin.getInstance().debug("SIZE: " + safeZoneDBS.size());

        for (SafeZoneDB zone : safeZoneDBS) {
            int minX = Math.min(zone.start_chunk_x, zone.end_chunk_x);
            int maxX = Math.max(zone.start_chunk_x, zone.end_chunk_x);
            int minZ = Math.min(zone.start_chunk_z, zone.end_chunk_z);
            int maxZ = Math.max(zone.start_chunk_z, zone.end_chunk_z);

            if (chunkX >= minX && chunkX <= maxX &&
                    chunkZ >= minZ && chunkZ <= maxZ &&
                    zone.player_id.equals(p.getUniqueId().toString())) {

                TrustPlugin.getInstance().debug("[DEBUG] Локація потрапила в безпечну зону гравця: " + p.getName());
                return true;
            }
        }

        TrustPlugin.getInstance().debug("[DEBUG] Локація не в безпечній зоні для гравця: " + p.getName());
        return false;
    }


}
