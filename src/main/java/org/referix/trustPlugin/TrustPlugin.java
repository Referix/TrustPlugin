package org.referix.trustPlugin;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginLogger;
import org.bukkit.plugin.java.JavaPlugin;
import org.referix.commands.command.AddReputation;
import org.referix.commands.command.ListReputation;
import org.referix.commands.command.ReloadCommand;
import org.referix.commands.command.RemoveReputation;
import org.referix.commands.command.utilcommand.TrustAccept;
import org.referix.commands.command.utilcommand.TrustDeny;
import org.referix.database.DatabaseManager;
import org.referix.database.DatabaseTable;
import org.referix.utils.*;

import java.util.logging.Logger;

public final class TrustPlugin extends JavaPlugin {

    private static TrustPlugin plugin;
    private DatabaseManager databaseManager;
    private PlayerDataCache playerDataCache;
    private ConfigManager configManager;
    private FileLogger logger;

    @Override
    public void onEnable() {
        plugin = this;
        databaseManager = new DatabaseManager(getDataFolder().getPath() + "/database/database.db");
        databaseManager.createTable(DatabaseTable.PLAYER_TRUSTS);
        databaseManager.createTable(DatabaseTable.TRUST_CHANGES);
        saveDefaultConfig();
        PermissionUtil.init();
        configManager = new ConfigManager(this);
        playerDataCache = new PlayerDataCache();
        logger = new FileLogger(this);

        Bukkit.getPluginManager().registerEvents(new org.referix.event.PlayerEvent(databaseManager, playerDataCache), this);

        new AddReputation("addrep", databaseManager);
        new RemoveReputation("removerep", databaseManager);
        new ListReputation("listrep", databaseManager);
        new TrustAccept("trustaccept", databaseManager, playerDataCache, logger);
        new TrustDeny("trustdeny",databaseManager, logger);
        new ReloadCommand("trust");


        new PlayerTrustPlaceholders(this, playerDataCache).register();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static TrustPlugin getInstance() {
        return plugin;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}
