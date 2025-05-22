package org.referix.savezone;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.referix.utils.PlayerDataCache;

import java.util.UUID;

public class ListenerSaveZone implements Listener {
    private final SafeZoneManager safeZoneManager;
    private final PlayerDataCache playerDataCache;

    public ListenerSaveZone(SafeZoneManager safeZoneManager, PlayerDataCache playerDataCache) {
        this.safeZoneManager = safeZoneManager;
        this.playerDataCache = playerDataCache;
    }

    private boolean isInSafeZoneAndNoTrusted(Location loc, UUID playerUUID) {
        // Якщо гравець у своїй зоні — дозволити
        if (safeZoneManager.isPlayerSafeZone(loc, Bukkit.getPlayer(playerUUID))) {
            return false;
        }

        // Якщо гравець trusted — дозволити
        if (playerDataCache.isTrusted(playerUUID)) {
            return false;
        }

        // Якщо це загальна безпечна зона і гравець не trusted і не власник — заборонити
        return safeZoneManager.isSafeZone(loc);
    }



    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (isInSafeZoneAndNoTrusted(event.getBlock().getLocation(), event.getPlayer().getUniqueId())) {
            event.getPlayer().sendMessage("Тут заборонено ламати блоки!");
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (isInSafeZoneAndNoTrusted(event.getBlock().getLocation(), event.getPlayer().getUniqueId())) {
            event.getPlayer().sendMessage("Тут заборонено ставити блоки!");
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.hasBlock() && isInSafeZoneAndNoTrusted(event.getClickedBlock().getLocation(), event.getPlayer().getUniqueId())) {
            event.getPlayer().sendMessage("Взаємодія з блоками заборонена в безпечній зоні!");
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        if (isInSafeZoneAndNoTrusted(event.getPlayer().getLocation(), event.getPlayer().getUniqueId())) {
            event.getPlayer().sendMessage("Підйом предметів заборонено в безпечній зоні!");
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (isInSafeZoneAndNoTrusted(event.getPlayer().getLocation(), event.getPlayer().getUniqueId())) {
            event.getPlayer().sendMessage("Скидання предметів заборонено в безпечній зоні!");
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof org.bukkit.entity.Player) {
            org.bukkit.entity.Player player = (org.bukkit.entity.Player) event.getEntity();
            if (isInSafeZoneAndNoTrusted(player.getLocation(), player.getUniqueId())) {
                event.setCancelled(true);
                player.sendMessage("У безпечній зоні не можна отримувати шкоду!");
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof org.bukkit.entity.Player) {
            org.bukkit.entity.Player damaged = (org.bukkit.entity.Player) event.getEntity();

            boolean damagedInSafeZone = isInSafeZoneAndNoTrusted(damaged.getLocation(), damaged.getUniqueId());

            if (event.getDamager() instanceof org.bukkit.entity.Player) {
                org.bukkit.entity.Player damager = (org.bukkit.entity.Player) event.getDamager();
                boolean damagerInSafeZone = isInSafeZoneAndNoTrusted(damager.getLocation(), damager.getUniqueId());

                if (damagedInSafeZone || damagerInSafeZone) {
                    event.setCancelled(true);
                    damager.sendMessage("У безпечній зоні не можна завдавати шкоди!");
                }
            } else {
                if (damagedInSafeZone) {
                    event.setCancelled(true);
                    damaged.sendMessage("У безпечній зоні не можна отримувати шкоду!");
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (isInSafeZoneAndNoTrusted(event.getPlayer().getLocation(), event.getPlayer().getUniqueId())) {
            event.getPlayer().sendMessage("Взаємодія з ентіті заборонена в безпечній зоні!");
            event.setCancelled(true);
        }
    }
}
