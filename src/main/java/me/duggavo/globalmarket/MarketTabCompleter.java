package me.duggavo.globalmarket;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MarketTabCompleter implements TabCompleter {
    private final GlobalMarketPlugin plugin;

    public MarketTabCompleter(GlobalMarketPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], Arrays.asList("buy", "sell", "info", "create"), completions);
        } else if (args.length == 2) {
            // 3. Corrected the stream logic
            List<String> materials = Arrays.stream(Material.values())
                    .filter(Material::isItem) // Keep only items
                    .filter(m -> plugin.getMarketManager().getMarket(m) != null) // keep only materials that have a market
                    .map(m -> m.name().toLowerCase()) // convert the material to a lowercase string
                    .collect(Collectors.toList());
                    
            StringUtil.copyPartialMatches(args[1], materials, completions);
        }

        return completions;
    }
}
