package org.referix.trustPlugin;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.referix.commands.command.AddReputation;
import org.referix.commands.command.ListReputation;
import org.referix.commands.command.RemoveReputation;
import org.referix.database.DatabaseManager;
import org.referix.database.DatabaseTable;
import org.referix.database.pojo.PlayerTrustDB;
import org.referix.database.pojo.TrustChangeDB;

import java.util.UUID;

public final class TrustPlugin extends JavaPlugin {

    private static TrustPlugin plugin;
    private DatabaseManager databaseManager;

    @Override
    public void onEnable() {
        plugin = this;
        databaseManager = new DatabaseManager(getDataFolder().getPath() + "/database/database.db");
        databaseManager.createTable(DatabaseTable.PLAYER_TRUSTS);
        databaseManager.createTable(DatabaseTable.TRUST_CHANGES);

        Bukkit.getPluginManager().registerEvents(new org.referix.event.PlayerEvent(databaseManager), this);

        new AddReputation("addrep", databaseManager);
        new RemoveReputation("removerep", databaseManager);
        new ListReputation("listrep", databaseManager);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static TrustPlugin getInstance() {
        return plugin;
    }
}
