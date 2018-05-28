package me.robomwm.claimslistclassifier.listener;

import me.robomwm.claimslistclassifier.ClaimslistClassifier;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.DataStore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created on 5/27/2018.
 *
 * @author RoboMWM
 */
public class ConfirmAbandonClaimListener implements Listener
{
    private ClaimslistClassifier plugin;
    private DataStore dataStore;
    private Map<Player, Claim> pendingClaimsToAbandon = new HashMap<>();

    public ConfirmAbandonClaimListener(ClaimslistClassifier plugin, DataStore dataStore)
    {
        this.plugin = plugin;
        this.dataStore = dataStore;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(ignoreCancelled = true)
    private void onAbandoningClaim(PlayerCommandPreprocessEvent event)
    {
        Player player = event.getPlayer();
        String[] args = event.getMessage().toLowerCase().split(" ");
        if (!plugin.getConfig().getStringList("abandonclaim_commands").contains(args[0]))
            return;
        Claim claim = dataStore.getClaimAt(event.getPlayer().getLocation(), true, null);
        if (claim == null || !event.getPlayer().getUniqueId().equals(claim.ownerID))
            return;

        if (pendingClaimsToAbandon.containsKey(player) && pendingClaimsToAbandon.get(player).equals(claim))
        {
            pendingClaimsToAbandon.remove(player);
            return;
        }

        player.sendMessage(plugin.getConfig().getConfigurationSection("messages").getString("abandonclaim_prompt"));
        pendingClaimsToAbandon.put(player, claim);
        event.setCancelled(true);
    }
}
