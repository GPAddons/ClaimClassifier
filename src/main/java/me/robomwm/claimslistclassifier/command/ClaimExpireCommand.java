package me.robomwm.claimslistclassifier.command;

import me.ryanhamshire.GriefPrevention.events.ClaimExpirationEvent;
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

    public ClaimExpireCommand(JavaPlugin plugin, int expiration)
    {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        prolongedExpirationFile = new File(plugin.getDataFolder() + File.separator + "prolongedExpirations.yml");
        prolongedExpiration = YamlConfiguration.loadConfiguration(prolongedExpirationFile);
        this.defaultExpiration = expiration;
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
                sender.sendMessage(player.getName() + "'s claims will expire after " + getExpirationDays(player) + " days of inactivity.");
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
        //default - (current - lastPlayed)
        long lastPlayed = player.getLastPlayed();
        return (int)TimeUnit.MILLISECONDS.toDays(
                getDefaultExpirationInMillis() - (System.currentTimeMillis() - lastPlayed));
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
        event.setCancelled(prolongedExpiration.getLong(event.getClaim().ownerID.toString()) > System.currentTimeMillis());
    }

}
