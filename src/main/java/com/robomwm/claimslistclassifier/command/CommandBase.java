package com.robomwm.claimslistclassifier.command;

import com.robomwm.claimslistclassifier.ClaimslistClassifier;
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
    protected ClaimslistClassifier plugin;
    protected GriefPrevention griefPrevention;
    protected DataStore dataStore;
    protected boolean isListener = false;

    public CommandBase(ClaimslistClassifier plugin, GriefPrevention griefPrevention, DataStore dataStore)
    {
        this.plugin = plugin;
        this.griefPrevention = griefPrevention;
        this.dataStore = dataStore;
    }

    public abstract void registerCommand();

    protected void registerCommand(String command)
    {
        plugin.getCommand(command).setExecutor(this);
    }

    public boolean registerListeners()
    {
        if (!isListener)
            return false;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        return true;
    }

    public abstract boolean onCommand(CommandSender sender, Command cmd, String label, String[] args);
}
