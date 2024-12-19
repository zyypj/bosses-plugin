package com.github.zyypj.bosses.model;

import lombok.Getter;
import org.bukkit.entity.Entity;

@Getter
public class Boss {

    private final String name;
    private final Entity mob;

    public Boss(String name, Entity mob) {
        this.name = name;
        this.mob = mob;
    }
}
