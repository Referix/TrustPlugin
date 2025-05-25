package org.referix.commands.command;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.referix.commands.AbstractCommand;
import org.referix.database.DatabaseProvider;
import org.referix.database.DatabaseTable;
import org.referix.database.pojo.TrustChangeDB;
import org.referix.trustPlugin.TrustPlugin;

import java.util.*;

public class RemoveReputation extends AbstractCommand {

    private final DatabaseProvider databaseManager;

    // Карта для зберігання часу останнього використання команди
    private final Map<UUID, Long> lastUseMap = new HashMap<>();

    // Перемінна затримки в хвилинах (конвертується в мс)
    private final long COOLDOWN_MINUTES = TrustPlugin.getInstance().getConfigManager().getUntrustCooldown();
    private final long COOLDOWN_MS = COOLDOWN_MINUTES * 60 * 1000;

    public RemoveReputation(String command, DatabaseProvider databaseManager) {
        super(command);
        this.databaseManager = databaseManager;
    }

    // /-rep {target} {reason}
    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(TrustPlugin.getInstance().getConfigManager().getMessage("no_correctly_command"));
            return true;
        }

        if (!sender.hasPermission("trust.removereputation")) {
            sender.sendMessage(TrustPlugin.getInstance().getConfigManager().getMessage("no_permission"));
            return true;
        }

        if (!(sender instanceof Player actor)) {
            sender.sendMessage(TrustPlugin.getInstance().getConfigManager().getMessage("not_player"));
            return true;
        }

        UUID actorId = actor.getUniqueId();
        long now = System.currentTimeMillis();

        if (lastUseMap.containsKey(actorId)) {
            long lastUsed = lastUseMap.get(actorId);
            long elapsed = now - lastUsed;
            if (elapsed < COOLDOWN_MS) {
                long secondsLeft = (COOLDOWN_MS - elapsed) / 1000;
                actor.sendMessage(TrustPlugin.getInstance().getConfigManager().getMessage("cooldown","time" , String.valueOf(secondsLeft)));
                return true;
            }
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);

        String combinedArgs = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        double change = TrustPlugin.getInstance().getConfigManager().getBaseUntrust();

        TrustChangeDB trustChangeDB = new TrustChangeDB(
                target.getUniqueId(),
                actorId,
                change,
                combinedArgs,
                now
        );

        databaseManager.insertDataAsync(DatabaseTable.TRUST_CHANGES, trustChangeDB, null);

        // Записати новий час використання
        lastUseMap.put(actorId, now);

        actor.sendMessage(Component.text("Репутація успішно зменшена."));
        return true;
    }

    @Override
    public List<String> complete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .toList();
        } else if (args.length == 2) {
            return List.of("Причина");
        }
        return null;
    }
}
