package org.referix.commands.command.admincommand;

import org.bukkit.entity.Player;
import org.referix.database.DatabaseProvider;
import org.referix.database.DatabaseTable;
import org.referix.database.pojo.PlayerTrustDB;
import org.referix.trustPlugin.TrustPlugin;

import java.util.UUID;

public class AdminRemoveTrust implements HelperCommand{


    private UUID targetPlayerID;
    private DatabaseProvider databaseManager;

    public AdminRemoveTrust(Player p, String[] args, UUID targetPlayerID, DatabaseProvider databaseManager) {
        this.targetPlayerID = targetPlayerID;
        this.databaseManager = databaseManager;
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
                            double newScore = playerTrustDBs.getFirst().getScore() - Double.parseDouble(args[2]);
                            databaseManager.updatePlayerTrust(targetPlayerID, newScore);
                            p.sendMessage(TrustPlugin.getInstance().getConfigManager().getMessage("trust_removed","score" , args[2]));
                        }

                    });
            return true;
        }
        return false;
    }
}
