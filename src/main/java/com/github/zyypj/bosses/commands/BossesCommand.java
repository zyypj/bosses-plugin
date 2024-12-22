package com.github.zyypj.bosses.commands;

import com.github.zyypj.bosses.BossesPlugin;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class BossesCommand implements CommandExecutor {

    private final BossesPlugin plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        if (!(sender instanceof Player)) return false;

        Player player = (Player) sender;

        if (!player.hasPermission("bosses.reload")) {
            player.sendMessage(plugin.getMessagesConfig().getMessage("no-permission"));
            return false;
        }

        if (args.length != 1) {
            player.sendMessage("§cUse: /bosses reload");
            return false;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            plugin.getBossConfig().load();
            plugin.getMatadoraConfig().load();
            plugin.getRecompensasConfig().load();
            plugin.getMessagesConfig().reload();

            player.sendMessage("§aConfigurações recarregadas com sucesso!");
            return true;
        }

        player.sendMessage("§cUse: /bosses reload");
        return false;
    }
}
