package me.robomwm.claimslistclassifier.command;

import me.ryanhamshire.GriefPrevention.DataStore;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created on 9/28/2018.
 *
 * @author RoboMWM
 */
public abstract class CommandBase implements CommandExecutor, Listener
{
    protected JavaPlugin plugin;
    protected GriefPrevention griefPrevention;
    protected DataStore dataStore;

    public CommandBase(JavaPlugin plugin, GriefPrevention griefPrevention, DataStore dataStore)
    {
        this.plugin = plugin;
        this.griefPrevention = griefPrevention;
        this.dataStore = dataStore;
    }

    protected void registerListeners()
    {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public abstract boolean onCommand(CommandSender sender, Command cmd, String label, String[] args);
}
