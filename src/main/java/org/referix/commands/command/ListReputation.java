package org.referix.commands.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.referix.commands.AbstractCommand;
import org.referix.database.DatabaseProvider;
import org.referix.database.pojo.TrustChangeDB;
import org.referix.trustPlugin.TrustPlugin;
import org.referix.utils.ConfigManager;

import java.util.Objects;
import java.util.UUID;

public class ListReputation extends AbstractCommand {
    private DatabaseProvider databaseManager;


    public ListReputation(String command, DatabaseProvider databaseManager) {
        super(command);
        this.databaseManager = databaseManager;
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        int page = 1;
        String targetId = null;
        if (!sender.hasPermission("trust.list")) {
            sender.sendMessage(TrustPlugin.getInstance().getConfigManager().getMessage("no_permission"));
            return true;
        }
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(TrustPlugin.getInstance().getConfigManager().getMessage("not_player"));
                return true;
            }
            targetId = ((Player) sender).getUniqueId().toString();
        } else if (args[0].equalsIgnoreCase("all")) {
            if (args.length > 1) {
                try {
                    page = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(TrustPlugin.getInstance().getConfigManager().getMessage("page_should_be_number"));
                    return true;
                }
            }
        } else {
            Player target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                sender.sendMessage(TrustPlugin.getInstance().getConfigManager().getMessage("player_not_found"));
                return true;
            }
            targetId = target.getUniqueId().toString();
            if (args.length > 1) {
                try {
                    page = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(TrustPlugin.getInstance().getConfigManager().getMessage("page_should_be_number"));
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
        int limit = 10;
        String countSql = "SELECT COUNT(*) FROM trust_changes";
        databaseManager.countRows(countSql, count -> {
            int totalPages = (int) Math.ceil(count / (double) limit);
            int currentPage = Math.max(1, Math.min(page, totalPages));

            String sql = "SELECT * FROM trust_changes ORDER BY timestamp DESC LIMIT ? OFFSET ?";
            Object[] params = {limit, limit * (currentPage - 1)};
            databaseManager.searchDataRaw(sql, params, TrustChangeDB.class, list -> {
                if (list.isEmpty()) {
                    sender.sendMessage(TrustPlugin.getInstance().getConfigManager().getMessage("reputation_not_found"));
                    return;
                }
                Component title = TrustPlugin.getInstance().getConfigManager().getMessage("reputations_for_all", "page", String.valueOf(currentPage));
                sender.sendMessage(title);

                for (TrustChangeDB entry : list) {
                    int id = entry.getId();
                    String actorName = Bukkit.getOfflinePlayer(entry.getActor_id()).getName();
                    String targetName = Bukkit.getOfflinePlayer(entry.getTarget_id()).getName();
                    String reason = entry.getReason();
                    double change = entry.getChange();
                    String firstWord = reason.split(" ")[0]; // перше слово з reason
                    TextComponent.Builder line = Component.text()
                            .append(Component.text("ID: " + id + " ", NamedTextColor.DARK_GRAY))
                            .append(Component.text(actorName, NamedTextColor.YELLOW))
                            .append(Component.text(" -> ", NamedTextColor.GRAY))
                            .append(Component.text(targetName, NamedTextColor.GREEN))
                            .append(getSignDescription(change))
                            .append(Component.text(firstWord, NamedTextColor.AQUA)
                                    .append(Component.space())
                                    .append(Component.text("[?]").color(TextColor.color(198, 153, 18)))
                                    .hoverEvent(HoverEvent.showText(Component.text(reason)))) // повний текст у ховері
                            .append(Component.space())
                            .append(Component.text("[accept]", NamedTextColor.GREEN)
                                    .clickEvent(ClickEvent.runCommand("/trustaccept " + id)))
                            .append(Component.space())
                            .append(Component.text("[deny]", NamedTextColor.RED)
                                    .clickEvent(ClickEvent.runCommand("/trustdeny " + id)));

                    if (sender instanceof Player player) {
                        player.sendMessage(line.build());
                    }
                }

                sendPageControls(sender, currentPage, totalPages, "/listrep all");
            });
        });
    }
    public Component getSignDescription(double change) {
        if (change > 0) {
            return Component.text(" (+) ", NamedTextColor.GREEN);
        } else if (change < 0) {
            return Component.text(" (-) ", NamedTextColor.RED);
        }
        return Component.text("0", NamedTextColor.GRAY);
    }

    private void listPlayerReputation(CommandSender sender, String playerId, int page) {
        int limit = 10;
        String countSql = "SELECT COUNT(*) FROM trust_changes WHERE target_id = ? OR actor_id = ?";
        Object[] countParams = {playerId, playerId};
        databaseManager.countRows(countSql, countParams, count -> {
            int totalPages = (int) Math.ceil(count / (double) limit);
            int currentPage = Math.max(1, Math.min(page, totalPages));

            String sql = "SELECT * FROM trust_changes WHERE target_id = ? OR actor_id = ? ORDER BY timestamp DESC LIMIT ? OFFSET ?";
            Object[] params = {playerId, playerId, limit, limit * (currentPage - 1)};
            databaseManager.searchDataRaw(sql, params, TrustChangeDB.class, list -> {
                if (list.isEmpty()) {
                    sender.sendMessage("§7Записів не знайдено.");
                    return;
                }
                Component title = TrustPlugin.getInstance().getConfigManager().getMessage("reputations_for_player", "page", String.valueOf(currentPage),
                        "player", Objects.requireNonNull(Bukkit.getOfflinePlayer(UUID.fromString(playerId))).getName());
                sender.sendMessage(title);
                for (TrustChangeDB entry : list) {
                    int id = entry.getId();
                    String actorName = Bukkit.getOfflinePlayer(entry.getActor_id()).getName();
                    String targetName = Bukkit.getOfflinePlayer(entry.getTarget_id()).getName();
                    String reason = entry.getReason();
                    double change = entry.getChange();
                    String firstWord = reason.split(" ")[0]; // перше слово з reason
                    TextComponent.Builder line = Component.text()
                            .append(Component.text("ID: " + id + " ", NamedTextColor.DARK_GRAY))
                            .append(Component.text(actorName, NamedTextColor.YELLOW))
                            .append(Component.text(" -> ", NamedTextColor.GRAY))
                            .append(Component.text(targetName, NamedTextColor.GREEN))
                            .append(getSignDescription(change))
                            .append(Component.text(firstWord, NamedTextColor.AQUA)
                            .append(Component.space())
                                    .append(Component.text("[?]").color(TextColor.color(198, 153, 18)))
                                        .hoverEvent(HoverEvent.showText(Component.text(reason))))
                            .append(Component.space())
                            .append(Component.text("[accept]", NamedTextColor.GREEN)
                                    .clickEvent(ClickEvent.runCommand("/trustaccept " + id)))
                            .append(Component.space())
                            .append(Component.text("[deny]", NamedTextColor.RED)
                                    .clickEvent(ClickEvent.runCommand("/trustdeny " + id)));
                    if (sender instanceof Player player) {
                        player.sendMessage(line.build());
                    }
                }
                String playerName = Bukkit.getOfflinePlayer(UUID.fromString(playerId)).getName();
                sendPageControls(sender, currentPage, totalPages, "/listrep " + playerName);
            });
        });
    }



    private void sendPageControls(CommandSender sender, int currentPage, int totalPages, String baseCommand) {
        int startPage = ((currentPage - 1) / 10) * 10 + 1;
        int endPage = Math.min(startPage + 9, totalPages);

        TextComponent.Builder pagination = Component.text();

        if (startPage > 1) {
            pagination.append(
                    Component.text("« ", NamedTextColor.GRAY)
                            .clickEvent(ClickEvent.runCommand(baseCommand + " " + (startPage - 1)))
            );
        }

        for (int i = startPage; i <= endPage; i++) {
            if (i == currentPage) {
                pagination.append(
                        Component.text("[" + i + "] ", NamedTextColor.GOLD)
                );
            } else {
                pagination.append(
                        Component.text(i + " ", NamedTextColor.GRAY)
                                .clickEvent(ClickEvent.runCommand(baseCommand + " " + i))
                );
            }
        }

        if (endPage < totalPages) {
            pagination.append(
                    Component.text("»", NamedTextColor.GRAY)
                            .clickEvent(ClickEvent.runCommand(baseCommand + " " + (endPage + 1)))
            );
        }

        if (sender instanceof Player player) {
            player.sendMessage(pagination.build());
        }
    }
}
