package com.github.zyypj.bosses.commands;

import com.github.zyypj.bosses.BossesPlugin;
import com.github.zyypj.bosses.config.BossConfig;
import com.github.zyypj.bosses.config.MatadoraConfig;
import com.github.zyypj.bosses.config.MessagesConfig;
import lombok.RequiredArgsConstructor;
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

@RequiredArgsConstructor
public class BossesCommand implements CommandExecutor, TabCompleter {

    private final BossesPlugin plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        MessagesConfig messagesConfig = plugin.getMessagesConfig();

        if (!sender.hasPermission("bosses.reload")
                && !sender.hasPermission("bosses.give")) {
            sender.sendMessage(messagesConfig.getMessage("no-permission"));
            return false;
        }

        if (args.length == 0) {
            sender.sendMessage("§cUse: /bosses reload ou /bosses give <boss|matadora> <nome> <jogador>");
            return false;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                if (!sender.hasPermission("bosses.reload")) {
                    sender.sendMessage(messagesConfig.getMessage("no-permission"));
                    return false;
                }

                plugin.getBossConfig().load();
                plugin.getMatadoraConfig().load();
                plugin.getRecompensasConfig().load();
                plugin.getMatadoraConfig().load();

                sender.sendMessage("§aConfigurações recarragadas com sucesso!");
                return true;

            case "give":
                if (!sender.hasPermission("bosses.give")) {
                    sender.sendMessage(messagesConfig.getMessage("no-permission"));
                    return false;
                }

                if (args.length != 4) {
                    sender.sendMessage("§cUse: /bosses give <boss|matadora> <nome> <jogador>");
                    return false;
                }

                String type = args[1].toLowerCase();
                String name = args[2];
                Player target = Bukkit.getPlayer(args[3]);

                if (target == null) {
                    sender.sendMessage(messagesConfig.getMessage("player-not-found")
                            .replace("{NAME}", args[3]));
                    return false;
                }

                switch (type) {
                    case "boss":
                        BossConfig bossConfig = plugin.getBossConfig();
                        ItemStack bossEgg = bossConfig.getBossEgg(name);
                        if (bossEgg == null) {
                            sender.sendMessage(messagesConfig.getMessage("boss-not-found")
                                    .replace("{NAME}", name));
                            return false;
                        }

                        target.getInventory().addItem(bossEgg);
                        target.sendMessage(messagesConfig.getMessage("receive-boss")
                                .replace("{AMOUNT}", "1")
                                .replace("{BOSS-NAME}", bossEgg.getItemMeta().getDisplayName()));

                        sender.sendMessage("§aVocê enviou 1x " + bossEgg.getItemMeta().getDisplayName() + " para " + target.getName() + "!");
                        return true;

                    case "matadora":
                        MatadoraConfig matadoraConfig = plugin.getMatadoraConfig();
                        ItemStack matadora = matadoraConfig.getMatadoraItem(name);
                        if (matadora == null) {
                            sender.sendMessage(messagesConfig.getMessage("matadora-not-found")
                                    .replace("{NAME}", name));
                            return false;
                        }

                        target.getInventory().addItem(matadora);
                        target.sendMessage(messagesConfig.getMessage("receive-matadora")
                                .replace("{AMOUNT}", "1")
                                .replace("{MATADORA-NAME}", matadora.getItemMeta().getDisplayName()));

                        sender.sendMessage("§aVocê enviou a matadora " + matadora.getItemMeta().getDisplayName() + " para " + target.getName() + "!");
                        return true;

                    default:
                        sender.sendMessage("§cTipo inválido. Use 'boss' ou 'matadora'.");
                        return false;
                }
            default:
                sender.sendMessage("§cUse: /bosses reload ou /bosses give <boss|matadora> <nome> <jogador>");
                return false;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("bosses.give")) return Collections.emptyList();

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("reload");
            completions.add("give");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            completions.add("boss");
            completions.add("matadora");
        } else if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            if (args[1].equalsIgnoreCase("boss")) {
                completions.addAll(plugin.getBossConfig().getBosses());
            } else if (args[1].equalsIgnoreCase("matadora")) {
                completions.addAll(plugin.getMatadoraConfig().getMatadoras());
            }
        } else if (args.length == 4 && args[0].equalsIgnoreCase("give")) {
            Bukkit.getOnlinePlayers().forEach(player -> completions.add(player.getName()));
        }
        return completions;
    }
}