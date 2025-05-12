package org.referix.utils;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import org.bukkit.OfflinePlayer;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PermissionUtil {

    private static LuckPerms luckPerms;

    public static void init() {
        if (luckPerms == null) {
            luckPerms = LuckPermsProvider.get();
        }
    }



    public static CompletableFuture<Void> givePermission(UUID playerUUID, String permission) {
        if (luckPerms == null) return CompletableFuture.completedFuture(null);

        return luckPerms.getUserManager().loadUser(playerUUID).thenAcceptAsync(user -> {
            Node node = net.luckperms.api.node.types.PermissionNode.builder(permission).build();
            user.data().add(node);
            System.out.println("Додаю право " + permission + " гравцю " + user.getUsername());
            luckPerms.getUserManager().saveUser(user);
        }).thenRun(() -> {
            // Завершення операції, повертає CompletableFuture<Void>
        });
    }

    public static CompletableFuture<Void> removePermission(UUID playerUUID, String permission) {
        if (luckPerms == null) return CompletableFuture.completedFuture(null);

        return luckPerms.getUserManager().loadUser(playerUUID).thenAcceptAsync(user -> {
            Node node = Node.builder(permission).value(true).build();
            user.data().remove(node);
            System.out.println("Забираю право " + permission + " гравцю " + user.getUsername());
            luckPerms.getUserManager().saveUser(user);
        }).thenRun(() -> {
            // Завершення операції, повертає CompletableFuture<Void>
        });
    }


    public static CompletableFuture<Boolean> hasPermission(UUID playerUUID, String permission) {
        if (luckPerms == null) return CompletableFuture.completedFuture(false);

        return luckPerms.getUserManager().loadUser(playerUUID)
                .thenApply(user -> user.getCachedData().getPermissionData().checkPermission(permission).asBoolean());
    }

    public static CompletableFuture<Boolean> hasPermission(OfflinePlayer player, String permission) {
        return hasPermission(player.getUniqueId(), permission);
    }


    public static void removePermission(OfflinePlayer player, String permission) {
        removePermission(player.getUniqueId(), permission);
    }
}


