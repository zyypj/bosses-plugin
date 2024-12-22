package com.github.zyypj.bosses.api.events;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;

@Getter
@Setter
public class BossPlaceEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final LivingEntity boss;
    private final String bossName;
    private final ItemStack bossEgg;

    private boolean cancelled;

    public BossPlaceEvent(Player player, LivingEntity boss, String bossName, ItemStack bossEgg) {
        super(player);

        this.boss = boss;
        this.bossName = bossName;
        this.bossEgg = bossEgg;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
