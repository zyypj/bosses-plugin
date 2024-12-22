package com.github.zyypj.bosses.menu;

import com.github.zyypj.bosses.BossesPlugin;
import com.github.zyypj.bosses.config.BossConfig;
import com.github.zyypj.bosses.config.RecompensasConfig;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
public class BossRewardsMenu {

    private final BossesPlugin plugin;

    public void openMenu(Player player, String bossName) {
        BossConfig bossConfig = plugin.getBossConfig();
        RecompensasConfig recompensasConfig = plugin.getRecompensasConfig();

        List<String> rewards = bossConfig.getBossConfig()
                .getStringList("bosses." + bossName + ".recompensas");

        if (rewards.isEmpty()) {
            player.sendMessage(plugin.getMessagesConfig().getMessage("boss-no-have-awards"));
            return;
        }

        Inventory menu = Bukkit.createInventory(null, 54, plugin.getMessagesConfig()
                .getMessage("reward.menu-title").replace("{BOSS-NAME}", bossName));

        int[] centerSlots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24 ,25, 28, 29, 30, 31, 32, 33, 34};
        int currentIndex = 0;

        for (String rewardEntry : rewards) {
            if (currentIndex >= centerSlots.length) break;

            String[] parts = rewardEntry.split(",");
            if (parts.length != 2) continue;

            int chance;
            try {
                chance = Integer.parseInt(parts[0]);
            } catch (NumberFormatException e) {
                plugin.debug("&4Erro ao interpretar a chance de recompensa: " + rewardEntry, false);
                continue;
            }

            String rewardKey = parts[1];

            ItemStack rewardItem = recompensasConfig.getRewardItem(rewardKey);
            if (rewardItem == null) {
                plugin.debug("&cRecompensa não encontrada: " + rewardKey, false);
                continue;
            }

            ItemMeta meta = rewardItem.getItemMeta();
            if (meta != null) {
                List<String> lore = recompensasConfig.getRecompensasConfig()
                        .getStringList("recompensas." + rewardKey + ".item.lore");
                List<String> newLore = new ArrayList<>(lore);
                String loreAddedRaw = plugin.getMessagesConfig().getMessage("reward-menu.lore-added");
                List<String> loreAdded = loreAddedRaw.contains("\n")
                        ? Arrays.asList(loreAddedRaw.split("\\n"))
                        : Arrays.asList(loreAddedRaw);

                for (String loreLine : loreAdded) {
                    newLore.add(loreLine.replace("{CHANCE}", String.valueOf(chance)));
                }
                meta.setLore(newLore);
                rewardItem.setItemMeta(meta);
            }

            menu.setItem(centerSlots[currentIndex], rewardItem);
            currentIndex++;
        }

        player.openInventory(menu);
    }
}
