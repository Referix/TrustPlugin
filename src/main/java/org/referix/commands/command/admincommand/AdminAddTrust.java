package org.referix.commands.command.admincommand;

import org.bukkit.entity.Player;
import org.referix.database.DatabaseProvider;
import org.referix.database.DatabaseTable;
import org.referix.database.pojo.PlayerTrustDB;
import org.referix.trustPlugin.TrustPlugin;
import org.referix.utils.PlayerDataCache;

import java.util.UUID;

public class AdminAddTrust implements HelperCommand {
    private UUID targetPlayerID;
    private DatabaseProvider databaseManager;
    private PlayerDataCache playerDataCache;

    public AdminAddTrust(Player p, String[] args, UUID targetPlayerID, DatabaseProvider databaseManager, PlayerDataCache playerDataCache) {
        this.targetPlayerID = targetPlayerID;
        this.databaseManager = databaseManager;
        this.playerDataCache = playerDataCache;
        execute(p,args);
    }

    @Override
    public boolean execute(Player p, String[] args) {
        if (args.length < 3) TrustPlugin.getInstance().getConfigManager().getMessage("no_correctly_command");
        if (args.length == 3 && targetPlayerID != null && !args[2].isEmpty()) {

            databaseManager.searchData(DatabaseTable.PLAYER_TRUSTS,"player_id = '" + targetPlayerID + "'", PlayerTrustDB.class,
                    playerTrustDBs -> {
                if (playerTrustDBs.isEmpty()) p.sendMessage(TrustPlugin.getInstance().getConfigManager().getMessage("reputation_not_found"));
                else {
                    double newScore = playerTrustDBs.getFirst().getScore() + Double.parseDouble(args[2]);
                    databaseManager.updatePlayerTrust(targetPlayerID, newScore);
                    if (playerDataCache.isLoaded()){
                        playerDataCache.set(playerTrustDBs.getFirst().getPlayerId(), String.valueOf(Math.floor(newScore)));
                        p.sendMessage(TrustPlugin.getInstance().getConfigManager().getMessage("trust_added","score" , args[2]));
                    }
                }

            });
            return true;
        }
        return false;
    }
}
