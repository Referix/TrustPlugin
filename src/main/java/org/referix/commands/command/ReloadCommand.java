package org.referix.commands.command;

import org.bukkit.command.CommandSender;
import org.referix.commands.AbstractCommand;
import org.referix.trustPlugin.TrustPlugin;
import org.referix.utils.ConfigManager;

public class ReloadCommand extends AbstractCommand {

    public ReloadCommand(String name) {
        super(name);
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            TrustPlugin.getInstance().reloadConfig(); // перезавантаження .yml
            TrustPlugin.getInstance().getConfigManager().reloadConfig(); // оновлення кешу полів
            sender.sendMessage("§aКонфігурація успішно перезавантажена.");
            return true;
        }

        sender.sendMessage("§cВикористання: /trust reload");
        return false;
    }
}
