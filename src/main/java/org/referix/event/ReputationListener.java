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

import java.util.Arrays;

public class ReputationListener implements Listener, PluginMessageListener {

    private final Plugin plugin;

    public static final String CHANNEL = "trust:reputation";

    public ReputationListener(Plugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, CHANNEL, this);
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, CHANNEL);
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals(CHANNEL)) return;

        ByteArrayDataInput in = ByteStreams.newDataInput(message);

        try {
            String category = in.readUTF();
            String uuidString = in.readUTF();         // UUID гравця
            String command = in.readUTF();            // Команда (наприклад: "/ban Player")
            if (category.equals(TrustPlugin.getInstance().getServerID())) {
                plugin.getLogger().info("this server initial this message serverID = " + category);
                return;
            }

            plugin.getLogger().info("Received from Velocity: UUID=" + uuidString + ", Command=" + command);

            Bukkit.getScheduler().runTask(plugin, () -> {
                plugin.getLogger().info("Executing command: " + command);
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            });

        } catch (Exception e) {
            plugin.getLogger().warning("Failed to parse PluginMessage: " + e.getMessage());
        }
    }


    public static void sendToVelocity(Player player, String uuid, String command) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(TrustPlugin.getInstance().getServerID());
        out.writeUTF(uuid);
        out.writeUTF(command);

        player.sendPluginMessage(TrustPlugin.getInstance(), CHANNEL, out.toByteArray());
        TrustPlugin.getInstance().getLogger().info("Sent to Velocity: UUID=" + uuid + ", Command=" + command);
    }

}
