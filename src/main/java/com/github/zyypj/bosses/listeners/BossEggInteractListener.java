package com.github.zyypj.bosses.listeners;

import com.github.zyypj.bosses.BossesPlugin;
import de.tr7zw.nbtapi.NBTItem;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

@RequiredArgsConstructor
public class BossEggInteractListener implements Listener {

    private final BossesPlugin plugin;

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        ItemStack item = e.getItem();

        if (item == null || !item.hasItemMeta()) return;

        NBTItem nbtItem = new NBTItem(item);
        String bossName = nbtItem.getString("bossName");

        if (bossName == null || bossName.isEmpty()) return;

        if ((e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK)
                && player.isSneaking()) plugin.getBossRewardsMenu().openMenu(player, bossName);

    }
}
