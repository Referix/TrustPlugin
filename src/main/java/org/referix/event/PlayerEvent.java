package org.referix.event;


import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.referix.database.DatabaseProvider;
import org.referix.database.DatabaseTable;
import org.referix.database.pojo.PlayerTrustDB;
import org.referix.trustPlugin.TrustPlugin;
import org.referix.utils.PlayerDataCache;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerEvent implements Listener {
    private DatabaseProvider databaseManager;
    private PlayerDataCache playerDataCache;

    public PlayerEvent(DatabaseProvider databaseManager, PlayerDataCache playerDataCache) {
        this.databaseManager = databaseManager;
        this.playerDataCache = playerDataCache;
        startBackupTask();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        databaseManager.searchData(DatabaseTable.PLAYER_TRUSTS, "player_id = '" + event.getPlayer().getUniqueId() + "'",
                PlayerTrustDB.class, playerTrustDBs -> {
                    if (playerTrustDBs.isEmpty()) {
                        PlayerTrustDB playerTrustDB = new PlayerTrustDB(event.getPlayer().getUniqueId(), TrustPlugin.getInstance().getConfigManager().getDefaultTrustFirstJoin());
                        databaseManager.insertDataAsync(DatabaseTable.PLAYER_TRUSTS, playerTrustDB, null);
                    } else {
                        // Якщо дані знайдено, обробляємо перший елемент
                        PlayerTrustDB playerTrustDB = playerTrustDBs.getFirst();
                        playerDataCache.set(playerTrustDB.getPlayerId(), String.valueOf(Math.floor(playerTrustDB.getScore())));
                    }
                });
    }
    private void startBackupTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getLogger().info("[TrustPlugin] Running hourly backup rumble...");

                databaseManager.searchData(DatabaseTable.PLAYER_TRUSTS, null, PlayerTrustDB.class, playerTrustDBs -> {
                    // Створюємо мапу для швидкого доступу
                    Map<UUID, PlayerTrustDB> trustMap = new HashMap<>();
                    for (PlayerTrustDB trustDB : playerTrustDBs) {
                        trustMap.put(trustDB.getPlayerId(), trustDB);
                    }

                    // Перебираємо лише онлайн гравців
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        PlayerTrustDB trustDB = trustMap.get(player.getUniqueId());
                        if (trustDB != null) {
                            double newScore = trustDB.getScore() + TrustPlugin.getInstance().getConfigManager().getHourAdd();
                            databaseManager.updatePlayerTrust(trustDB.getPlayerId(), newScore);
                        }
                    }
                });
            }
        }.runTaskTimerAsynchronously(TrustPlugin.getInstance(), 0L, 3600L * 20); // раз на годину
    }


}
