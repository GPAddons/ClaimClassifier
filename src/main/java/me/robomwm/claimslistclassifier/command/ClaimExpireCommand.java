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
        prolongedExpirationFile = new File(plugin.getDataFolder() + File.pathSeparator + "prolongedExpirations.yml");
        prolongedExpiration = YamlConfiguration.loadConfiguration(prolongedExpirationFile);
        this.defaultExpiration = expiration;
    }

    long getDefaultExpirationInMillis()
    {
        return TimeUnit.DAYS.toMillis(defaultExpiration) + System.currentTimeMillis();
    }

    OfflinePlayer getPlayer(CommandSender sender, String[] args, int i)
    {
        if (args.length > i)
            return plugin.getServer().getOfflinePlayer(args[i]);
        if (sender instanceof Player)
            return (OfflinePlayer)sender;
        return null;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (args.length == 0)
            return false;

        OfflinePlayer player = getPlayer(sender, args, 1);

        //Command requires a player
        if (player == null)
            return false;

        //No permissions to check others
        if (player != sender && !sender.isOp())
            return false;

        switch(args[0].toLowerCase())
        {
            case "check":
                sender.sendMessage(player.getName() + "'s claims will expire after " + getExpirationDays(player) + " of inactivity.");
                return true;
            case "delay":
                //No permissions to set delay
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
                sender.sendMessage(player.getName() + "'s claims will expire after " + getExpirationDays(player) + " of inactivity.");
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
            return (int)TimeUnit.MILLISECONDS.toDays(extendedExpiration);
        }

        //Return the default expiration if the player is online
        if (player.isOnline())
            return defaultExpiration;

        //Else do math based on when player last logged in
        long lastPlayed = player.getLastPlayed();
        return (int)TimeUnit.MILLISECONDS.toDays(getDefaultExpirationInMillis() - lastPlayed);
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
