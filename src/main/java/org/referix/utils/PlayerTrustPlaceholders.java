package org.referix.utils;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.referix.trustPlugin.TrustPlugin;

public class PlayerTrustPlaceholders extends PlaceholderExpansion {
    private  TrustPlugin plugin;
    private  PlayerDataCache cache;

    public PlayerTrustPlaceholders(TrustPlugin trustPlugin, PlayerDataCache playerDataCache) {
        this.plugin = trustPlugin;
        this.cache = playerDataCache;
    }


    @Override
    public @NotNull String getIdentifier() {
        return "trust";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Referix";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true; // не видаляється при перезавантаженні
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) return "";

        switch (params.toLowerCase()) {
            case "player_score":
                return cache.get(player.getUniqueId());
            default:
                return null;
        }
    }
}
