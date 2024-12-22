package com.github.zyypj.bosses.api.events;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.inventory.ItemStack;

@Getter
@Setter
public class BossDeathEvent extends EntityEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final Player killer;
    private final LivingEntity boss;
    private final String bossName;
    private final ItemStack item;
    private final boolean isMatadora;
    private final String matadoraName;

    private boolean cancelled;

    public BossDeathEvent(Player killer, LivingEntity boss, String bossName, ItemStack item, boolean isMatadora, String matadoraName) {
        super(boss);
        this.killer = killer;
        this.boss = boss;
        this.bossName = bossName;
        this.item = item;
        this.isMatadora = isMatadora;
        this.matadoraName = matadoraName;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
