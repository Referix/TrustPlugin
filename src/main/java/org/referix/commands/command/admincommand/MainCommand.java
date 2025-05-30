package org.referix.commands.command.admincommand;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.referix.commands.AbstractCommand;
import org.referix.database.DatabaseProvider;
import org.referix.savezone.SafeZoneManager;
import org.referix.trustPlugin.TrustPlugin;
import org.referix.utils.PlayerDataCache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class MainCommand extends AbstractCommand {
    private DatabaseProvider databaseManager;
    private PlayerDataCache playerDataCache;
    private SafeZoneManager safeZoneManager;


    public MainCommand(String command, DatabaseProvider databaseManager, PlayerDataCache playerDataCache, SafeZoneManager safeZoneManager) {
        super(command);
        this.databaseManager = databaseManager;
        this.playerDataCache = playerDataCache;
        this.safeZoneManager = safeZoneManager;
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        if (!sender.hasPermission("trust.admin")) sender.sendMessage(TrustPlugin.getInstance().getConfigManager().getMessage("no_permission"));
        if (!(sender instanceof Player)) sender.sendMessage(TrustPlugin.getInstance().getConfigManager().getMessage("not_player"));
        Player p = null;
        if (sender instanceof Player) {
            p = (Player) sender;
        }


        switch (args[0].toLowerCase()) {
            case "reload" -> {
                if (!sender.hasPermission("trust.admin.reload")) {
                    sender.sendMessage(TrustPlugin.getInstance().getConfigManager().getMessage("no_permission"));
                    return true;
                }
                new ReloadCommand(p, args);
            }
            case "add" -> {
                if (!sender.hasPermission("trust.admin.add")) {
                    sender.sendMessage(TrustPlugin.getInstance().getConfigManager().getMessage("no_permission"));
                    return true;
                }
                if (args.length < 3) {
                    sender.sendMessage("§cВикористання: /trusts add <гравець> <score>");
                    return true;
                }
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
                UUID targetUUID = offlinePlayer.getUniqueId();

                new AdminAddTrust(p, args, targetUUID, databaseManager, playerDataCache);
            }
            case "remove" -> {
                if (!sender.hasPermission("trust.admin.remove")) {
                    sender.sendMessage(TrustPlugin.getInstance().getConfigManager().getMessage("no_permission"));
                    return true;
                }
                if (args[1].equalsIgnoreCase("safezone") && args.length == 2) {
                    new SafeZoneDelete(p,args, safeZoneManager);
                    p.sendMessage(TrustPlugin.getInstance().getConfigManager().getMessage("safezone_deleted"));
                    return true;
                }
                if (args.length < 3) {
                    sender.sendMessage("§cВикористання: /trusts remove <гравець>");
                    return true;
                }
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
                UUID targetUUID = offlinePlayer.getUniqueId();

                new AdminAddTrust(p, args, targetUUID, databaseManager, playerDataCache);
            }
            case "list" -> {
                if (!sender.hasPermission("trust.admin.list")) {
                    sender.sendMessage(TrustPlugin.getInstance().getConfigManager().getMessage("no_permission"));
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage("§cВикористання: /trusts list");
                    return true;
                }
                new ListReputation(p,args,databaseManager);
            }
            case "help" -> {
                if (!sender.hasPermission("trust.admin.help")) {
                    sender.sendMessage(TrustPlugin.getInstance().getConfigManager().getMessage("no_permission"));
                    return true;
                }
                sendHelp(sender);
            }
            case "debug" -> {
                if (TrustPlugin.getInstance().isDebugEnabled()){
                    TrustPlugin.getInstance().setDebugEnabled(false);
                    sender.sendMessage("Debug off");
                } else {
                    TrustPlugin.getInstance().setDebugEnabled(true);
                    sender.sendMessage("Debug on");
                }
            }
            default -> {
                sender.sendMessage("§cНевідома команда. Використайте /trusts help для списку команд.");
            }
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6/trusts help §7- Показати допомогу");
        sender.sendMessage("§6/trusts reload §7- Перезавантажити конфігурацію");
        sender.sendMessage("§6/trusts add <гравець> <score> §7- Додати гравця до довірених");
        sender.sendMessage("§6/trusts remove <гравець> <score> §7- Видалити гравця з довірених");
    }

    @Override
    public List<String> complete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return List.of("help", "reload", "add", "remove", "list").stream()
                    .filter(sub -> sender.hasPermission("trust.admin." + sub))
                    .filter(sub -> sub.startsWith(args[0].toLowerCase()))
                    .toList();
        }

        if (args.length == 2) {

            switch (args[0].toLowerCase()) {
                case "add", "remove", "list", "debug" -> {
                    List<String> list = new ArrayList<>(Arrays.stream(Bukkit.getOfflinePlayers())
                            .map(OfflinePlayer::getName)
                            .filter(name -> name != null && name.toLowerCase().startsWith(args[1].toLowerCase()))
                            .distinct()
                            .sorted()
                            .toList());
                    if (args[0].equalsIgnoreCase("remove")) {
                        list.add("safezone");
                    }
                    if (args[0].equalsIgnoreCase("list")) {
                        list.add("all");
                    };
                    return list;
                }
            }
        }

        return List.of();
    }



}
