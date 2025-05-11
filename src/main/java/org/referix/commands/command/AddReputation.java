package org.referix.commands.command;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.referix.commands.AbstractCommand;
import org.referix.database.DatabaseManager;
import org.referix.database.DatabaseTable;
import org.referix.database.pojo.PlayerTrustDB;
import org.referix.database.pojo.TrustChangeDB;

import java.util.List;

public class AddReputation extends AbstractCommand {

    private final DatabaseManager databaseManager;

    public AddReputation(String command, DatabaseManager databaseManager) {
        super(command);
        this.databaseManager = databaseManager;
    }
    // /+rep {target} {res}
    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if ( args.length != 2) {
            sender.sendMessage("ну коректно");
            return true;
        } else if (!sender.hasPermission("trust.addrep")) {
            sender.hasPermission("no have permission");
            return true;
        } else if (!(sender instanceof Player)) {
            sender.sendMessage("only players command");
        }
        Player target = Bukkit.getPlayer(args[0]);
        Player actor = (Player) sender;
        if (target == null) return true;

        TrustChangeDB trustChangeDB = new TrustChangeDB(target.getUniqueId(), actor.getUniqueId(), 10, args[1], System.currentTimeMillis());
        databaseManager.insertDataAsync(DatabaseTable.TRUST_CHANGES, trustChangeDB);


        return false;
    }

    @Override
    public List<String> complete(CommandSender sender, String[] args) {

        return null;
    }
}
