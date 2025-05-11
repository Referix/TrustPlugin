package org.referix.commands.command;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.referix.commands.AbstractCommand;
import org.referix.database.DatabaseManager;
import org.referix.database.DatabaseTable;
import org.referix.database.pojo.TrustChangeDB;

import java.util.List;
import java.util.stream.Collectors;

public class RemoveReputation extends AbstractCommand {

    private final DatabaseManager databaseManager;

    public RemoveReputation(String command, DatabaseManager databaseManager) {
        super(command);
        this.databaseManager = databaseManager;
    }
    // /-rep {target} {res}
    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
            if ( args.length != 2) {
                sender.sendMessage("ну коректно");
                return true;
            } else if (!sender.hasPermission("trust.removerep")) {
                sender.hasPermission("no have permission");
                return true;
            } else if (!(sender instanceof Player)) {
                sender.sendMessage("only players command");
            }
            Player target = Bukkit.getPlayer(args[0]);
            Player actor = (Player) sender;
            if (target == null) return true;

            TrustChangeDB trustChangeDB = new TrustChangeDB(target.getUniqueId(), actor.getUniqueId(), -10, args[1], System.currentTimeMillis());
            databaseManager.insertDataAsync(DatabaseTable.TRUST_CHANGES, trustChangeDB);


        return false;
    }

    @Override
    public List<String> complete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            // Підказка для імен гравців
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            // Підказка для можливих причин зміни репутації
            return List.of("Причина");
        }
        return null;
    }
}
