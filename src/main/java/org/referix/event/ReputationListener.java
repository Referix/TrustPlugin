package org.referix.event;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

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
        plugin.getLogger().info("Received PluginMessage on channel: " + channel);

        if (!channel.equals(CHANNEL)) {
            plugin.getLogger().info("Ignoring message on unrelated channel: " + channel);
            return;
        }

        plugin.getLogger().info("Raw message bytes: " + Arrays.toString(message));

        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String command;

        try {
            command = in.readUTF();
            plugin.getLogger().info("Parsed command: " + command);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to read command from PluginMessage: " + e.getMessage());
            return;
        }

        // Виконати команду від імені консолі
        Bukkit.getScheduler().runTask(plugin, () -> {
            plugin.getLogger().info("Executing command from Velocity: " + command);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        });
    }
}
