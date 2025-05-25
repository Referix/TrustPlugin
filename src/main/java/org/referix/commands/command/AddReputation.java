package org.referix.commands.command;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.referix.commands.AbstractCommand;
import org.referix.database.DatabaseProvider;
import org.referix.database.DatabaseTable;
import org.referix.database.pojo.TrustChangeDB;
import org.referix.trustPlugin.TrustPlugin;

import java.util.*;
import java.util.stream.Collectors;

public class AddReputation extends AbstractCommand {

    private final DatabaseProvider databaseManager;

    // Cooldown map: гравець -> час останнього використання
    private final Map<UUID, Long> lastUseMap = new HashMap<>();

    // Перемінна затримки (в мілісекундах)
    private final int COOLDOWN_MS = TrustPlugin.getInstance().getConfigManager().getTrustCooldown() * 60 * 1000; // 60 секунд

    public AddReputation(String command, DatabaseProvider databaseManager) {
        super(command);
        this.databaseManager = databaseManager;
    }

    // /+rep {target} {reason}
    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(TrustPlugin.getInstance().getConfigManager().getMessage("no_correctly_command"));
            return true;
        }

        if (!sender.hasPermission("trust.addreputation")) {
            sender.sendMessage(TrustPlugin.getInstance().getConfigManager().getMessage("no_permission"));
            return true;
        }

        if (!(sender instanceof Player actor)) {
            sender.sendMessage(TrustPlugin.getInstance().getConfigManager().getMessage("not_player"));
            return true;
        }

        // Перевірка затримки
        long now = System.currentTimeMillis();
        UUID actorId = actor.getUniqueId();

        if (lastUseMap.containsKey(actorId)) {
            long lastUsed = lastUseMap.get(actorId);
            long timeSince = now - lastUsed;
            if (timeSince < COOLDOWN_MS) {
                long secondsLeft = (COOLDOWN_MS - timeSince) / 1000;
                actor.sendMessage(TrustPlugin.getInstance().getConfigManager().getMessage("cooldown","time" , String.valueOf(secondsLeft)));
                return true;
            }
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            target = Bukkit.getOfflinePlayer(args[0]).getPlayer();
        }
        if (target != null && target.equals(actor)) {
            actor.sendMessage(TrustPlugin.getInstance().getConfigManager().getMessage("cant_trust_yourself"));
            return true;
        }

        String combinedArgs = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        double change = TrustPlugin.getInstance().getConfigManager().getBaseTrust();

        TrustChangeDB trustChangeDB = new TrustChangeDB(
                target.getUniqueId(),
                actor.getUniqueId(),
                change,
                combinedArgs,
                System.currentTimeMillis()
        );

        databaseManager.insertDataAsync(DatabaseTable.TRUST_CHANGES, trustChangeDB, null);

        // Оновлення часу останнього використання
        lastUseMap.put(actorId, now);

        actor.sendMessage(Component.text("Репутація успішно нарахована!"));
        return true;
    }

    @Override
    public List<String> complete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            return List.of("Причина");
        }
        return null;
    }
}
