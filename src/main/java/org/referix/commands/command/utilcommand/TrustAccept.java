package org.referix.commands.command.utilcommand;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.referix.commands.AbstractCommand;
import org.referix.database.DatabaseProvider;
import org.referix.database.DatabaseTable;
import org.referix.database.pojo.PlayerTrustDB;
import org.referix.database.pojo.TrustChangeDB;
import org.referix.trustPlugin.TrustPlugin;
import org.referix.utils.ConfigManager;
import org.referix.utils.FileLogger;
import org.referix.utils.PermissionUtil;
import org.referix.utils.PlayerDataCache;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class TrustAccept extends AbstractCommand {
    private DatabaseProvider databaseManager;
    private PlayerDataCache playerDataCache;
    private FileLogger fileLogger;

    public TrustAccept(String command, DatabaseProvider databaseManager, PlayerDataCache playerDataCache, FileLogger fileLogger) {
        super(command);
        this.databaseManager = databaseManager;
        this.playerDataCache = playerDataCache;
        this.fileLogger = fileLogger;
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(TrustPlugin.getInstance().getConfigManager().getMessage("not_player"));
            return true;
        }

        if (!sender.hasPermission("trust.accept")) {
            sender.sendMessage(TrustPlugin.getInstance().getConfigManager().getMessage("no_permission"));
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

                    double baseTrust = TrustPlugin.getInstance().getConfigManager().getBaseTrust();
                    double baseUntrust = TrustPlugin.getInstance().getConfigManager().getBaseUntrust();
                    double getPermToTrust = TrustPlugin.getInstance().getConfigManager().getTrustLineScore();

                    double delta;

                    if (rawChange > 0) {
                        delta = baseTrust + (0.1 * trustedPlayerTrust + 0.01) / (1 + Math.abs(playerTrust) * 0.01);
                    } else {
                        delta = baseUntrust - (0.1 * trustedPlayerTrust + 0.01) / (1 + Math.abs(playerTrust) * 0.01);
                    }

                    double newTrust = playerTrust + delta;
                    double firstLineScore = TrustPlugin.getInstance().getConfigManager().getFirstLineScore();
                    double secondLineScore = TrustPlugin.getInstance().getConfigManager().getSecondLineScore();
                    System.out.println(firstLineScore + " " + secondLineScore + " " + newTrust);

                    databaseManager.updatePlayerTrust(targetId, newTrust);

                    databaseManager.deleteById(DatabaseTable.TRUST_CHANGES, id);

                    String sign = delta >= 0 ? "+" : "-";
                    if (newTrust < firstLineScore && newTrust > secondLineScore){
                        System.out.println("first line");
                        Component messageTemplate = TrustPlugin.getInstance().getConfigManager().getMessage("first_line.command","player", Bukkit.getOfflinePlayer(targetId).getName());
                        String command = PlainTextComponentSerializer.plainText().serialize(messageTemplate);
                        TrustPlugin.getInstance().getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
                    }
                    if (newTrust < secondLineScore){
                        System.out.println("second line");
                        Component messageTemplate = TrustPlugin.getInstance().getConfigManager().getMessage("second_line.command","player", Bukkit.getOfflinePlayer(targetId).getName());
                        String command = PlainTextComponentSerializer.plainText().serialize(messageTemplate);
                        TrustPlugin.getInstance().getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
                    }

                    if (newTrust < getPermToTrust) {
                        PermissionUtil.hasPermission(targetId, "trust.addreputation").thenAccept(has -> {
                            if (has) {
                                System.out.println("remove reputation permission");
                                PermissionUtil.removePermission(targetId, "trust.addreputation").thenCompose(aVoid -> {
                                    return PermissionUtil.removePermission(targetId, "trust.removereputation");
                                });
                            }
                        });
                    }

                    if (newTrust >= getPermToTrust) {
                        PermissionUtil.hasPermission(targetId, "trust.addreputation").thenAccept(has -> {
                            if (!has) {
                                System.out.println("add reputation permission");
                                PermissionUtil.givePermission(targetId, "trust.addreputation").thenCompose(aVoid -> {
                                    return PermissionUtil.givePermission(targetId, "trust.removereputation");
                                });
                            }
                        });
                    }

                    Component messageTemplate = TrustPlugin.getInstance().getConfigManager().getMessage(
                            "trust_change_message",
                            "player", Bukkit.getOfflinePlayer(targetId).getName(),
                            "trust_level", String.valueOf((int) Math.floor(newTrust)),
                            "sign", sign,
                            "delta", String.format("%.2f", Math.abs(delta))
                    );
                    playerDataCache.set(targetId, String.valueOf(Math.floor(newTrust)));
                    fileLogger.logReputationChange(
                            Objects.requireNonNull(Bukkit.getOfflinePlayer(actorId).getName()),
                            Math.abs(actorList.getFirst().getScore()),
                            Objects.requireNonNull(Bukkit.getOfflinePlayer(targetId).getName()),
                            targetList.getFirst().getScore(),
                            delta,
                            changes.getFirst().getReason(),
                            true,
                            false
                    );


                    player.sendMessage(messageTemplate);
                });
            });
        });

        return true;
    }
}
