package com.github.zyypj.bosses.config;

import com.github.zyypj.bosses.BossesPlugin;
import com.github.zyypj.bosses.utils.ItemBuilder;
import com.github.zyypj.bosses.utils.Text;
import de.tr7zw.nbtapi.NBTItem;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Getter
public class BossConfig {

    private final BossesPlugin plugin;
    private File bossFile;
    private FileConfiguration bossConfig;

    public void load() {
        bossFile = new File(plugin.getDataFolder(), "bosses.yml");

        if (!bossFile.exists()) {
            plugin.saveResource("bosses.yml", false);
        }

        bossConfig = YamlConfiguration.loadConfiguration(bossFile);
    }

    public ItemStack getBossEgg(String bossName) {

        ConfigurationSection bossSection = bossConfig.getConfigurationSection("bosses." + bossName);

        if (bossSection == null) {
            plugin.debug("§cNão foi encontrado a seção bosses." + bossName, false);
            return null;
        }

        String materialData = bossSection.getString("egg.material", "MONSTER_EGG:0");
        String[] materialParts = materialData.split(":");
        Material material = Material.getMaterial(materialParts[0]);
        int data = materialParts.length > 1 ? Integer.parseInt(materialParts[1]) : 0;

        String name = Text.colorTranslate(bossSection.getString("egg.name", "Boss Egg"));
        List<String> lore = Text.colorTranslate(bossSection.getStringList("egg.lore"));
        boolean glow = bossSection.getBoolean("egg.glow", false);

        ItemStack bossEgg = new ItemBuilder(material)
                .durability((short) data)
                .displayName(name)
                .lore(lore)
                .glow(glow)
                .build();

        NBTItem nbtItem = new NBTItem(bossEgg);
        nbtItem.setString("bossName", bossName);
        return nbtItem.getItem();
    }

    public List<String> getBosses() {
        ConfigurationSection section = bossConfig.getConfigurationSection("bosses");
        if (section == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(section.getKeys(false));
    }
}
