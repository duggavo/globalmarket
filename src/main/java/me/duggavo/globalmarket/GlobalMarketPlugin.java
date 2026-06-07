package me.duggavo.globalmarket;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class GlobalMarketPlugin extends JavaPlugin {

    private Economy econ;
    private MarketManager marketManager;

    @Override
    public void onEnable() {
        saveDefaultConfig(); // Generates config.yml if it doesn't exist
        
        if (!setupEconomy()) {
            getLogger().severe("Disabled due to no Vault dependency found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        marketManager = new MarketManager(this);
        marketManager.loadMarkets();

        getServer().getPluginManager().registerEvents(new MobKillListener(this), this);
        
        getCommand("market").setExecutor(new MarketCommand(this));
        getCommand("market").setTabCompleter(new MarketTabCompleter(this));

        getLogger().info("GlobalMarket has been enabled!");
    }

    @Override
    public void onDisable() {
        if (marketManager != null) {
            marketManager.saveMarkets();
        }
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return false;
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        econ = rsp.getProvider();
        return econ != null;
    }

    public Economy getEconomy() { return econ; }
    public MarketManager getMarketManager() { return marketManager; }
    
    // New method to fetch the fee from config
    public double getTradingFee() {
        return getConfig().getDouble("trading-fee", 0.05);
    }

    public double getMobKillRewardChance() {
        return getConfig().getDouble("mob-kill-reward-chance", 0.1);
    }

    public double getMobKillReward() {
        return getConfig().getDouble("mob-kill-reward", 1);
    }
}