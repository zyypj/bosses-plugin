package com.github.zyypj.bosses.listeners;

import com.github.zyypj.bosses.BossesPlugin;
import com.github.zyypj.bosses.utils.ActionBar;
import com.github.zyypj.bosses.utils.ProgressBar;
import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;

import java.util.List;
import java.util.Random;

@RequiredArgsConstructor
public class BossDamageListener implements Listener {

    private final BossesPlugin plugin;
    private final Random random = new Random();

    @EventHandler
    public void onBossDamage(EntityDamageByEntityEvent e) {

        if (!(e.getEntity() instanceof LivingEntity)) return;
        if (!(e.getDamager() instanceof Player)) return;

        LivingEntity boss = (LivingEntity) e.getEntity();
        Player player = (Player) e.getDamager();

        if (!boss.hasMetadata("bossName")) return;

        String bossName = getBossMetadata(boss);
        if (bossName == null) return;

        boolean onlyMatadora = plugin.getBossConfig().getBossConfig()
                .getBoolean("bosses." + bossName + ".only-matadora", false);

        ItemStack itemInHand = player.getInventory().getItemInHand();
        double damage = e.getDamage();

        if (onlyMatadora && !isMatadora(itemInHand)) {
            e.setCancelled(true);
            return;
        }

        if (isMatadora(itemInHand)) {
            String matadoraName = getMatadoraMetadata(itemInHand, "matadoraName");
            if (matadoraName != null) {
                damage = plugin.getMatadoraConfig().getMatadoraDamage(matadoraName);
                e.setDamage(damage);
            }
        }

        double health = boss.getHealth() - damage;
        health = Math.max(0, health);

        String actionBarMessage = plugin.getBossConfig().getBossConfig()
                .getString("bosses." + bossName + ".action-bar.on-hit", "");

        if (!actionBarMessage.isEmpty()) {
            String formattedMessage = ChatColor.translateAlternateColorCodes('&', actionBarMessage)
                    .replace("{HEALTH}", String.valueOf((int) health))
                    .replace("{DAMAGE}", String.valueOf((int) damage))
                    .replace("{PROGRESS-BAR}", ProgressBar.getBar((health / boss.getMaxHealth()) * 100, "|", "-", "§a", "§c", 10));

            ActionBar.sendActionBarMessage(player, formattedMessage);
        }
    }

    @EventHandler
    public void onBossDeath(EntityDeathEvent e) {
        LivingEntity boss = e.getEntity();

        if (!boss.hasMetadata("bossName")) return;

        e.getDrops().clear();

        String bossName = getBossMetadata(boss);
        if (bossName == null) return;

        List<String> rewards = plugin.getBossConfig().getBossConfig()
                .getStringList("bosses." + bossName + ".recompensas");

        if (rewards.isEmpty()) return;

        Player player = boss.getKiller();
        if (player == null) return;

        for (String reward : rewards) {
            String[] parts = reward.split(",");
            if (parts.length != 2) continue;

            int chance = Integer.parseInt(parts[0]);
            String rewardKey = parts[1];

            if (random.nextInt(100) < chance) {
                ItemStack rewardItem = plugin.getRecompensasConfig().getRewardItem(rewardKey);
                if (rewardItem != null) {
                    if (player.getInventory().firstEmpty() != -1) {
                        player.getInventory().addItem(rewardItem);
                    } else {
                        player.getWorld().dropItemNaturally(player.getLocation(), rewardItem);
                    }
                }
            }
        }

        List<String> messages = plugin.getBossConfig().getBossConfig()
                .getStringList("bosses." + bossName + ".message-for-kill");

        for (String message : messages) {
            player.sendMessage(message);
        }
    }

    private String getBossMetadata(LivingEntity entity) {
        for (MetadataValue value : entity.getMetadata("bossName")) {
            if (value.getOwningPlugin() == plugin) {
                return value.asString();
            }
        }

        return null;
    }

    private String getMatadoraMetadata(ItemStack item, String key) {
        if (item == null || !item.hasItemMeta()) return null;
        if (!item.getItemMeta().hasLore()) return null;

        List<String> lore = item.getItemMeta().getLore();
        for (String line : lore) {
            if (ChatColor.stripColor(line).contains(key + ":")) {
                return ChatColor.stripColor(line).split(":" , 2)[1].trim();
            }
        }

        return null;
    }

    private boolean isMatadora(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getLore() != null && item.getItemMeta().getLore().stream()
                .anyMatch(line -> ChatColor.stripColor(line).contains("Matadora"));
    }
}
