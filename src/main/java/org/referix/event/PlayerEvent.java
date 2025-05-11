package org.referix.event;


import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.referix.database.DatabaseManager;
import org.referix.database.DatabaseTable;
import org.referix.database.pojo.PlayerTrustDB;
import org.referix.utils.PlayerDataCache;

public class PlayerEvent implements Listener {
    private DatabaseManager databaseManager;
    private PlayerDataCache playerDataCache;

    public PlayerEvent(DatabaseManager databaseManager, PlayerDataCache playerDataCache) {
        this.databaseManager = databaseManager;
        this.playerDataCache = playerDataCache;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        databaseManager.searchData(DatabaseTable.PLAYER_TRUSTS, "player_id = '" + event.getPlayer().getUniqueId()
                + "' ", PlayerTrustDB.class, playerTrustDBs -> {
            if (playerTrustDBs.isEmpty()){
                PlayerTrustDB playerTrustDB = new PlayerTrustDB(event.getPlayer().getUniqueId(), 0);
                databaseManager.insertDataAsync(DatabaseTable.PLAYER_TRUSTS, playerTrustDB);
            }
            playerDataCache.set(playerTrustDBs.getFirst().getPlayerId(), String.valueOf(Math.floor(playerTrustDBs.getFirst().getScore())));
        });
    }

}
