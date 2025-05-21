// ConfigManager.java
package org.referix.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.referix.trustPlugin.TrustPlugin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class ConfigManager {
    private final TrustPlugin plugin;
    private FileConfiguration config;

    public ConfigManager(TrustPlugin plugin) {
        this.plugin = plugin;

        File configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                configFile.createNewFile();
                config = YamlConfiguration.loadConfiguration(configFile);

                // Генерація повного дефолтного конфігу
                config.set("messages.not_player", "Ця команда доступна лише гравцям.");
                config.set("messages.no_correctly_command", "Ця команда не коректно введена");
                config.set("messages.no_permission", "У вас немає дозволу.");
                config.set("messages.usage_trust_accept", "Використання: /trustaccept {id}");
                config.set("messages.record_not_found", "Запис з ID {id} не знайдено.");
                config.set("messages.reputation_not_found", "Записів не знайдено.");
                config.set("messages.only_players_command", "тільки гравці можуть використовувати цю команду.");
                config.set("messages.reputations_for_all", "== Репутація всіх гравців (сторінка {page}) ==");
                config.set("messages.reputations_for_player", "== Репутація гравця {player} (сторінка {page}) ==");
                config.set("messages.player_not_found", "Гравець не знайдений.");
                config.set("messages.page_should_be_number", "Сторінка повинна бути числом.");
                config.set("messages.trust_change_message", "<#00ff00>Довіра гравця {player} змінена до {trust_level} ({sign}{delta})");

                config.set("setting.base_trust", 1);
                config.set("setting.base_untrust", -1);
                config.set("setting.trust_player", 100);
                config.set("setting.first_line.score", 30);
                config.set("setting.first_line.command", "kick {player}");
                config.set("setting.second_line.score", 10);
                config.set("setting.second_line.command", "ban {player}");

                config.set("save_zone.default_zone.start_chunk_x", 10);
                config.set("save_zone.default_zone.end_chunk_x", 10);
                config.set("save_zone.default_zone.start_chunk_z", -10);
                config.set("save_zone.default_zone.end_chunk_z", -10);

                config.set("database.type", "sqlite");
                config.set("database.mysql.host", "localhost");
                config.set("database.mysql.port", 3306);
                config.set("database.mysql.database", "trust");
                config.set("database.mysql.user", "root");
                config.set("database.mysql.password", "password");
                config.set("database.sqlite.file", "data.db");

                // Генерація serverID
                String serverID = UUID.randomUUID().toString();
                config.set("serverID", serverID);
                plugin.setServerID(serverID);

                config.save(configFile);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        plugin.reloadConfig();
        config = plugin.getConfig();
        plugin.setServerID(config.getString("serverID"));
    }

    public Component getMessage(String key, String... placeholders) {
        String messageTemplate = getMessageByKey(key);
        if (messageTemplate == null) return Component.text("");

        for (int i = 0; i < placeholders.length; i += 2) {
            messageTemplate = messageTemplate.replace("{" + placeholders[i] + "}", placeholders[i + 1]);
        }

        return MiniMessage.miniMessage().deserialize(messageTemplate);
    }

    private String getMessageByKey(String key) {
        return switch (key) {
            case "not_player" -> config.getString("messages.not_player");
            case "no_correctly_command" -> config.getString("messages.no_correctly_command");
            case "no_permission" -> config.getString("messages.no_permission");
            case "usage_trust_accept" -> config.getString("messages.usage_trust_accept");
            case "record_not_found" -> config.getString("messages.record_not_found");
            case "reputation_not_found" -> config.getString("messages.reputation_not_found");
            case "only_players_command" -> config.getString("messages.only_players_command");
            case "reputations_for_all" -> config.getString("messages.reputations_for_all");
            case "reputations_for_player" -> config.getString("messages.reputations_for_player");
            case "player_not_found" -> config.getString("messages.player_not_found");
            case "page_should_be_number" -> config.getString("messages.page_should_be_number");
            case "trust_change_message" -> config.getString("messages.trust_change_message");
            case "second_line.command" -> config.getString("setting.second_line.command");
            case "first_line.command" -> config.getString("setting.first_line.command");
            default -> "";
        };
    }

    public int defaultSaveZoneStartX() {
        return config.getInt("save_zone.default_zone.start_chunk_x");
    }

    public int defaultSaveZoneEndX() {
        return config.getInt("save_zone.default_zone.end_chunk_x");
    }

    public int defaultSaveZoneStartZ() {
        return config.getInt("save_zone.default_zone.start_chunk_z");
    }

    public int defaultSaveZoneEndZ() {
        return config.getInt("save_zone.default_zone.end_chunk_z");
    }

    public double getBaseUntrust() {
        return config.getDouble("setting.base_untrust");
    }

    public double getBaseTrust() {
        return config.getDouble("setting.base_trust");
    }

    public double getFirstLineScore() {
        return config.getDouble("setting.first_line.score");
    }

    public double getSecondLineScore() {
        return config.getDouble("setting.second_line.score");
    }

    public double getTrustLineScore() {
        return config.getDouble("setting.trust_player");
    }

    public String getDatabaseType() {
        return config.getString("database.type");
    }

    public String getMySQLHost() {
        return config.getString("database.mysql.host");
    }

    public int getMySQLPort() {
        return config.getInt("database.mysql.port");
    }

    public String getMySQLDatabase() {
        return config.getString("database.mysql.database");
    }

    public String getMySQLUser() {
        return config.getString("database.mysql.user");
    }

    public String getMySQLPassword() {
        return config.getString("database.mysql.password");
    }

    public String getSQLiteFile() {
        return config.getString("database.sqlite.file");
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        config = plugin.getConfig();
    }
}
