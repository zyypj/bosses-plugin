package com.github.zyypj.bosses.config;

import com.github.zyypj.bosses.BossesPlugin;
import com.github.zyypj.bosses.utils.ItemBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;

@RequiredArgsConstructor
@Getter
public class RecompensasConfig {

    private final BossesPlugin plugin;
    private File recompensasFile;
    private FileConfiguration recompensasConfig;

    public void load() {
        recompensasFile = new File(plugin.getDataFolder(), "recompensas.yml");

        if (!recompensasFile.exists()) {
            plugin.saveResource("recompensas.yml", false);
        }

        recompensasConfig = YamlConfiguration.loadConfiguration(recompensasFile);
    }

    public ItemStack getRewardItem(String rewardKey) {
        ConfigurationSection rewardSection = recompensasConfig.getConfigurationSection("recompensas." + rewardKey);

        if (rewardSection == null) {
            plugin.debug("§cRecompensa não encontrada: " + rewardKey, true);
            return null;
        }

        String materialData = rewardSection.getString("item.material", "STONE:0");
        String[] materialParts = materialData.split(":");
        Material material = Material.getMaterial(materialParts[0]);
        int data = materialParts.length > 1 ? Integer.parseInt(materialParts[1]) : 0;

        String name = rewardSection.getString("item.name", "Recompensa");
        int amount = rewardSection.getInt("item.amount", 1);
        boolean glow = rewardSection.getBoolean("item.glow", false);

        ItemBuilder itemBuilder = new ItemBuilder(material, amount)
                .durability((short) data)
                .displayName(name)
                .glow(glow);

        rewardSection.getStringList("item.lore").forEach(itemBuilder::lore);

        return itemBuilder.build();

    }
}
