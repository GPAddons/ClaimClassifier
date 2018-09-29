package com.robomwm.claimslistclassifier.command;

import com.robomwm.claimslistclassifier.ClaimslistClassifier;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.DataStore;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created on 1/20/2018.
 *
 * @author RoboMWM
 */
public class NameClaimCommand implements CommandExecutor
{
    private ClaimslistClassifier instance;
    private DataStore dataStore;

    public NameClaimCommand(ClaimslistClassifier plugin, DataStore dataStore)
    {
        this.instance = plugin;
        this.dataStore = dataStore;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (instance.getClaimNames() == null)
        {
            sender.sendMessage(ChatColor.AQUA + "Claim naming is disabled. See server log at startup for more info.");
            return true;
        }

        if (!(sender instanceof Player))
            return false;
        if (args.length < 1)
            return false;

        Player player = (Player)sender;
        Claim claim = dataStore.getClaimAt(player.getLocation(), true, null);

        if (claim != null && claim.parent != null) //even more GP ugliness
            claim = claim.parent;

        if (claim == null || !claim.ownerID.equals(player.getUniqueId())) //oof more legacy GP ugliness
        {
            player.sendMessage(ChatColor.RED + "You must be inside a claim you own to use this command.");
            return true;
        }

        switch (args[0].toLowerCase())
        {
            case "off":
            case "clear":
            case "remove":
                instance.getClaimNames().set(claim.getID().toString(), null);
                player.sendMessage(ChatColor.GREEN + "Claim name removed.");
                break;
            default:
                instance.getClaimNames().set(claim.getID().toString(), StringUtils.join(args, " "));
                player.sendMessage(ChatColor.GREEN + "Claim named as: " + StringUtils.join(args, " "));
                break;
        }

        if (!instance.saveClaimNames())
            player.sendMessage(ChatColor.RED + "An error occurred while trying to save claim names. See server log for details.");

        return true;
    }
}
