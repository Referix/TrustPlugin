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
import org.referix.database.DatabaseFactory;
import org.referix.database.DatabaseProvider;
import org.referix.database.DatabaseTable;
import org.referix.event.ReputationListener;
import org.referix.utils.*;

import java.util.logging.Logger;

public final class TrustPlugin extends JavaPlugin {

    private static TrustPlugin plugin;
    private DatabaseProvider database;
    private PlayerDataCache playerDataCache;
    private ConfigManager configManager;
    private FileLogger logger;

    @Override
    public void onEnable() {
        plugin = this;
        this.database = DatabaseFactory.createDatabase();
        database.connect();
        database.createTable(DatabaseTable.PLAYER_TRUSTS);
        database.createTable(DatabaseTable.TRUST_CHANGES);
        saveDefaultConfig();
        PermissionUtil.init();
        configManager = new ConfigManager(this);
        playerDataCache = new PlayerDataCache();
        logger = new FileLogger(this);

        Bukkit.getPluginManager().registerEvents(new org.referix.event.PlayerEvent(database, playerDataCache), this);

        new AddReputation("addrep", database);
        new RemoveReputation("removerep", database);
        new ListReputation("listrep", database);
        new TrustAccept("trustaccept", database, playerDataCache, logger);
        new TrustDeny("trustdeny",database, logger);
        new ReloadCommand("trust");


        new PlayerTrustPlaceholders(this, playerDataCache).register();
        ReputationListener listener = new ReputationListener(this);
        Bukkit.getPluginManager().registerEvents(listener, this);
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
