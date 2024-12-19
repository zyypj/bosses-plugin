package com.github.zyypj.bosses;

import com.github.zyypj.bosses.commands.BossesGiveCommand;
import com.github.zyypj.bosses.config.BossConfigManager;
import com.github.zyypj.bosses.config.ConfigManager;
import com.github.zyypj.bosses.config.MatadoraConfigManager;
import com.github.zyypj.bosses.listeners.BossPlaceListener;
import com.google.common.base.Stopwatch;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class BossesPlugin extends JavaPlugin {

    private ConfigManager configManager;
    private BossConfigManager bossConfigManager;
    private MatadoraConfigManager matadoraConfigManager;

    @Override
    public void onEnable() {

        Stopwatch stopwatch = Stopwatch.createStarted();
        debug(" ", false);
        debug("&eIniciando plugin de bosses...", false);

        loadConfigs();

        registerCommands();
        registerListeners();

        debug(" ", false);
        debug("&2&lPlugin de Bosses&2 iniciado em " + stopwatch.stop() + "!", false);
        debug(" ", false);
    }

    @Override
    public void onDisable() {

        debug("&aPlugin desligado com sucesso!", true);
    }

    public void debug(String message, boolean debug) {

        message = message.replace("&", "§");

        if (debug) {

            if (getConfig().getBoolean("debug", true)) {
                Bukkit.getConsoleSender().sendMessage("§8[BOSSES-DEBUG] §f" + message);
            }
            return;
        }

        Bukkit.getConsoleSender().sendMessage("§f" + message);
    }

    private void loadConfigs() {

        Stopwatch stopwatch = Stopwatch.createStarted();
        debug(" ", false);
        debug("&eCarregando configurações...", true);

        saveDefaultConfig();

        configManager = new ConfigManager(this);

        bossConfigManager = new BossConfigManager(this);
        bossConfigManager.load();

        matadoraConfigManager = new MatadoraConfigManager(this);
        matadoraConfigManager.load();

        debug("&aConfigurações carregadas em " + stopwatch.stop() + "!", true);
        debug(" ", false);

    }

    private void registerCommands() {

        Stopwatch stopwatch = Stopwatch.createStarted();
        debug(" ", false);
        debug("&eRegistrando comandos...", true);

        BossesGiveCommand bossesGiveCommand = new BossesGiveCommand(this);
        getCommand("bossesGive").setExecutor(bossesGiveCommand);
        getCommand("bossesGive").setTabCompleter(bossesGiveCommand);

        debug("&aComandos registrados em " + stopwatch.stop() + "!", true);
        debug(" ", false);

    }

    private void registerListeners() {

        Stopwatch stopwatch = Stopwatch.createStarted();
        debug(" ", false);
        debug("&eRegistrando eventos...", true);

        getServer().getPluginManager().registerEvents(new BossPlaceListener(this), this);

        debug("&aEventos registrados em " + stopwatch.stop() + "!", true);
        debug(" ", false);

    }
}
