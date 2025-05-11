package org.referix.trustPlugin;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.referix.commands.command.AddReputation;
import org.referix.commands.command.ListReputation;
import org.referix.commands.command.RemoveReputation;
import org.referix.commands.command.utilcommand.TrustAccept;
import org.referix.commands.command.utilcommand.TrustDeny;
import org.referix.database.DatabaseManager;
import org.referix.database.DatabaseTable;
import org.referix.database.pojo.PlayerTrustDB;
import org.referix.database.pojo.TrustChangeDB;
import org.referix.utils.ConfigManager;

import java.util.UUID;

public final class TrustPlugin extends JavaPlugin {

    private static TrustPlugin plugin;
    private DatabaseManager databaseManager;

    private ConfigManager configManager;

    @Override
    public void onEnable() {
        plugin = this;
        databaseManager = new DatabaseManager(getDataFolder().getPath() + "/database/database.db");
        databaseManager.createTable(DatabaseTable.PLAYER_TRUSTS);
        databaseManager.createTable(DatabaseTable.TRUST_CHANGES);
        saveDefaultConfig();
        configManager = new ConfigManager(this);



        Bukkit.getPluginManager().registerEvents(new org.referix.event.PlayerEvent(databaseManager), this);

        new AddReputation("addrep", databaseManager,configManager);
        new RemoveReputation("removerep", databaseManager, configManager);
        new ListReputation("listrep", databaseManager,configManager);
        new TrustAccept("trustaccept", databaseManager, configManager);
        new TrustDeny("trustdeny",databaseManager, configManager);
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
