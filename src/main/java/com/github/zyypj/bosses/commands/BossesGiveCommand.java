package com.github.zyypj.bosses.commands;

import com.github.zyypj.bosses.BossesPlugin;
import com.github.zyypj.bosses.config.BossConfig;
import com.github.zyypj.bosses.config.MessagesConfig;
import com.github.zyypj.bosses.config.MatadoraConfig;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BossesGiveCommand implements CommandExecutor, TabCompleter {

    private final BossesPlugin plugin;
    private final MessagesConfig messagesConfig;
    private final BossConfig bossConfig;
    private final MatadoraConfig matadoraConfig;

    public BossesGiveCommand(BossesPlugin plugin) {
        this.plugin = plugin;
        this.messagesConfig = plugin.getMessagesConfig();
        this.bossConfig = plugin.getBossConfig();
        this.matadoraConfig = plugin.getMatadoraConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("bosses.give")) {
            sender.sendMessage(messagesConfig.getMessage("no-permission"));
            plugin.debug("O comando foi tentado sem permissão por: " + sender.getName(), true);
            return false;
        }

        if (args.length != 3) {
            sender.sendMessage("§cUse: /bossesGive <boss|matadora> <nome> <jogador>");
            return false;
        }

        String type = args[0].toLowerCase();
        String name = args[1];
        Player target = Bukkit.getPlayer(args[2]);

        if (target == null) {
            sender.sendMessage(plugin.getMessagesConfig().getMessage("player-not-found")
                    .replace("{NAME}", args[2]));
            return false;
        }

        switch (type) {
            case "boss":
                ItemStack bossEgg = bossConfig.getBossEgg(name);
                if (bossEgg == null) {
                    sender.sendMessage(plugin.getMessagesConfig().getMessage("boss-not-found")
                            .replace("{NAME}", name));
                    return false;
                }

                target.getInventory().addItem(bossEgg);
                target.sendMessage(messagesConfig.getMessage("receive-boss")
                        .replace("{AMOUNT}", "1")
                        .replace("{BOSS-NAME}", bossEgg.getItemMeta().getDisplayName()));

                sender.sendMessage("§aVocê enviou 1x bosses de "
                        + bossEgg.getItemMeta().getDisplayName()
                        + " para " + target.getName() + "!");
                plugin.debug(sender.getName() + " deu um boss " + bossEgg.getItemMeta().getDisplayName() + " para " + target.getName(), true);
                break;

            case "matadora":
                ItemStack matadora = matadoraConfig.getMatadoraItem(name);
                if (matadora == null) {
                    sender.sendMessage(plugin.getMessagesConfig().getMessage("matadora-not-found")
                            .replace("{NAME}", name));
                    return false;
                }

                target.getInventory().addItem(matadora);
                target.sendMessage(messagesConfig.getMessage("receive-matadora")
                        .replace("{AMOUNT}", "1")
                        .replace("{MATADORA-NAME}", matadora.getItemMeta().getDisplayName()));

                sender.sendMessage("§aVocê enviou a matadora "
                        + matadora.getItemMeta().getDisplayName()
                        + " para " + target.getName() + "!");
                plugin.debug(sender.getName() + " deu uma matadora " + matadora.getItemMeta().getDisplayName() + " para " + target.getName(), true);
                break;

            default:
                sender.sendMessage("§cTipo inválido. Use 'boss' ou 'matadora'.");
                break;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("bosses.give")) {
            return Collections.emptyList();
        }

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("boss");
            completions.add("matadora");
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("boss")) {
                completions.addAll(bossConfig.getBosses());
            } else if (args[0].equalsIgnoreCase("matadora")) {
                completions.addAll(matadoraConfig.getMatadoras());
            }
        } else if (args.length == 3) {
            Bukkit.getOnlinePlayers().forEach(player -> completions.add(player.getName()));
        }

        return completions;
    }
}