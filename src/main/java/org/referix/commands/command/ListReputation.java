package org.referix.commands.command;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.referix.commands.AbstractCommand;
import org.referix.database.DatabaseManager;
import org.referix.database.pojo.TrustChangeDB;

import java.util.Objects;

public class ListReputation extends AbstractCommand {
    private final DatabaseManager databaseManager;

    public ListReputation(String command, DatabaseManager databaseManager) {
        super(command);
        this.databaseManager = databaseManager;
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        int page = 1;
        String targetId = null;

        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cЦя команда доступна лише гравцям.");
                return true;
            }
            targetId = ((Player) sender).getUniqueId().toString();
        } else if (args[0].equalsIgnoreCase("all")) {
            if (args.length > 1) {
                try {
                    page = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cСторінка повинна бути числом.");
                    return true;
                }
            }
        } else {
            Player target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                sender.sendMessage("§cГравець не знайдений.");
                return true;
            }
            targetId = target.getUniqueId().toString();
            if (args.length > 1) {
                try {
                    page = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cСторінка повинна бути числом.");
                    return true;
                }
            }
        }

        if (targetId == null) {
            listAllReputation(sender, page);
        } else {
            listPlayerReputation(sender, targetId, page);
        }

        return true;
    }

    private void listAllReputation(CommandSender sender, int page) {
        String sql = "SELECT * FROM trust_changes ORDER BY timestamp DESC LIMIT ? OFFSET ?";
        Object[] params = {10, 10 * (page - 1)};
        databaseManager.searchDataRaw(sql, params, TrustChangeDB.class, list -> {
            if (list.isEmpty()) {
                sender.sendMessage("§7Записів не знайдено.");
                return;
            }
            sender.sendMessage("§6== Репутація всіх гравців (сторінка " + page + ") ==");
            for (TrustChangeDB entry : list) {
                sender.sendMessage("§e" + Objects.requireNonNull(Bukkit.getPlayer(entry.getActor_id())).getName() + " §7-> §a" + Objects.requireNonNull(Bukkit.getPlayer(entry.getTarget_id())).getName() +
                        " §f(" + entry.getChange() + "): §b" + entry.getReason());
            }
        });
    }

    private void listPlayerReputation(CommandSender sender, String playerId, int page) {
        String sql = "SELECT * FROM trust_changes WHERE target_id = ? OR actor_id = ? ORDER BY timestamp DESC LIMIT ? OFFSET ?";
        Object[] params = {playerId, playerId, 10, 10 * (page - 1)};
        databaseManager.searchDataRaw(sql, params, TrustChangeDB.class, list -> {
            if (list.isEmpty()) {
                sender.sendMessage("§7Записів не знайдено.");
                return;
            }
            sender.sendMessage("§6== Репутація гравця (сторінка " + page + ") ==");
            for (TrustChangeDB entry : list) {
                sender.sendMessage("§e" + Objects.requireNonNull(Bukkit.getPlayer(entry.getActor_id())).getName() + " §7-> §a" + Objects.requireNonNull(Bukkit.getPlayer(entry.getTarget_id())).getName() +
                        " §f(" + entry.getChange() + "): §b" + entry.getReason());
            }
        });
    }
}
