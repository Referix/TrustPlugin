// ConfigManager.java
package org.referix.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.referix.trustPlugin.TrustPlugin;

public class ConfigManager {
    private  TrustPlugin plugin;
    private  FileConfiguration config;


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

    public ConfigManager(TrustPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    /**
     * Метод для отримання компонента з кольором та вставками змінних
     */
    public Component getMessage(String key, String... placeholders) {
        String messageTemplate = getMessageByKey(key);
        if (messageTemplate == null) return Component.text("");

        // Обробка плейсхолдерів
        for (int i = 0; i < placeholders.length; i += 2) {
            messageTemplate = messageTemplate.replace("{" + placeholders[i] + "}", placeholders[i + 1]);
        }

        // Парсинг рядка через MiniMessage
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


    public void reloadConfig() {
        plugin.reloadConfig();
        config = plugin.getConfig();
    }

}