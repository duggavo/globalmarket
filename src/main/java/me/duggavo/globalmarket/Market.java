package me.duggavo.globalmarket;

import org.bukkit.Material;

public class Market {
    private final Material material;
    private double currencyPool;
    private double itemPool;

    public Market(Material material, double currencyPool, double itemPool) {
        this.material = material;
        this.currencyPool = currencyPool;
        this.itemPool = itemPool;
    }

    public Material getMaterial() { return material; }
    public double getCurrencyPool() { return currencyPool; }
    public double getItemPool() { return itemPool; }

    public double getCurrentPrice() {
        if (itemPool <= 0) return Double.MAX_VALUE;
        return currencyPool / itemPool;
    }

    // Calculates cost to buy X items using the Constant Product Formula (x * y = k) before fee
    public double calculateBuyCost(int amount) {
        if (amount <= 0) return 0;
        if (itemPool <= amount) return Double.MAX_VALUE; // Not enough liquidity
        
        double k = currencyPool * itemPool;
        double newItemPool = itemPool - amount;
        double newCurrencyPool = k / newItemPool;
        
        // Round up to prevent exploit rounding errors
        return Math.ceil((newCurrencyPool - currencyPool) * 100.0) / 100.0;
    }

    // Calculates reward for selling X items before fee
    public double calculateSellReward(int amount) {
        if (amount <= 0) return 0;
        
        double k = currencyPool * itemPool;
        double newItemPool = itemPool + amount;
        double newCurrencyPool = k / newItemPool;
        
        // Round down to prevent exploit rounding errors
        return Math.floor((currencyPool - newCurrencyPool) * 100.0) / 100.0;
    }

    public void executeBuy(int amount, double totalCostPaid) {
        currencyPool += totalCostPaid; 
        itemPool -= amount;
    }

    public void executeSell(int amount, double totalRewardGiven) {
        currencyPool -= totalRewardGiven;
        itemPool += amount;
    }
}
    
    
