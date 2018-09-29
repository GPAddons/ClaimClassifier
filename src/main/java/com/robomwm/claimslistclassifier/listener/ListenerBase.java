package com.robomwm.claimslistclassifier.listener;

import me.ryanhamshire.GriefPrevention.DataStore;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created on 9/28/2018.
 *
 * @author RoboMWM
 */
public abstract class ListenerBase implements Listener
{
    protected JavaPlugin plugin;
    protected GriefPrevention griefPrevention;
    protected DataStore dataStore;

    public ListenerBase(JavaPlugin plugin, GriefPrevention griefPrevention, DataStore dataStore)
    {
        this.plugin = plugin;
        this.griefPrevention = griefPrevention;
        this.dataStore = dataStore;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
}
