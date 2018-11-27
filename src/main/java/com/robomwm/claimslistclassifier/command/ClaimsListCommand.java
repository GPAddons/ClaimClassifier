package com.robomwm.claimslistclassifier.command;

import com.robomwm.claimslistclassifier.ClaimslistClassifier;
import com.robomwm.claimslistclassifier.LazyText;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.DataStore;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.Messages;
import me.ryanhamshire.GriefPrevention.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Created on 1/1/2018.
 *
 * @author RoboMWM
 */
public class ClaimsListCommand implements CommandExecutor, Listener
{
    private ClaimslistClassifier instance;
    private DataStore dataStore;

    public ClaimsListCommand(ClaimslistClassifier plugin, DataStore dataStore)
    {
        this.instance = plugin;
        this.dataStore = dataStore;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        Player player = null;
        OfflinePlayer otherPlayer; //player whose claims will be listed

        if (sender instanceof Player)
            player = (Player)sender;

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
        GriefPrevention.sendMessage(player, ChatColor.YELLOW, Messages.StartBlockMath,
                String.valueOf(playerData.getAccruedClaimBlocks()),
                String.valueOf((playerData.getBonusClaimBlocks() + dataStore.getGroupBonusBlocks(otherPlayer.getUniqueId()))),
                String.valueOf((playerData.getAccruedClaimBlocks() + playerData.getBonusClaimBlocks() + dataStore.getGroupBonusBlocks(otherPlayer.getUniqueId()))));
        if(claims.size() > 0)
        {
            GriefPrevention.sendMessage(player, ChatColor.YELLOW, Messages.ClaimsListHeader);

            //Insertion sort for now. If needed will utilize a more efficient algorithm
            Map<World, List<Claim>> sortedClaims = new LinkedHashMap<>();
            for (World world : instance.getServer().getWorlds())
            {
                sortedClaims.put(world, new ArrayList<>());
            }

            for (Claim claim : playerData.getClaims())
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
                    if (instance.getClaimNames() != null && instance.getClaimNames().get(claim.getID().toString()) != null)
                        name = instance.getClaimNames().getString(claim.getID().toString()) + ": ";
                    else
                        name = "";
                    if (instance.getConfig().getBoolean("ClickableClaimslist"))
                    {
                        Location middle = claim.getGreaterBoundaryCorner().clone();
                        middle.setX(getMiddle(claim.getLesserBoundaryCorner().getBlockX(), middle.getBlockX()));
                        middle.setZ(getMiddle(claim.getLesserBoundaryCorner().getBlockZ(), middle.getBlockZ()));
                        LazyText.Builder builder = new LazyText.Builder()
                                .add(getfriendlyLocationString(claim.getLesserBoundaryCorner(), name))
                                .color(net.md_5.bungee.api.ChatColor.YELLOW)
                                .suggest("/tp " + middle.getBlockX() +
                                        " " + middle.getBlockY() + " " +
                                        middle.getBlockZ(), true)
                                .add(dataStore.getMessage(Messages.ContinueBlockMath, String.valueOf(claim.getArea())))
                                .color(net.md_5.bungee.api.ChatColor.YELLOW);
                    }
                    else
                        GriefPrevention.sendMessage(player, ChatColor.YELLOW, getfriendlyLocationString(claim.getLesserBoundaryCorner(), name) + dataStore.getMessage(Messages.ContinueBlockMath, String.valueOf(claim.getArea())));
                }


            GriefPrevention.sendMessage(player, ChatColor.YELLOW, Messages.EndBlockMath, String.valueOf(playerData.getRemainingClaimBlocks()));
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

    //pretty sure there's a better way to math this
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

    //Other way is to hack into bukkit and remove the command from the commandmap
    public boolean interceptClaimsListCommand(CommandSender sender, String msg)
    {
        List<String> message = new LinkedList<>(Arrays.asList(msg.split(" ")));
        String command = message.get(0).toLowerCase().substring(1);

        switch (command)
        {
            case "claimslist":
            case "claimlist":
            case "listclaims":
                message.remove(0);
                String[] args = message.toArray(new String[message.size()]);
                this.onCommand(sender, null, command, args);
                return true;
        }
        return false;
    }

    @EventHandler(ignoreCancelled = true)
    private void onPlayerPreprocess(PlayerCommandPreprocessEvent event)
    {
        event.setCancelled(interceptClaimsListCommand(event.getPlayer(), event.getMessage()));
    }

    @EventHandler(ignoreCancelled = true)
    private void onServerPreprocess(ServerCommandEvent event)
    {
        event.setCancelled(interceptClaimsListCommand(event.getSender(), "/" + event.getCommand()));
    }

    private int getMiddle(int lesser, int greater)
    {
        return lesser + (greater - lesser / 2);
    }
}
