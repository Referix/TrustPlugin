package org.referix.commands.command;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.referix.commands.AbstractCommand;
import org.referix.database.DatabaseProvider;
import org.referix.database.DatabaseTable;
import org.referix.database.pojo.PlayerTrustDB;
import org.referix.database.pojo.TrustChangeDB;
import org.referix.trustPlugin.TrustPlugin;
import org.referix.utils.ConfigManager;

import java.util.List;
import java.util.stream.Collectors;

public class AddReputation extends AbstractCommand {

    private DatabaseProvider databaseManager;

    public AddReputation(String command, DatabaseProvider databaseManager) {
        super(command);
        this.databaseManager = databaseManager;
    }
    // /+rep {target} {res}
    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if ( args.length != 2) {
            Component message = TrustPlugin.getInstance().getConfigManager().getMessage("no_correctly_command");
            sender.sendMessage(message);
            return true;
        } else if (!sender.hasPermission("trust.addrep")) {
            sender.sendMessage(TrustPlugin.getInstance().getConfigManager().getMessage("no_permission"));
            return true;
        } else if (!(sender instanceof Player)) {
            sender.sendMessage(TrustPlugin.getInstance().getConfigManager().getMessage("not_player"));
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
