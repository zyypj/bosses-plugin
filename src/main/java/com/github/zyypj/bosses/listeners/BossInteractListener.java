package com.github.zyypj.bosses.listeners;

import com.github.zyypj.bosses.BossesPlugin;
import com.github.zyypj.bosses.api.events.BossPlaceEvent;
import com.github.zyypj.bosses.utils.ActionBar;
import com.intellectualcrafters.plot.api.PlotAPI;
import com.intellectualcrafters.plot.object.Plot;
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
    private final PlotAPI plotAPI = new PlotAPI();

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

        BossPlaceEvent bossPlaceEvent = new BossPlaceEvent(player, boss, bossName, item);
        plugin.getServer().getPluginManager().callEvent(bossPlaceEvent);

        if (bossPlaceEvent.isCancelled()) {
            boss.remove();
            return;
        }

        plugin.getDatabaseManager().addBossPlaced(player);

        Plot plot = plotAPI.getPlot(player.getLocation());
        if (plot == null) {
            player.sendMessage(plugin.getMessagesConfig().getMessage("cant-place"));
            bossPlaceEvent.setCancelled(true);
            boss.remove();
            return;
        }

        if (!plot.isOwner(player.getUniqueId()) && !plot.getTrusted().contains(player.getUniqueId())) {
            player.sendMessage(plugin.getMessagesConfig().getMessage("cant-place"));
            bossPlaceEvent.setCancelled(true);
            boss.remove();
            return;
        }

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
                    .replace("{PROGRESS-BAR}", "§a" + healthBar + "§c" + emptyBar);

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

        if (!actionBarMessage.isEmpty()) {
            int totalBars = 30;
            int healthBars = (int) Math.round((health / maxHealth) * totalBars);
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
                    .replace("{PROGRESS-BAR}", "§a" + healthBar + "§c" + emptyBar);

            ActionBar.sendActionBarMessage(player, formattedMessage);
        }
    }
}