package com.github.zyypj.bosses.hooks;

import com.github.zyypj.bosses.BossesPlugin;
import lombok.RequiredArgsConstructor;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@RequiredArgsConstructor
public class BossesPlaceholder extends PlaceholderExpansion {

    private final BossesPlugin plugin;

    @Override
    public @NotNull String getIdentifier() {
        return "bosses";
    }

    @Override
    public @NotNull String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {

        if (player == null) {
            return "§cPlayer not found";
        }

        switch (params.toLowerCase()) {
            case "killed":
                return String.valueOf(plugin.getDatabaseManager().getPlayerStats(player.getPlayer()).getBossesKilled());
            case "placed":
                return String.valueOf(plugin.getDatabaseManager().getPlayerStats(player.getPlayer()).getBossesPlaced());

            default:
                return null;
        }

    }
}
