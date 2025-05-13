package org.referix.event;


import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.referix.database.DatabaseProvider;
import org.referix.database.DatabaseTable;
import org.referix.database.pojo.PlayerTrustDB;
import org.referix.utils.PlayerDataCache;

public class PlayerEvent implements Listener {
    private DatabaseProvider databaseManager;
    private PlayerDataCache playerDataCache;

    public PlayerEvent(DatabaseProvider databaseManager, PlayerDataCache playerDataCache) {
        this.databaseManager = databaseManager;
        this.playerDataCache = playerDataCache;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        databaseManager.searchData(DatabaseTable.PLAYER_TRUSTS, "player_id = '" + event.getPlayer().getUniqueId() + "'",
                PlayerTrustDB.class, playerTrustDBs -> {
                    if (playerTrustDBs.isEmpty()) {
                        PlayerTrustDB playerTrustDB = new PlayerTrustDB(event.getPlayer().getUniqueId(), 50);
                        databaseManager.insertDataAsync(DatabaseTable.PLAYER_TRUSTS, playerTrustDB);
                    } else {
                        // Якщо дані знайдено, обробляємо перший елемент
                        PlayerTrustDB playerTrustDB = playerTrustDBs.getFirst();
                        playerDataCache.set(playerTrustDB.getPlayerId(), String.valueOf(Math.floor(playerTrustDB.getScore())));
                    }
                });
    }


}
