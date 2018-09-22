package me.robomwm.claimslistclassifier.command;

import me.ryanhamshire.GriefPrevention.DataStore;
import me.ryanhamshire.GriefPrevention.events.ClaimExpirationEvent;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created on 9/19/2018.
 *
 * @author RoboMWM
 */
public class ClaimExpireCommand implements Listener, CommandExecutor
{
    private int defaultExpiration;
    private JavaPlugin plugin;
    private File prolongedExpirationFile;
    private YamlConfiguration prolongedExpiration;
    private DataStore dataStore;

    public ClaimExpireCommand(JavaPlugin plugin, int expiration, DataStore dataStore)
    {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        prolongedExpirationFile = new File(plugin.getDataFolder() + File.separator + "prolongedExpirations.yml");
        prolongedExpiration = YamlConfiguration.loadConfiguration(prolongedExpirationFile);
        this.defaultExpiration = expiration;
        this.dataStore = dataStore;
    }

    long getDefaultExpirationInMillis()
    {
        return TimeUnit.DAYS.toMillis(defaultExpiration) + System.currentTimeMillis();
    }

    //This is a stupid idea
    OfflinePlayer getPlayer(CommandSender sender, String[] args, int i, int maxSize)
    {
        if (args.length > i && args.length >= maxSize)
            return plugin.getServer().getOfflinePlayer(args[i]);
        if (sender instanceof Player)
            return (OfflinePlayer)sender;
        return null;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (args.length == 0)
            return false;

        OfflinePlayer player;

        switch(args[0].toLowerCase())
        {
            case "check":
                player = getPlayer(sender, args, 1, 2);
                if (player == null)
                    return false;
                if (player != sender && !sender.isOp())
                    return false;
                int daysToExpire = getExpirationDays(player);
                if (daysToExpire >= 0)
                    sender.sendMessage(player.getName() + "'s claims will expire after " + daysToExpire + " days of inactivity.");
                else if (!dataStore.getPlayerData(player.getUniqueId()).getClaims().isEmpty())
                    sender.sendMessage(player.getName() + "'s claims are pending expiration.");
                else
                    sender.sendMessage(player.getName() + "'s claims have expired due to inactivity.");
                return true;
            case "delay":
                player = getPlayer(sender, args, 1, 3);
                if (player == null)
                    return false;
                if (!sender.isOp())
                    return false;
                try
                {
                    if (!extendExpiration(player.getUniqueId().toString(), Integer.valueOf(args[args.length - 1])))
                        sender.sendMessage("Failed to save delay extension. See console for details.");
                }
                catch (NumberFormatException e)
                {
                    return false;
                }
                sender.sendMessage("Successfully extended " + player.getName() + "'s expiration days.");
                sender.sendMessage(player.getName() + "'s claims will expire after " + getExpirationDays(player) + " days of inactivity.");
                return true;
        }
        return false;
    }

    /**
     * @param player can be null
     * @return remaining days, rounded down
     */
    public int getExpirationDays(OfflinePlayer player)
    {
        //First return what we have stored, if it's longer than the default expiration
        long extendedExpiration = prolongedExpiration.getLong(player.getUniqueId().toString());
        if (extendedExpiration > getDefaultExpirationInMillis())
        {
            return (int)TimeUnit.MILLISECONDS.toDays(extendedExpiration - System.currentTimeMillis());
        }

        //Return the default expiration if the player is online
        if (player.isOnline())
            return defaultExpiration;

        //Else do math based on when player last logged in
        long lastPlayed = player.getLastPlayed();
        plugin.getLogger().info("Server reports " + player.getName() + " played " + TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - lastPlayed) + " ago");
        
        //Calculate absolute time this player's claims will expire
        long expireTime = TimeUnit.DAYS.toMillis(defaultExpiration) + lastPlayed;
        //Subtract current time to get remaining time left relative to now
        expireTime = expireTime - System.currentTimeMillis();
        return (int)TimeUnit.MILLISECONDS.toDays(expireTime);
    }

    public boolean extendExpiration(String uuidString, int days)
    {
        long currentDelay = prolongedExpiration.getLong(uuidString);
        if (currentDelay <= System.currentTimeMillis())
            currentDelay = getDefaultExpirationInMillis();
        prolongedExpiration.set(uuidString, currentDelay + TimeUnit.DAYS.toMillis(days));
        return save();
    }

    public boolean save()
    {
        try
        {
            prolongedExpiration.save(prolongedExpirationFile);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    private void onClaimExpire(ClaimExpirationEvent event)
    {
        //Cancel if the expiration time is greater than the current time (expiration is in the future)
        if (prolongedExpiration.getLong(event.getClaim().ownerID.toString()) > System.currentTimeMillis())
        {
            plugin.getLogger().info("Prevented a claim owned by player UUID " + event.getClaim().ownerID.toString() + " from expiring.");
            event.setCancelled(true);
        }
        else
        {
            OfflinePlayer player = plugin.getServer().getOfflinePlayer(event.getClaim().ownerID);
            if (player.getName() != null)
                plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "mail send " + player.getName() + " Your old claims have expired due to inactivity. A grace period for new claims can be checked by typing the command " + ChatColor.GREEN + "/claimexpire check");
        }
    }

}
