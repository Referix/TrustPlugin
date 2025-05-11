package org.referix.event;


import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.referix.database.DatabaseManager;
import org.referix.database.DatabaseTable;
import org.referix.database.pojo.PlayerTrustDB;

public class PlayerEvent implements Listener {
    private DatabaseManager databaseManager;

    public PlayerEvent(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        databaseManager.searchData(DatabaseTable.PLAYER_TRUSTS, "player_id = '" + event.getPlayer().getUniqueId()
                + "' ", PlayerTrustDB.class, playerTrustDBs -> {
            if (playerTrustDBs.isEmpty()){
                PlayerTrustDB playerTrustDB = new PlayerTrustDB(event.getPlayer().getUniqueId(), 0);
                databaseManager.insertDataAsync(DatabaseTable.PLAYER_TRUSTS, playerTrustDB);
            }
        });
    }

}
