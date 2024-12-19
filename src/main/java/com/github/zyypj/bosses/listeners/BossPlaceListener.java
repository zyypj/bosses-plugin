package com.github.zyypj.bosses.listeners;

import com.github.zyypj.bosses.BossesPlugin;
import com.github.zyypj.bosses.utils.ActionBar;
import com.github.zyypj.bosses.utils.ProgressBar;
import de.tr7zw.nbtapi.NBTItem;
import lombok.RequiredArgsConstructor;
import lombok.var;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

@RequiredArgsConstructor
public class BossPlaceListener implements Listener {

    private final BossesPlugin plugin;

    @EventHandler
    public void onPlayerPlaceBoss(PlayerInteractEvent e) {

        Player player = e.getPlayer();
        ItemStack item = e.getItem();

        if (item == null || !item.hasItemMeta()) return;

        NBTItem nbtItem = new NBTItem(item);
        String bossName = nbtItem.getString("bossName");

        if (bossName == null || bossName.isEmpty()) return;

        e.setCancelled(true);

        var bossConfig = plugin.getBossConfigManager().getBossConfig().getConfigurationSection("bosses." + bossName);

        if (bossConfig == null) {
            player.sendMessage("§cBoss não encontrado, contacte um administrador: " + bossName);
            plugin.debug("§cBoss não encontrado: " + bossName, false);
            return;
        }

        EntityType entityType;
        try {
            entityType = EntityType.valueOf(bossConfig.getString("entity", "ZOMBIE"));
        } catch (IllegalArgumentException ex) {
            player.sendMessage("§cEntidade não encontrada, contacte um administrador: " + bossName);
            Bukkit.getConsoleSender().sendMessage(ex.toString());
            return;
        }

        LivingEntity boss = (LivingEntity) player.getWorld().spawnEntity(player.getLocation(), entityType);

        boss.setCustomName(ChatColor.translateAlternateColorCodes('&', bossConfig.getString("name", bossName)));
        boss.setCustomNameVisible(true);
        boss.setMetadata("boss", new FixedMetadataValue(plugin, bossName));

        double health = bossConfig.getDouble("health", 100.0);
        boss.setMaxHealth(health);
        boss.setHealth(health);

        plugin.debug("§aBoss " + bossName + " colocado por " + player.getName(), true);

        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().setItemInHand(new ItemStack(Material.AIR));
        }

        String actionBarMessage = bossConfig.getString("action-bar.on-interact");

        if (actionBarMessage != null && !actionBarMessage.isEmpty()) {
            String formattedMessage = ChatColor.translateAlternateColorCodes('&', actionBarMessage)
                    .replace("{HEALTH}", String.valueOf(boss.getHealth()))
                    .replace("{PROGRESS-BAR}", ProgressBar.getBar(100, "|", "-", "§a", "§c", 10));

            plugin.debug("§aActionBar criada para " + player.getName(), true);
            ActionBar.sendActionBarMessage(player, formattedMessage);
        }

        player.sendMessage("§aVocê colocou um boss!");
    }
}
