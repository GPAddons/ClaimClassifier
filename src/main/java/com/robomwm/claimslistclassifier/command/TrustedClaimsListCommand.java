package com.robomwm.claimslistclassifier.command;

import com.robomwm.claimslistclassifier.ClaimslistClassifier;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.DataStore;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.Messages;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created on 9/28/2018.
 *
 * @author RoboMWM
 */
public class TrustedClaimsListCommand extends CommandBase
{

    public TrustedClaimsListCommand(ClaimslistClassifier plugin, GriefPrevention griefPrevention, DataStore dataStore)
    {
        super(plugin, griefPrevention, dataStore);
    }

    @Override
    public void registerCommand()
    {
        plugin.getCommand("trustedclaimslist").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (!(sender instanceof Player))
            return false;

        Player player = (Player)sender;

        Set<Claim> claims = new HashSet<>();

        //Iterate through all claims, see if player has any trust level or /permissiontrust
        for (Claim claim : dataStore.getClaims())
        {
            //Skip claims they own
            if (player.getUniqueId().equals(claim.ownerID))
                continue;

            if (claim.allowAccess(player) == null || claim.allowGrantPermission(player) == null)
                claims.add(claim);
        }

        if (claims.isEmpty())
        {
            player.sendMessage(ChatColor.RED + "You are not trusted to any claims.");
            return true;
        }

        if(claims.size() > 0) //redundant check
        {
            GriefPrevention.sendMessage(player, ChatColor.YELLOW, Messages.ClaimsListHeader);

            //Insertion sort for now. If needed will utilize a more efficient algorithm
            Map<World, List<Claim>> sortedClaims = new LinkedHashMap<>();
            for (World world : plugin.getServer().getWorlds())
            {
                sortedClaims.put(world, new ArrayList<>());
            }

            for (Claim claim : claims)
            {
                World world = claim.getLesserBoundaryCorner().getWorld();
                int index = 0;
                for (Claim otherClaim : sortedClaims.get(world))
                {
                    if (isLesser(claim.getLesserBoundaryCorner(), otherClaim.getLesserBoundaryCorner()))
                    {
                        sortedClaims.get(world).add(index, claim);
                        break;
                    }
                    index++;
                }
                if (index >= sortedClaims.get(world).size())
                    sortedClaims.get(world).add(index, claim);
            }
            //end sorting

            String name;

            for (World world : sortedClaims.keySet())
                for(Claim claim : sortedClaims.get(world))
                {
                    if (plugin.getClaimNames() != null && plugin.getClaimNames().get(claim.getID().toString()) != null)
                        name = plugin.getClaimNames().getString(claim.getID().toString()) + ": ";
                    else
                        name = "";
                    GriefPrevention.sendMessage(player, ChatColor.YELLOW, getfriendlyLocationString(claim.getLesserBoundaryCorner(), name) + dataStore.getMessage(Messages.ContinueBlockMath, String.valueOf(claim.getArea())));
                }
        }

        return true;
    }

    private boolean isLesser(Location location, Location otherLocation)
    {
        if (location.getBlockX() < otherLocation.getBlockX())
            return true;
        else if (location.getBlockX() > otherLocation.getBlockX())
            return false;
        else
        {
            if (location.getBlockZ() < otherLocation.getBlockZ())
                return true;
            else if (location.getBlockZ() > otherLocation.getBlockZ())
                return false;
        }
        return false;
    }

    public String getfriendlyLocationString(Location location, String name) {
        if (!name.isEmpty())
            return location.getWorld().getName() + ": " + ChatColor.AQUA + name + ChatColor.YELLOW + ": x" + location.getBlockX() + ", z" + location.getBlockZ();
        return location.getWorld().getName() + ": x" + location.getBlockX() + ", z" + location.getBlockZ();
    }
}
