// ConfigManager.java
package org.referix.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.Nullable;
import org.referix.trustPlugin.TrustPlugin;

public class ConfigManager {
    private final TrustPlugin plugin;
    private final FileConfiguration config;

    // Змінні для кожного повідомлення
    private final String notPlayer;
    private final String noPermission;
    private final String usageTrustAccept;
    private final String recordNotFound;
    private final String reputationNotFound;
    private final String onlyPlayersCommand;
    private final String reputationsForAll;
    private final String reputationsForPlayer;
    private final String playerNotFound;
    private final String pageShouldBeNumber;
    private final String notCorrectly;
    private final String trustChangeMassege;

    public ConfigManager(TrustPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();

        // Завантаження значень із конфігурації
        this.notPlayer = config.getString("messages.not_player");
        this.notCorrectly = config.getString("messages.no_correctly_command");
        this.noPermission = config.getString("messages.no_permission");
        this.usageTrustAccept = config.getString("messages.usage_trust_accept");
        this.recordNotFound = config.getString("messages.record_not_found");
        this.reputationNotFound = config.getString("messages.reputation_not_found");
        this.onlyPlayersCommand = config.getString("messages.only_players_command");
        this.reputationsForAll = config.getString("messages.reputations_for_all");
        this.reputationsForPlayer = config.getString("messages.reputations_for_player");
        this.playerNotFound = config.getString("messages.player_not_found");
        this.pageShouldBeNumber = config.getString("messages.page_should_be_number");
        this.trustChangeMassege = config.getString("messages.trust_change_message");
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
            case "not_player" -> notPlayer;
            case "no_correctly_command" -> notCorrectly;
            case "no_permission" -> noPermission;
            case "usage_trust_accept" -> usageTrustAccept;
            case "record_not_found" -> recordNotFound;
            case "reputation_not_found" -> reputationNotFound;
            case "only_players_command" -> onlyPlayersCommand;
            case "reputations_for_all" -> reputationsForAll;
            case "reputations_for_player" -> reputationsForPlayer;
            case "player_not_found" -> playerNotFound;
            case "page_should_be_number" -> pageShouldBeNumber;
            case "trust_change_message" -> trustChangeMassege;
            default -> "";
        };
    }
}