package com.github.zyypj.bosses.listeners;

import com.github.zyypj.bosses.BossesPlugin;
import com.github.zyypj.bosses.api.events.BossDeathEvent;
import com.github.zyypj.bosses.utils.ActionBar;
import com.github.zyypj.bosses.utils.ProgressBar;
import de.tr7zw.nbtapi.NBTItem;
import lombok.RequiredArgsConstructor;
import lombok.var;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
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

        var bossConfig = plugin.getBossConfig().getBossConfig().getConfigurationSection("bosses." + bossName);

        boolean onlyMatadora = bossConfig.getBoolean("only-matadora");

        ItemStack itemInHand = player.getInventory().getItemInHand();
        double damage = e.getDamage();

        if (onlyMatadora && !isMatadora(itemInHand)) {
            player.sendMessage(plugin.getMessagesConfig().getMessage("only-matadora"));
            e.setCancelled(true);
            return;
        }

        if (isMatadora(itemInHand)) {
            String matadoraName = getMatadoraMetadata(itemInHand, "matadoraName");
            if (matadoraName != null) {
                damage = plugin.getMatadoraConfig().getMatadoraDamage(matadoraName);

                if (damage == 0) {
                    damage = 999999999;
                }

                e.setDamage(damage);
            }
        }

        double health = boss.getHealth() - damage;
        health = Math.max(0, health);

        String actionBarMessage = bossConfig.getString("action-bar.on-hit");

        if (!actionBarMessage.isEmpty()) {
            int totalBars = 30;
            int healthBars = (int) Math.round((health / boss.getMaxHealth()) * totalBars);
            int emptyBars = totalBars - healthBars;

            StringBuilder healthBar = new StringBuilder();
            for (int i = 0; i < healthBars; i++) {
                healthBar.append("|");
            }

            StringBuilder emptyBar = new StringBuilder();
            for (int i = 0; i < emptyBars; i++) {
                emptyBar.append("|");
            }

            String formattedMessage = ChatColor.translateAlternateColorCodes('&', actionBarMessage)
                    .replace("{HEALTH}", String.valueOf((int) health))
                    .replace("{DAMAGE}", String.valueOf((int) damage))
                    .replace("{PROGRESS-BAR}", "§a" + healthBar + "§c" + emptyBar);

            ActionBar.sendActionBarMessage(player, formattedMessage);
        }

        boss.setCustomName(ChatColor.translateAlternateColorCodes('&',
                bossConfig.getString("name", bossName)
                        .replace("{HEALTH}", String.valueOf(boss.getHealth() - damage))));
        boss.setCustomNameVisible(true);
    }

    @EventHandler
    public void onBossDeath(EntityDeathEvent e) {
        LivingEntity boss = e.getEntity();

        if (!boss.hasMetadata("bossName")) return;

        e.getDrops().clear();
        e.setDroppedExp(0);

        String bossName = getBossMetadata(boss);
        if (bossName == null) return;

        List<String> rewards = plugin.getBossConfig().getBossConfig()
                .getStringList("bosses." + bossName + ".recompensas");

        if (rewards.isEmpty()) return;

        Player player = boss.getKiller();
        if (player == null) return;

        ItemStack item = player.getInventory().getItemInHand();
        boolean isMatadora = isMatadora(item);
        String matadoraName = isMatadora ? getMatadoraMetadata(item, "matadoraName") : null;

        BossDeathEvent bossDeathEvent = new BossDeathEvent(player, boss, bossName, item, isMatadora, matadoraName);
        plugin.getServer().getPluginManager().callEvent(bossDeathEvent);

        if (bossDeathEvent.isCancelled()) return;

        plugin.getDatabaseManager().addBossKilled(player);

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
            player.sendMessage(message.replace("&", "§"));
        }
    }

    @EventHandler
    public void onMatadoraDamage(PlayerItemDamageEvent e) {
        ItemStack item = e.getItem();
        if (isMatadora(item)) e.setCancelled(true);
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
        if (item == null) return null;

        NBTItem nbtItem = new NBTItem(item);
        return nbtItem.getString(key);
    }

    private boolean isMatadora(ItemStack item) {
        if (item == null || item.getType().equals(Material.AIR)) return false;

        NBTItem nbtItem = new NBTItem(item);
        return "matadora".equalsIgnoreCase(nbtItem.getString("itemType"));
    }
}