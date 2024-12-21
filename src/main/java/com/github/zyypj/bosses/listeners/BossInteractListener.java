package com.github.zyypj.bosses.listeners;

import com.github.zyypj.bosses.BossesPlugin;
import com.github.zyypj.bosses.utils.ActionBar;
import com.github.zyypj.bosses.utils.ProgressBar;
import de.tr7zw.nbtapi.NBTEntity;
import de.tr7zw.nbtapi.NBTItem;
import lombok.RequiredArgsConstructor;
import lombok.var;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

@RequiredArgsConstructor
public class BossInteractListener implements Listener {

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

        var bossConfig = plugin.getBossConfig().getBossConfig().getConfigurationSection("bosses." + bossName);

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
            plugin.debug("§cErro ao identificar a entidade: " + ex.getMessage(), true);
            return;
        }

        LivingEntity boss = (LivingEntity) player.getWorld().spawnEntity(player.getLocation(), entityType);

        NBTEntity nbtEntity = new NBTEntity(boss);
        nbtEntity.setString("bossName", bossName);

        boss.setMetadata("bossName", new FixedMetadataValue(plugin, bossName));

        plugin.debug("§aNBT aplicado à entidade: " + bossName, true);

        double health = bossConfig.getDouble("health", 100.0);
        boss.setMaxHealth(health);
        boss.setHealth(health);

        boss.setCustomName(ChatColor.translateAlternateColorCodes('&',
                bossConfig.getString("name", bossName)
                        .replace("{HEALTH}", String.valueOf(boss.getHealth()))));
        boss.setCustomNameVisible(true);

        if (!bossConfig.getBoolean("options.ai", true)) {
            nbtEntity.setBoolean("NoAI", true);
        }

        plugin.debug("§aBoss " + bossName + " colocado por " + player.getName(), true);

        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().setItemInHand(null);
        }

        String actionBarMessage = bossConfig.getString("action-bar.on-interact");

        if (actionBarMessage != null && !actionBarMessage.isEmpty()) {
            String formattedMessage = ChatColor.translateAlternateColorCodes('&', actionBarMessage)
                    .replace("{HEALTH}", String.valueOf(boss.getHealth()))
                    .replace("{PROGRESS-BAR}", ProgressBar.getBar(100, "|", "-", "§a", "§c", 10));

            ActionBar.sendActionBarMessage(player, formattedMessage);
        }
    }

    @EventHandler
    public void onBossInteract(PlayerInteractAtEntityEvent event) {
        if (!(event.getRightClicked() instanceof LivingEntity)) return;
        LivingEntity entity = (LivingEntity) event.getRightClicked();

        NBTEntity nbtEntity = new NBTEntity(entity);
        String bossName = nbtEntity.getString("bossName");

        if ((bossName == null || bossName.isEmpty()) && entity.hasMetadata("bossName")) {
            bossName = entity.getMetadata("bossName").get(0).asString();
        }

        if (bossName == null || bossName.isEmpty()) return;

        Player player = event.getPlayer();
        plugin.debug("§aInteração com o boss: " + bossName, true);

        double health = entity.getHealth();
        double maxHealth = entity.getMaxHealth();

        String actionBarMessage = plugin.getBossConfig().getBossConfig()
                .getString("bosses." + bossName + ".action-bar.on-interact", "");

        if (actionBarMessage != null && !actionBarMessage.isEmpty()) {
            String formattedMessage = ChatColor.translateAlternateColorCodes('&', actionBarMessage)
                    .replace("{HEALTH}", String.valueOf((int) health))
                    .replace("{PROGRESS-BAR}", ProgressBar.getBar((health / maxHealth) * 100, "|", "-", "§a", "§c", 10));

            ActionBar.sendActionBarMessage(player, formattedMessage);
        }
    }
}