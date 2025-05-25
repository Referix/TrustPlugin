package org.referix.commands.command.admincommand;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.referix.database.DatabaseTable;
import org.referix.database.pojo.PlayerTrustDB;
import org.referix.database.pojo.SafeZoneDB;
import org.referix.savezone.SafeZoneManager;
import org.referix.trustPlugin.TrustPlugin;

import static org.referix.event.ReputationListener.sendToVelocityCache;

public class SafeZoneDelete implements HelperCommand{
    private SafeZoneManager safeZoneManager;

    public SafeZoneDelete(Player p, String[] a, SafeZoneManager safeZoneManager) {
        this.safeZoneManager = safeZoneManager;
        execute(p,  a);

    }

    @Override
    public boolean execute(Player p, String[] args) {
        if (args.length < 2) TrustPlugin.getInstance().getConfigManager().getMessage("no_correctly_command");
        if (args.length == 2) {
            if (safeZoneManager.isSafeZone(p.getLocation())){
                SafeZoneDB safeZoneAt = safeZoneManager.getSafeZoneAt(p.getLocation());
                if (safeZoneAt == null) {
                    p.sendMessage("This SafeZone no found!");
                    return false;
                }
                if (safeZoneAt.getPlayer_id().equals("server")){
                    p.sendMessage("This Server SafeZone!");
                    return false;
                }
                safeZoneManager.deleteSafeZone(safeZoneAt,() ->{
                    p.sendMessage("SafeZone deleted!");
                });

            }
        }
        return false;
    }
}
