package com.github.zyypj.bosses.config;

import com.github.zyypj.bosses.BossesPlugin;
import com.github.zyypj.bosses.utils.ItemBuilder;
import de.tr7zw.nbtapi.NBTItem;
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
public class MatadoraConfigManager {

    private final BossesPlugin plugin;
    private File matadoraFile;
    private FileConfiguration matadoraConfig;

    public void load() {
        matadoraFile = new File(plugin.getDataFolder(), "matadoras.yml");

        if (!matadoraFile.exists()) {
            plugin.saveResource("matadoras.yml", false);
        }

        matadoraConfig = YamlConfiguration.loadConfiguration(matadoraFile);
    }

    public ItemStack getMatadoraItem(String matadoraName) {

        ConfigurationSection matadoraSection = matadoraConfig.getConfigurationSection("matadoras." + matadoraName);

        if (matadoraSection == null) {

            plugin.debug("§cNão foi encontrado a seção bosses." + matadoraName, false);
            return null;
        }

        String materialData = matadoraSection.getString("item.material", "DIAMOND_SWORD:0");
        String[] materialParts = materialData.split(":");
        Material material = Material.getMaterial(materialParts[0]);
        int data = materialParts.length > 1 ? Integer.parseInt(materialParts[1]) : 0;
        
        String name = matadoraSection.getString("item.name", "Matadora de Bosses");
        List<String> lore = matadoraSection.getStringList("item.lore");
        boolean glow = matadoraSection.getBoolean("item.glow", false);
        int amount = matadoraSection.getInt("item.amount", 1);

        ItemStack matadora = new ItemBuilder(material, amount)
                .durability((short) data)
                .displayName(name)
                .lore(lore)
                .glow(glow)
                .build();

        NBTItem nbtItem = new NBTItem(matadora);
        nbtItem.setString("itemType", "matadora");
        nbtItem.setString("matadoraName", matadoraName);
        return nbtItem.getItem();
    }

    public List<String> getMatadoras() {
        ConfigurationSection section = matadoraConfig.getConfigurationSection("matadoras");
        if (section == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(section.getKeys(false));
    }
}
