package org.referix.commands.command.utilcommand;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.referix.commands.AbstractCommand;
import org.referix.database.DatabaseManager;
import org.referix.database.DatabaseTable;
import org.referix.database.pojo.PlayerTrustDB;
import org.referix.database.pojo.TrustChangeDB;
import org.referix.trustPlugin.TrustPlugin;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class TrustAccept extends AbstractCommand {
    private final DatabaseManager databaseManager;

    public TrustAccept(String command, DatabaseManager databaseManager) {
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

            TrustChangeDB change = changes.get(0);
            UUID targetId = change.getTarget_id();
            UUID actorId = change.getActor_id();
            double rawChange = change.getChange();

            databaseManager.searchData(DatabaseTable.PLAYER_TRUSTS, "player_id = '" + targetId + "'", PlayerTrustDB.class, targetList -> {
                double playerTrust = targetList.isEmpty() ? 0 : targetList.getFirst().getScore();

                databaseManager.searchData(DatabaseTable.PLAYER_TRUSTS, "player_id = '" + actorId + "'", PlayerTrustDB.class, actorList -> {
                    double trustedPlayerTrust = actorList.isEmpty() ? 0 : actorList.getFirst().getScore();

                    double baseTrust = TrustPlugin.getInstance().getConfig().getDouble("trust.base_trust", 1.0);
                    double baseUntrust = TrustPlugin.getInstance().getConfig().getDouble("trust.base_untrust", -1.0);

                    double delta;

                    if (rawChange > 0) {
                        delta = baseTrust + (0.1 * trustedPlayerTrust + 0.01) / (1 + playerTrust * 0.01);
                    } else {
                        delta = baseUntrust - (0.1 * trustedPlayerTrust + 0.01) / (1 + playerTrust * 0.01);
                    }

                    double newTrust = playerTrust + delta;

                    databaseManager.updatePlayerTrust(targetId, newTrust);

                    databaseManager.deleteById(DatabaseTable.TRUST_CHANGES, id);

                    String sign = delta >= 0 ? "+" : "-";
                    player.sendMessage(ChatColor.GREEN + "Довіра гравця " +
                            Bukkit.getOfflinePlayer(targetId).getName() +
                            " змінена до " + (int) Math.floor(newTrust) +
                            " (" + sign + String.format("%.2f", Math.abs(delta)) + ")");
                });
            });
        });

        return true;
    }
}
