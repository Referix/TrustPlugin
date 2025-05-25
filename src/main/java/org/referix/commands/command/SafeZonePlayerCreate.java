package org.referix.commands.command;

import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.referix.commands.AbstractCommand;
import org.referix.database.DatabaseProvider;
import org.referix.database.DatabaseTable;
import org.referix.database.pojo.PlayerCommandDB;
import org.referix.database.pojo.SafeZoneDB;
import org.referix.database.pojo.TrustChangeDB;
import org.referix.savezone.SafeZoneManager;
import org.referix.trustPlugin.TrustPlugin;
import org.referix.utils.ConfigManager;

import javax.swing.*;

public class SafeZonePlayerCreate extends AbstractCommand {
    private final ConfigManager configManager;
    private final DatabaseProvider databaseManager;
    private SafeZoneManager safeZoneManager;


    public SafeZonePlayerCreate(String command, ConfigManager configManager, DatabaseProvider databaseManager, SafeZoneManager safeZoneManager) {
        super(command);
        this.configManager = configManager;
        this.databaseManager = databaseManager;
        this.safeZoneManager = safeZoneManager;
    }


    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (args.length != 0) {
            sender.sendMessage(configManager.getMessage("no_correctly_command"));
            return false;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage(configManager.getMessage("not_player"));
            return false;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("trust.safezone.create")) {
            player.sendMessage(configManager.getMessage("no_permission"));
            return false;
        }

        int radius = configManager.getPlayerZoneRadius();
        createOrUpdateSafeZone(player, radius, () ->
            player.sendMessage(configManager.getMessage("player_zone_confirm")));

        return true;
    }


    public void createOrUpdateSafeZone(Player player, int radius, Runnable onFinish) {
        synchronized (player.getUniqueId().toString().intern()) {
            databaseManager.searchData(DatabaseTable.SAFE_ZONE,
                    "player_id = '" + player.getUniqueId() + "'",
                    SafeZoneDB.class,
                    safeZoneList -> {

                        int playerChunkX = player.getLocation().getChunk().getX();
                        int playerChunkZ = player.getLocation().getChunk().getZ();

                        int startChunkX = playerChunkX - radius;
                        int endChunkX = playerChunkX + radius;
                        int startChunkZ = playerChunkZ - radius;
                        int endChunkZ = playerChunkZ + radius;

                        SafeZoneDB newZone = new SafeZoneDB(
                                TrustPlugin.getInstance().getServerID(),
                                player.getUniqueId().toString(),
                                startChunkX,
                                endChunkX,
                                startChunkZ,
                                endChunkZ
                        );

                        if (safeZoneList.isEmpty()) {
                            databaseManager.insertDataAsync(DatabaseTable.SAFE_ZONE, newZone, () -> {
                                safeZoneManager.getSafeZoneDBS().add(newZone);
                                player.sendMessage("Безпечна зона створена навколо вас!");
                                if (onFinish != null) onFinish.run();
                            });
                        } else {
                            SafeZoneDB existingZone = safeZoneList.getFirst();
                            databaseManager.updateSafeZone(newZone, () -> {
                                safeZoneManager.getSafeZoneDBS().remove(existingZone);
                                safeZoneManager.getSafeZoneDBS().add(newZone);
                                player.sendMessage("Ваша безпечна зона оновлена!");
                                if (onFinish != null) onFinish.run();
                            });
                        }
                    });
        }
    }



}
