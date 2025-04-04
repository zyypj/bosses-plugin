package com.github.zyypj.bosses.config;

import com.github.zyypj.bosses.BossesPlugin;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class MessagesConfig {

    private final BossesPlugin plugin;
    private FileConfiguration config;

    public MessagesConfig(BossesPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    public String getMessage(String path) {

        path = "messages." + path;

        if (config.isString(path)) {
            return ChatColor.translateAlternateColorCodes('&', config.getString(path));
        }

        if (config.isList(path)) {
            List<String> messages = config.getStringList(path);
            String combinedMessage = String.join(" ", messages);
            return ChatColor.translateAlternateColorCodes('&', combinedMessage);
        }

        return "§cMensagem não encontrada: " + path;
    }

    public void reload() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }
}
