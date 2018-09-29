package com.robomwm.claimslistclassifier.listener;

import com.robomwm.claimslistclassifier.ClaimslistClassifier;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.DataStore;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created on 9/28/2018.
 *
 * @author RoboMWM
 */
public class ConfirmAbandonClaimListener extends ListenerBase
{
    private Map<Player, Claim> pendingClaimsToAbandon = new HashMap<>();
    private YamlConfiguration abandonClaimCommands;

    public ConfirmAbandonClaimListener(ClaimslistClassifier plugin, GriefPrevention griefPrevention, DataStore dataStore)
    {
        super(plugin, griefPrevention, dataStore);
    }

    @Override
    public void registerListeners()
    {
        super.registerListeners();
        File abandonClaimCommandsFile = new File(plugin.getDataFolder() + File.separator + "abandonClaimCommands.yml");
        abandonClaimCommands = YamlConfiguration.loadConfiguration(abandonClaimCommandsFile);

        List<String> commandList = new ArrayList<>();
        commandList.add("/abandonclaim");
        commandList.add("/unclaim");
        commandList.add("/declaim");
        commandList.add("/removeclaim");
        commandList.add("/disclaim");
        abandonClaimCommands.addDefault("abandonclaim_commands", commandList);
        abandonClaimCommands.addDefault("abandonclaim_prompt", "Are you sure you wish to abandon this claim? Type /abandonclaim again to confirm");
        abandonClaimCommands.options().copyDefaults(true);
        try
        {
            abandonClaimCommands.save(abandonClaimCommandsFile);
        }
        catch (Throwable rock)
        {
            rock.printStackTrace();
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onAbandoningClaim(PlayerCommandPreprocessEvent event)
    {
        Player player = event.getPlayer();
        String[] args = event.getMessage().toLowerCase().split(" ");
        if (!abandonClaimCommands.getStringList("abandonclaim_commands").contains(args[0]))
            return;
        Claim claim = dataStore.getClaimAt(event.getPlayer().getLocation(), true, null);
        if (claim == null || !event.getPlayer().getUniqueId().equals(claim.ownerID))
            return;

        if (pendingClaimsToAbandon.containsKey(player) && pendingClaimsToAbandon.get(player).equals(claim))
        {
            pendingClaimsToAbandon.remove(player);
            return;
        }

        player.sendMessage(abandonClaimCommands.getString("abandonclaim_prompt"));
        pendingClaimsToAbandon.put(player, claim);
        event.setCancelled(true);
    }
}
