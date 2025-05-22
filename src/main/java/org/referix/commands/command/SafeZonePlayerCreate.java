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

    private final Object lock = new Object();

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

        synchronized (player.getUniqueId().toString().intern()) { // Синхронізація по рядку UUID
            databaseManager.searchData(DatabaseTable.SAFE_ZONE, "player_id = '" + player.getUniqueId() + "'", SafeZoneDB.class, safeZoneList -> {
                int playerChunkX = player.getLocation().getChunk().getX();
                int playerChunkZ = player.getLocation().getChunk().getZ();

                int startChunkX = playerChunkX - 2;
                int endChunkX = playerChunkX + 1;
                int startChunkZ = playerChunkZ - 2;
                int endChunkZ = playerChunkZ + 1;

                if (safeZoneList.isEmpty()) {
                    SafeZoneDB newZone = new SafeZoneDB(
                            TrustPlugin.getInstance().getServerID(),
                            player.getUniqueId().toString(),
                            startChunkX,
                            endChunkX,
                            startChunkZ,
                            endChunkZ
                    );

                    databaseManager.insertDataAsync(DatabaseTable.SAFE_ZONE, newZone, () -> {
                        safeZoneManager.getSafeZoneDBS().add(newZone);
                        player.sendMessage("Безпечна зона створена навколо вас!");
                    });
                } else {
                    SafeZoneDB existingZone = safeZoneList.getFirst();
                    SafeZoneDB newZone = new SafeZoneDB(TrustPlugin.getInstance().getServerID(),
                            player.getUniqueId().toString(), startChunkX, endChunkX, startChunkZ, endChunkZ);
                    databaseManager.updateSafeZone(newZone, () -> {
                        safeZoneManager.getSafeZoneDBS().remove(existingZone);
                        safeZoneManager.getSafeZoneDBS().add(newZone);
                        player.sendMessage("Ваша безпечна зона оновлена!");
                    });
                }
            });
        }

        return true;
    }


}
