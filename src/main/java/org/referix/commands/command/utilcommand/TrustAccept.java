package org.referix.commands.command.utilcommand;

import net.kyori.adventure.text.Component;
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
import org.referix.utils.ConfigManager;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class TrustAccept extends AbstractCommand {
    private final DatabaseManager databaseManager;
    private ConfigManager configManager;

    public TrustAccept(String command, DatabaseManager databaseManager, ConfigManager configManager) {
        super(command);
        this.databaseManager = databaseManager;
        this.configManager = configManager;
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(configManager.getMessage("not_player"));
            return true;
        }

        if (!sender.hasPermission("trust.accept")) {
            sender.sendMessage(configManager.getMessage("no_permission"));
            return true;
        }

        if (args.length != 1) {
            return true;
        }

        String id = args[0];

        databaseManager.searchData(DatabaseTable.TRUST_CHANGES, "id = '" + id + "'", TrustChangeDB.class, changes -> {
            if (changes.isEmpty()) {
                player.sendMessage(ChatColor.RED + "Запис c ID " + id + " не найдено.");
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
                    Component messageTemplate = configManager.getMessage(
                            "trust_change_message",
                            "player", Bukkit.getOfflinePlayer(targetId).getName(),
                            "trust_level", String.valueOf((int) Math.floor(newTrust)),
                            "sign", sign,
                            "delta", String.format("%.2f", Math.abs(delta))
                    );
                    player.sendMessage(messageTemplate);
                });
            });
        });

        return true;
    }
}
