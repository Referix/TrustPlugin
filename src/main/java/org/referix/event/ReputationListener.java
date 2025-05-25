package org.referix.event;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.referix.trustPlugin.TrustPlugin;
import org.referix.utils.PlayerDataCache;

import java.util.UUID;

public class ReputationListener implements Listener, PluginMessageListener {

    private final Plugin plugin;
    private final PlayerDataCache playerDataCache;

    public static final String CHANNEL_Reputation = "trust:reputation";
    public static final String CHANNEL_Cache = "trust:cache";

    public ReputationListener(Plugin plugin, PlayerDataCache playerDataCache) {
        this.plugin = plugin;
        this.playerDataCache = playerDataCache;

        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, CHANNEL_Reputation, this);
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, CHANNEL_Reputation);

        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, CHANNEL_Cache, this);
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, CHANNEL_Cache);
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("trust:reputation") && !channel.equals("trust:cache")) {
            return;
        }

        ByteArrayDataInput in = ByteStreams.newDataInput(message);

        try {
            String category = in.readUTF();        // Server ID
            String uuidString = in.readUTF();      // UUID гравця
            String command = in.readUTF();         // Команда або інші дані

            if (category.equals(TrustPlugin.getInstance().getServerID())) {
                plugin.getLogger().info("This server initiated the message (channel=" + channel + ", serverID=" + category + ")" + "channalType=" + channel + ", uuid=" + uuidString + ", data=" + command);
                return;
            }

            switch (channel) {
                case "trust:reputation" -> {
                    plugin.getLogger().info("Received on [trust:reputation]: UUID=" + uuidString + ", Command=" + command);
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        plugin.getLogger().info("Executing command: " + command);
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                    });
                }

                case "trust:cache" -> {
                    plugin.getLogger().info("Received on [trust:cache]: UUID=" + uuidString + ", Data=" + command);
                    updateLocalReputationCache(uuidString, command);
                }
            }


        } catch (Exception e) {
            plugin.getLogger().warning("Failed to parse PluginMessage on channel [" + channel + "]: " + e.getMessage());
        }
    }


    public static void sendToVelocity(Player player, String uuid, String command) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(TrustPlugin.getInstance().getServerID());
        out.writeUTF(uuid);
        out.writeUTF(command);

        player.sendPluginMessage(TrustPlugin.getInstance(), CHANNEL_Reputation, out.toByteArray());
        TrustPlugin.getInstance().getLogger().info("Sent to Velocity [trust:reputation]: UUID=" + uuid + ", Command=" + command);
    }

    public static void sendToVelocityCache(Player player, String uuid, String data) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(TrustPlugin.getInstance().getServerID());
        out.writeUTF(uuid);
        out.writeUTF("Cache:" + data);

        player.sendPluginMessage(TrustPlugin.getInstance(), CHANNEL_Cache, out.toByteArray());
        TrustPlugin.getInstance().getLogger().info("Sent to Velocity [trust:reputation:cache]: UUID=" + uuid + ", Data=" + data);
    }


    private void updateLocalReputationCache(String uuid, String data) {
        playerDataCache.set(UUID.fromString(uuid),data);
        plugin.getLogger().info("Updating local cache for UUID=" + uuid + " with data: " + data);
    }

}
