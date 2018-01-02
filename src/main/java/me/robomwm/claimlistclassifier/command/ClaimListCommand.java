package me.robomwm.claimlistclassifier.command;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.DataStore;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.Messages;
import me.ryanhamshire.GriefPrevention.PlayerData;
import me.ryanhamshire.GriefPrevention.TextMode;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;
import java.util.Vector;

import static me.ryanhamshire.GriefPrevention.GriefPrevention.getfriendlyLocationString;

/**
 * Created on 1/1/2018.
 *
 * @author RoboMWM
 */
public class ClaimListCommand implements CommandExecutor
{
    JavaPlugin instance;
    DataStore dataStore;

    public ClaimListCommand(JavaPlugin plugin, DataStore dataStore)
    {
        this.instance = plugin;
        this.dataStore = dataStore;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        Player player = null;
        OfflinePlayer otherPlayer; //player whose claims will be listed
        SortType sortType;

        if (sender instanceof Player)
            player = (Player)sender;

        //Determine sort order, if specified
        if (args.length >= 2)
            sortType = determineSortOption(args[1]);
        else if (args.length >= 1)
            sortType = determineSortOption(args[0]);

        //if another player isn't specified, assume current player
        if(args.length < 1)
        {
            if(player != null)
                otherPlayer = player;
            else
                return false;
        }

        //otherwise if no permission to delve into another player's claims data
        else if(player != null && !player.hasPermission("griefprevention.claimslistother"))
        {
            GriefPrevention.sendMessage(player, ChatColor.RED, Messages.ClaimsListNoPermission);
            return true;
        }

        //otherwise try to find the specified player
        else
        {
            otherPlayer = resolvePlayerByName(args[0]);
            if(otherPlayer == null)
            {
                GriefPrevention.sendMessage(player, ChatColor.RED, Messages.PlayerNotFound2);
                return true;
            }
        }

        //load the target player's data
        PlayerData playerData = dataStore.getPlayerData(otherPlayer.getUniqueId());
        Vector<Claim> claims = playerData.getClaims();
        GriefPrevention.sendMessage(player, ChatColor.AQUA, Messages.StartBlockMath,
                String.valueOf(playerData.getAccruedClaimBlocks()),
                String.valueOf((playerData.getBonusClaimBlocks() + dataStore.getGroupBonusBlocks(otherPlayer.getUniqueId()))),
                String.valueOf((playerData.getAccruedClaimBlocks() + playerData.getBonusClaimBlocks() + dataStore.getGroupBonusBlocks(otherPlayer.getUniqueId()))));
        if(claims.size() > 0)
        {
            GriefPrevention.sendMessage(player, ChatColor.AQUA, Messages.ClaimsListHeader);
            StringBuilder claimBuilder = new StringBuilder();
            for (Claim claim : playerData.getClaims())
            {
                
            }
            for(int i = 0; i < playerData.getClaims().size(); i++)
            {
                Claim claim = playerData.getClaims().get(i);
                GriefPrevention.sendMessage(player, ChatColor.AQUA, getfriendlyLocationString(claim.getLesserBoundaryCorner()) + dataStore.getMessage(Messages.ContinueBlockMath, String.valueOf(claim.getArea())));
            }

            GriefPrevention.sendMessage(player, ChatColor.AQUA, Messages.EndBlockMath, String.valueOf(playerData.getRemainingClaimBlocks()));
        }

        return true;
    }

    private OfflinePlayer resolvePlayerByName(String name)
    {
        //try online players first
        Player targetPlayer = instance.getServer().getPlayerExact(name);
        if(targetPlayer != null) return targetPlayer;

        targetPlayer = instance.getServer().getPlayer(name);
        if(targetPlayer != null) return targetPlayer;

        return instance.getServer().getOfflinePlayer(name);
    }

    private SortType determineSortOption(String arg)
    {
        if (arg.equalsIgnoreCase("x"))
            return SortType.X;
        if (arg.equalsIgnoreCase("z"))
            return SortType.Z;
        return SortType.X; //sort via x coordinate by default
    }
}

enum SortType
{
    NONE,
    X,
    Z
}
