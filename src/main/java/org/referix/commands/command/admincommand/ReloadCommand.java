package org.referix.commands.command.admincommand;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.referix.trustPlugin.TrustPlugin;

public class ReloadCommand implements HelperCommand{

    public ReloadCommand(Player p, String[] args) {
        execute(p, args);
    }

    @Override
    public boolean execute(Player p, String[] args) {
        if (!p.hasPermission("trust.admin.reload")){
            p.sendMessage(TrustPlugin.getInstance().getConfigManager().getMessage("no_permission"));
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            TrustPlugin.getInstance().reloadConfig(); // перезавантаження .yml
            TrustPlugin.getInstance().getConfigManager().reloadConfig(); // оновлення кешу полів
            p.sendMessage("§aКонфігурація успішно перезавантажена.");
            return true;
        }

        p.sendMessage("§cВикористання: /trust reload");
        return false;
    }
}
