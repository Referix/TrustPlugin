package org.referix.trustPlugin;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.referix.commands.command.*;
import org.referix.commands.command.admincommand.ListReputation;
import org.referix.commands.command.admincommand.MainCommand;
import org.referix.commands.command.utilcommand.TrustAccept;
import org.referix.commands.command.utilcommand.TrustDeny;
import org.referix.database.DatabaseFactory;
import org.referix.database.DatabaseProvider;
import org.referix.database.DatabaseTable;
import org.referix.database.TableCreateCallback;
import org.referix.event.ReputationListener;
import org.referix.savezone.ListenerSaveZone;
import org.referix.savezone.SafeZoneManager;
import org.referix.utils.*;

import java.util.Arrays;
import java.util.List;

public final class TrustPlugin extends JavaPlugin {

    private static TrustPlugin plugin;
    private DatabaseProvider database;
    private PlayerDataCache playerDataCache;
    private ConfigManager configManager;
    private FileLogger logger;

    private String serverID;

    private SafeZoneManager safeZoneManager;

    private boolean debugEnabled = false; // перемикач

    @Override
    public void onEnable() {
        configManager = new ConfigManager(this);
        plugin = this;

        try {
            initDatabaseAndTables();
        } catch (Exception e) {
            Bukkit.getLogger().severe("[TrustPlugin] Failed to enable plugin: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initDatabaseAndTables() {
        database = DatabaseFactory.createDatabase();
        database.connect();

        List<DatabaseTable> tables = Arrays.asList(
                DatabaseTable.PLAYER_TRUSTS,
                DatabaseTable.TRUST_CHANGES,
                DatabaseTable.PLAYER_LINES,
                DatabaseTable.SAFE_ZONE
        );

        final int totalTables = tables.size();
        final int[] successCount = {0};

        for (DatabaseTable table : tables) {
            database.createTable(table, new TableCreateCallback() {
                @Override
                public void onSuccess() {
                    successCount[0]++;
                    if (successCount[0] == totalTables) {
                        Bukkit.getLogger().info("[TrustPlugin] Database tables created successfully.");
                        // Всі таблиці створені, викликаємо логіку плагіну
                        Bukkit.getScheduler().runTask(TrustPlugin.getInstance(), () -> {
                                loadPluginLogic();
                        });
                    }
                }

                @Override
                public void onError(Exception e) {
                    Bukkit.getLogger().severe("[TrustPlugin] Error creating table " + table.getTableName() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            });
        }
    }


    private void loadPluginLogic() {
        try {
            PermissionUtil.init();
            safeZoneManager = new SafeZoneManager(database, configManager, () -> {
                try {
                    Bukkit.getLogger().info("[TrustPlugin] Safe zones loaded. Continuing plugin logic...");
                    continuePluginInit(); // вся логіка, що була після safeZoneManager
                } catch (Exception e) {
                    Bukkit.getLogger().severe("[TrustPlugin] Failed during logic after safe zone load: " + e.getMessage());
                    e.printStackTrace();
                }
            });

        } catch (Exception e) {
            Bukkit.getLogger().severe("[TrustPlugin] Error during plugin logic initialization: " + e.getMessage());
            throw e;
        }
    }

    private void continuePluginInit() {
        try {

            playerDataCache = new PlayerDataCache(configManager);
            playerDataCache.loadCacheAsync(database, () -> {
                Bukkit.getPluginManager().registerEvents(new org.referix.event.PlayerEvent(database, playerDataCache), this);
                logger = new FileLogger(this);

                Bukkit.getPluginManager().registerEvents(new ReputationListener(this, playerDataCache), this);
                Bukkit.getPluginManager().registerEvents(new ListenerSaveZone(safeZoneManager, playerDataCache), this);

                new AddReputation("trust", database);
                new RemoveReputation("untrust", database);
                new TrustAccept("trustaccept", database, playerDataCache, logger);
                new TrustDeny("trustdeny", database, logger);
                new MainCommand("trusts", database, playerDataCache);
                new SafeZonePlayerCreate("safezone", configManager, database, safeZoneManager);

                new PlayerTrustPlaceholders(this, playerDataCache).register();
                Bukkit.getLogger().info("[TrustPlugin] Plugin logic initialized successfully!");
            });
        } catch (Exception e) {
            Bukkit.getLogger().severe("[TrustPlugin] Error during plugin logic initialization: " + e.getMessage());
            throw e;
        }
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

    public String getServerID() {
        return serverID;
    }

    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    public void setDebugEnabled(boolean debugEnabled) {
        this.debugEnabled = debugEnabled;
    }

    public void debug(String message) {
        if (debugEnabled) {
            getLogger().info("[DEBUG] " + message);
        }
    }

    public void setServerID(String serverID) {
        this.serverID = serverID;
    }
}
