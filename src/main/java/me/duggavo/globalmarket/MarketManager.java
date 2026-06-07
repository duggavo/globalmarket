package me.duggavo.globalmarket;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MarketManager {

    private final GlobalMarketPlugin plugin;
    private final File marketsFile;
    private final FileConfiguration marketsConfig;
    private final Map<Material, Market> markets = new HashMap<>();

    public MarketManager(GlobalMarketPlugin plugin) {
        this.plugin = plugin;
        this.marketsFile = new File(plugin.getDataFolder(), "markets.yml");
        if (!marketsFile.exists()) {
            plugin.saveResource("markets.yml", false);
        }
        this.marketsConfig = YamlConfiguration.loadConfiguration(marketsFile);
    }

    public Market getMarket(Material material) {
        return markets.get(material);
    }

    public void createMarket(Material material, double currencyPool, double itemPool) {
        Market market = new Market(material, currencyPool, itemPool);
        markets.put(material, market);
        saveMarkets();
    }

    public void loadMarkets() {
        markets.clear();
        if (!marketsConfig.isConfigurationSection("markets")) return;

        for (String key : marketsConfig.getConfigurationSection("markets").getKeys(false)) {
            try {
                Material material = Material.valueOf(key.toUpperCase());
                double currencyPool = marketsConfig.getDouble("markets." + key + ".currency-pool");
                double itemPool = marketsConfig.getDouble("markets." + key + ".item-pool");
                markets.put(material, new Market(material, currencyPool, itemPool));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid material in markets.yml: " + key);
            }
        }
    }

    public void saveMarkets() {
        for (Map.Entry<Material, Market> entry : markets.entrySet()) {
            String path = "markets." + entry.getKey().name();
            marketsConfig.set(path + ".currency-pool", entry.getValue().getCurrencyPool());
            marketsConfig.set(path + ".item-pool", entry.getValue().getItemPool());
        }
        try {
            marketsConfig.save(marketsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save markets.yml!");
        }
    }
}