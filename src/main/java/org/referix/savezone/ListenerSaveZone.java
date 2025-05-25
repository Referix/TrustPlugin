package org.referix.savezone;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.*;
import org.referix.trustPlugin.TrustPlugin;
import org.referix.utils.PlayerDataCache;

import java.util.UUID;

public class ListenerSaveZone implements Listener {
    private final SafeZoneManager safeZoneManager;
    private final PlayerDataCache playerDataCache;
    private final Component message = TrustPlugin.getInstance().getConfigManager().getMessage("in_safe_zone");

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
            event.getPlayer().sendMessage(message);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (isInSafeZoneAndNoTrusted(event.getBlock().getLocation(), event.getPlayer().getUniqueId())) {
            event.getPlayer().sendMessage(message);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.hasBlock()) {
            Block clicked = event.getClickedBlock();
            if (clicked != null && isInSafeZoneAndNoTrusted(clicked.getLocation(), event.getPlayer().getUniqueId())) {
                // Дозволити клік по верстаку
                if (clicked.getType() == Material.CRAFTING_TABLE) return;

                event.getPlayer().sendMessage(message);
                event.setCancelled(true);
            }
        }
    }


    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        if (event.getRightClicked() instanceof ArmorStand) {
            if (isInSafeZoneAndNoTrusted(event.getRightClicked().getLocation(), event.getPlayer().getUniqueId())) {
                event.getPlayer().sendMessage(message);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onHangingBreakByEntity(HangingBreakByEntityEvent event) {
        if (event.getRemover() instanceof Player player) {
            if (isInSafeZoneAndNoTrusted(event.getEntity().getLocation(), player.getUniqueId())) {
                player.sendMessage(message);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (isInSafeZoneAndNoTrusted(event.getPlayer().getLocation(), event.getPlayer().getUniqueId())) {
            if (event.getRightClicked() instanceof ArmorStand) {
                event.getPlayer().sendMessage(message);
                event.setCancelled(true);
            }
            if (isInSafeZoneAndNoTrusted(event.getRightClicked().getLocation(), event.getPlayer().getUniqueId())) {
                if (event.getRightClicked() instanceof ArmorStand || event.getRightClicked() instanceof ItemFrame) {
                    event.getPlayer().sendMessage(message);
                    event.setCancelled(true);
                }
            }
        }
    }

}
