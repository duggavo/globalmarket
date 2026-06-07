package me.duggavo.globalmarket;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class MobKillListener implements Listener {
	GlobalMarketPlugin plugin;

	MobKillListener(GlobalMarketPlugin p) {
		plugin = p;
	}

    @EventHandler
    public void onMobKill(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
		if (killer == null) {
			return;
		}
        if (Math.random() > plugin.getMobKillRewardChance()) {
            plugin.getEconomy().depositPlayer(killer, plugin.getMobKillReward());
        }
	}
}
