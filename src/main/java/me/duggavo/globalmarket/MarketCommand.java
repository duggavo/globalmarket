package me.duggavo.globalmarket;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class MarketCommand implements CommandExecutor {

    private final GlobalMarketPlugin plugin;

    public MarketCommand(GlobalMarketPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "info":
                handleInfo(sender, args);
                break;
            case "buy":
                handleBuy(sender, args);
                break;
            case "sell":
                handleSell(sender, args);
                break;
            case "create":
                handleCreate(sender, args);
                break;
            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    private void handleInfo(CommandSender sender, String[] args) {
        if (args.length < 2) { sender.sendMessage(ChatColor.RED + "Usage: /market info <item>"); return; }
        Material material = Material.matchMaterial(args[1]);
        if (material == null) { sender.sendMessage(ChatColor.RED + "Invalid item."); return; }

        Market market = plugin.getMarketManager().getMarket(material);
        if (market == null) { sender.sendMessage(ChatColor.RED + "No market exists for " + material.name()); return; }
        
        sender.sendMessage(ChatColor.GOLD + "=== Market info: " + material.name() + " ===");
        Economy eco = plugin.getEconomy();
        sender.sendMessage(ChatColor.YELLOW + "Price per item: " + ChatColor.WHITE + eco.format(market.getCurrentPrice()));
        sender.sendMessage(ChatColor.YELLOW + "Item liquidity: " + ChatColor.WHITE + (long)market.getItemPool());
        sender.sendMessage(ChatColor.YELLOW + "Currency liquidity: " + ChatColor.WHITE + eco.format(market.getCurrencyPool()));
    }

    private void handleBuy(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return;
        }
        Player player = (Player) sender;
        if (args.length < 3) { player.sendMessage(ChatColor.RED + "Usage: /market buy <item> <amount>"); return; }
        Material material = Material.matchMaterial(args[1]);
        if (material == null) { player.sendMessage(ChatColor.RED + "Invalid item."); return; }

        int amount;
        try { amount = Integer.parseInt(args[2]); } catch (NumberFormatException e) { player.sendMessage(ChatColor.RED + "Invalid amount."); return; }
        if (amount <= 0) { player.sendMessage(ChatColor.RED + "Amount must be positive."); return; }

        Market market = plugin.getMarketManager().getMarket(material);
        if (market == null) { player.sendMessage(ChatColor.RED + "No market exists for " + material.name()); return; }

        double feePercent = plugin.getTradingFee();
        double baseCost = market.calculateBuyCost(amount);
        if (baseCost == Double.MAX_VALUE) { player.sendMessage(ChatColor.RED + "Not enough liquidity in the market to fulfill this order."); return; }

        // Calculate Fee and Total Cost
        double fee = Math.ceil(baseCost * feePercent * 100.0) / 100.0; // Round up fee
        double totalCost = baseCost + fee;

        Economy econ = plugin.getEconomy();
        if (econ.getBalance(player) < totalCost) { 
            player.sendMessage(ChatColor.RED + "You need " + econ.format(totalCost) + " but only have " + econ.format(econ.getBalance(player))); 
            return; 
        }

        // Execute Transaction
        EconomyResponse resp = econ.withdrawPlayer(player, totalCost);
        if (resp.transactionSuccess()) {
            market.executeBuy(amount, totalCost); // totalCost (base + fee) goes into the pool
            player.getInventory().addItem(new ItemStack(material, amount));
            plugin.getMarketManager().saveMarkets();
            player.sendMessage(ChatColor.GREEN + "Bought " + amount + " " + material.name() + " for " + econ.format(totalCost) + ChatColor.GRAY + " (Includes " + econ.format(fee) + " fee)");
        } else {
            player.sendMessage(ChatColor.RED + "Transaction failed: " + resp.errorMessage);
        }
    }

    private void handleSell(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return;
        }
        Player player = (Player) sender;
        if (args.length < 3) { player.sendMessage(ChatColor.RED + "Usage: /market sell <item> <amount>"); return; }
        Material material = Material.matchMaterial(args[1]);
        if (material == null) { player.sendMessage(ChatColor.RED + "Invalid item."); return; }

        int amount;
        if (args[2].equalsIgnoreCase("all")) {
            amount = itemCount(player, material);
        } else {
            try { amount = Integer.parseInt(args[2]); } catch (NumberFormatException e) { player.sendMessage(ChatColor.RED + "Invalid amount."); return; }
        }
        if (amount <= 0) { player.sendMessage(ChatColor.RED + "Amount must be positive."); return; }

        Market market = plugin.getMarketManager().getMarket(material);
        if (market == null) { player.sendMessage(ChatColor.RED + "No market exists for " + material.name()); return; }

        if (!hasItems(player, material, amount)) { player.sendMessage(ChatColor.RED + "You don't have " + amount + " " + material.name()); return; }

        double feePercent = plugin.getTradingFee();
        double baseReward = market.calculateSellReward(amount);
        if (baseReward <= 0) { player.sendMessage(ChatColor.RED + "Market liquidity too low to pay you for this."); return; }

        // Calculate Fee and Total Reward
        double fee = Math.floor(baseReward * feePercent * 100.0) / 100.0; // Round down fee
        double totalReward = baseReward - fee;

        if (totalReward <= 0) { player.sendMessage(ChatColor.RED + "Amount too small to sell after fees."); return; }

        // Execute Transaction
        removeItems(player, material, amount);
        Economy econ = plugin.getEconomy();
        EconomyResponse resp = econ.depositPlayer(player, totalReward);
        if (resp.transactionSuccess()) {
            market.executeSell(amount, totalReward); // totalReward (base - fee) leaves the pool, fee stays
            plugin.getMarketManager().saveMarkets();
            player.sendMessage(ChatColor.GREEN + "Sold " + amount + " " + material.name() + " for " + econ.format(totalReward) + ChatColor.GRAY + " (Fee: " + econ.format(fee) + ")");
        } else {
            // Refund items if deposit fails
            player.getInventory().addItem(new ItemStack(material, amount));
            player.sendMessage(ChatColor.RED + "Transaction failed: " + resp.errorMessage);
        }
    }

    private void handleCreate(CommandSender sender, String[] args) {
        if (!sender.hasPermission("globalmarket.admin")) { sender.sendMessage(ChatColor.RED + "No permission."); return; }
        if (args.length < 4) { sender.sendMessage(ChatColor.RED + "Usage: /market create <item> <currency_pool> <item_pool>"); return; }
        
        Material material = Material.matchMaterial(args[1]);
        if (material == null) { sender.sendMessage(ChatColor.RED + "Invalid item."); return; }

        double currencyPool, itemPool;
        try {
            currencyPool = Double.parseDouble(args[2]);
            itemPool = Double.parseDouble(args[3]);
        } catch (NumberFormatException e) { sender.sendMessage(ChatColor.RED + "Invalid pool amounts."); return; }

        if (currencyPool <= 0 || itemPool <= 0) { sender.sendMessage(ChatColor.RED + "Pools must be greater than 0."); return; }

        Economy econ = plugin.getEconomy();

        plugin.getMarketManager().createMarket(material, currencyPool, itemPool);
        sender.sendMessage(ChatColor.GREEN + "Created market for " + material.name() + " with " + econ.format(currencyPool) + " and " + itemPool + " items.");
    }

    private boolean hasItems(Player player, Material material, int amount) {
        return itemCount(player, material) >= amount;
    }
    private int itemCount(Player player, Material material) {
        int total = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) {
                total += item.getAmount();
            }
        }
        return total;
    }

    private void removeItems(Player player, Material material, int amount) {
        int remaining = amount;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) {
                int itemAmount = item.getAmount();
                if (itemAmount <= remaining) {
                    remaining -= itemAmount;
                    item.setAmount(0);
                } else {
                    item.setAmount(itemAmount - remaining);
                    remaining = 0;
                }
                if (remaining <= 0) break;
            }
        }
    }

    private void sendHelp(CommandSender player) {
        player.sendMessage(ChatColor.GOLD + "=== Global Market ===");
        player.sendMessage(ChatColor.YELLOW + "/market info <item>" + ChatColor.GRAY + " - View market info");
        player.sendMessage(ChatColor.YELLOW + "/market buy <item> <amount>" + ChatColor.GRAY + " - Buy items");
        player.sendMessage(ChatColor.YELLOW + "/market sell <item> <amount>" + ChatColor.GRAY + " - Sell items");
        if (player.hasPermission("globalmarket.admin")) {
            player.sendMessage(ChatColor.YELLOW + "/market create <item> <currency> <items>" + ChatColor.GRAY + " - Create market");
        }
    }
}