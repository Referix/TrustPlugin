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
import java.util.concurrent.ConcurrentHashMap;

public class SafeZoneManager {

    private final DatabaseProvider databaseManager;
    private final Map<String, List<ZoneRectangle>> playerZones = new ConcurrentHashMap<>();

    public List<SafeZoneDB> getSafeZoneDBS() {
        return safeZoneDBS;
    }

    private List<SafeZoneDB> safeZoneDBS = new ArrayList<>();
    private ConfigManager configManager;


    public SafeZoneManager(DatabaseProvider databaseManager, ConfigManager configManager) {
        this.databaseManager = databaseManager;
        this.configManager = configManager;
        loadSafeZones();
    }

    private void loadSafeZones() {
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
                        databaseManager.insertDataAsync(DatabaseTable.SAFE_ZONE, zone, () -> {
                            // Після вставки — завантажуємо список
                            loadSafeZoneList();
                        });
                    } else {
                        System.out.println(startX + " " + endX + " " + startZ + " " + endZ);
                        System.out.println("UPDATE SAFE ZONE DEFAULT");
                        databaseManager.updateSafeZone(zone, () -> {
                            // Після оновлення — завантажуємо список
                            loadSafeZoneList();
                        });
                    }
                }
        );
    }

    private void loadSafeZoneList() {
        databaseManager.searchData(
                DatabaseTable.SAFE_ZONE,
                "server_id = '" + TrustPlugin.getInstance().getServerID() + "'",
                SafeZoneDB.class,
                list -> {
                    this.safeZoneDBS = list;

                    System.out.println("[DEBUG] Завантажено безпечні зони:");
                    for (SafeZoneDB zone : safeZoneDBS) {
                        int minX = Math.min(zone.start_chunk_x, zone.end_chunk_x);
                        int maxX = Math.max(zone.start_chunk_x, zone.end_chunk_x);
                        int minZ = Math.min(zone.start_chunk_z, zone.end_chunk_z);
                        int maxZ = Math.max(zone.start_chunk_z, zone.end_chunk_z);

                        int minBlockX = minX * 16;
                        int maxBlockX = maxX * 16 + 15;
                        int minBlockZ = minZ * 16;
                        int maxBlockZ = maxZ * 16 + 15;

                        System.out.println("  - Чанки: X [" + minX + " → " + maxX + "], Z [" + minZ + " → " + maxZ + "]");
                        System.out.println("  - Блоки: X [" + minBlockX + " → " + maxBlockX + "], Z [" + minBlockZ + " → " + maxBlockZ + "]");
                    }
                }
        );
    }




    /**
     * Перевіряє, чи знаходиться координати у безпечній зоні для будь-якого гравця
     */
    public boolean isSafeZone(Location location) {
        int chunkX = location.getBlockX() >> 4;
        int chunkZ = location.getBlockZ() >> 4;
        System.out.println("SIZE: " + safeZoneDBS.size());

        for (SafeZoneDB zone : safeZoneDBS) {
            int minX = Math.min(zone.start_chunk_x, zone.end_chunk_x);
            int maxX = Math.max(zone.start_chunk_x, zone.end_chunk_x);
            int minZ = Math.min(zone.start_chunk_z, zone.end_chunk_z);
            int maxZ = Math.max(zone.start_chunk_z, zone.end_chunk_z);

            if (chunkX >= minX && chunkX <= maxX &&
                    chunkZ >= minZ && chunkZ <= maxZ) {
                System.out.println("[DEBUG] Локація потрапила в безпечну зону!");
                return true;
            }
        }

        System.out.println("[DEBUG] Локація не в безпечній зоні.");
        return false;
    }


    public boolean isPlayerSafeZone(Location location, Player p) {
        int chunkX = location.getBlockX() >> 4;
        int chunkZ = location.getBlockZ() >> 4;
        System.out.println("SIZE: " + safeZoneDBS.size());

        for (SafeZoneDB zone : safeZoneDBS) {
            int minX = Math.min(zone.start_chunk_x, zone.end_chunk_x);
            int maxX = Math.max(zone.start_chunk_x, zone.end_chunk_x);
            int minZ = Math.min(zone.start_chunk_z, zone.end_chunk_z);
            int maxZ = Math.max(zone.start_chunk_z, zone.end_chunk_z);

            if (chunkX >= minX && chunkX <= maxX &&
                    chunkZ >= minZ && chunkZ <= maxZ &&
                    zone.player_id.equals(p.getUniqueId().toString())) {

                System.out.println("[DEBUG] Локація потрапила в безпечну зону гравця: " + p.getName());
                return true;
            }
        }

        System.out.println("[DEBUG] Локація не в безпечній зоні для гравця: " + p.getName());
        return false;
    }



    /**
     * Приватний допоміжний клас для зберігання діапазонів
     */
    private static class ZoneRectangle {
        final int minX, maxX;
        final int minZ, maxZ;

        ZoneRectangle(int x1, int x2, int z1, int z2) {
            this.minX = Math.min(x1, x2);
            this.maxX = Math.max(x1, x2);
            this.minZ = Math.min(z1, z2);
            this.maxZ = Math.max(z1, z2);
        }

        boolean isInside(int x, int z) {
            return x >= minX && x <= maxX && z >= minZ && z <= maxZ;
        }
    }

}
