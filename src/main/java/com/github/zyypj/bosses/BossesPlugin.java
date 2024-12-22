package com.github.zyypj.bosses;

import com.github.zyypj.bosses.commands.BossesCommand;
import com.github.zyypj.bosses.commands.BossesGiveCommand;
import com.github.zyypj.bosses.config.BossConfig;
import com.github.zyypj.bosses.config.MessagesConfig;
import com.github.zyypj.bosses.config.MatadoraConfig;
import com.github.zyypj.bosses.config.RecompensasConfig;
import com.github.zyypj.bosses.database.DatabaseManager;
import com.github.zyypj.bosses.listeners.BossDamageListener;
import com.github.zyypj.bosses.listeners.BossInteractListener;
import com.google.common.base.Stopwatch;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class BossesPlugin extends JavaPlugin {

    private MessagesConfig messagesConfig;
    private RecompensasConfig recompensasConfig;
    private BossConfig bossConfig;
    private MatadoraConfig matadoraConfig;

    private DatabaseManager databaseManager;

    @Override
    public void onEnable() {

        Stopwatch stopwatch = Stopwatch.createStarted();
        debug(" ", false);
        debug("&eIniciando plugin de bosses...", false);

        loadConfigs();
        setupDatabase();

        registerCommands();
        registerListener();

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

        messagesConfig = new MessagesConfig(this);

        recompensasConfig = new RecompensasConfig(this);
        recompensasConfig.load();

        bossConfig = new BossConfig(this);
        bossConfig.load();

        matadoraConfig = new MatadoraConfig(this);
        matadoraConfig.load();

        debug("&aConfigurações carregadas em " + stopwatch.stop() + "!", true);
        debug(" ", false);

    }

    private void setupDatabase() {

        Stopwatch stopwatch = Stopwatch.createStarted();
        debug(" ", false);
        debug("&eConectando database...", true);

        databaseManager = new DatabaseManager(this);

        debug("&aDatabase conectada em " + stopwatch.stop() + "!", true);
        debug(" ", false);

    }

    private void registerCommands() {

        Stopwatch stopwatch = Stopwatch.createStarted();
        debug(" ", false);
        debug("&eRegistrando comandos...", true);

        BossesGiveCommand bossesGiveCommand = new BossesGiveCommand(this);
        getCommand("bossesGive").setExecutor(bossesGiveCommand);
        getCommand("bossesGive").setTabCompleter(bossesGiveCommand);

        getCommand("bosses").setExecutor(new BossesCommand(this));

        debug("&aComandos registrados em " + stopwatch.stop() + "!", true);
        debug(" ", false);

    }

    private void registerListener() {

        Stopwatch stopwatch = Stopwatch.createStarted();
        debug(" ", false);
        debug("&eRegistrando eventos...", true);

        registerListeners(
                new BossInteractListener(this),
                new BossDamageListener(this)
        );

        debug("&aEventos registrados em " + stopwatch.stop() + "!", true);
        debug(" ", false);

    }

    private void registerListeners(Listener... listeners) {
        for (Listener listener : listeners) {
            getServer().getPluginManager().registerEvents(listener, this);
        }
    }
}
