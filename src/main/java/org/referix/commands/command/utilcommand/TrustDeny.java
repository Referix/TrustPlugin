package org.referix.commands.command.utilcommand;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.referix.commands.AbstractCommand;
import org.referix.database.DatabaseManager;
import org.referix.database.DatabaseTable;
import org.referix.database.pojo.TrustChangeDB;

public class TrustDeny extends AbstractCommand {
    private final DatabaseManager databaseManager;

    public TrustDeny(String command, DatabaseManager databaseManager) {
        super(command);
        this.databaseManager = databaseManager;
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Ця команда доступна лише гравцям.");
            return true;
        }

        if (!sender.hasPermission("trust.accept")) {
            sender.sendMessage(ChatColor.RED + "У вас немає дозволу.");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Використання: /trustaccept {id}");
            return true;
        }

        String id = args[0];

        databaseManager.searchData(DatabaseTable.TRUST_CHANGES, "id = '" + id + "'", TrustChangeDB.class, changes -> {
            if (changes.isEmpty()) {
                player.sendMessage(ChatColor.RED + "Запис з ID " + id + " не знайдено.");
                return;
            }
            databaseManager.deleteById(DatabaseTable.TRUST_CHANGES, id);
            player.sendMessage(Component.text("Запис з ID:" + id + " успішно видалено!").color(TextColor.color(0, 255, 0)));
        });
        return false;
    }
}
