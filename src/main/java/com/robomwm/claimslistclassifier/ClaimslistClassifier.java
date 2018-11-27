package com.robomwm.claimslistclassifier;

import com.robomwm.claimslistclassifier.command.ClaimExpireCommand;
import com.robomwm.claimslistclassifier.command.ClaimsListCommand;
import com.robomwm.claimslistclassifier.command.CommandBase;
import com.robomwm.claimslistclassifier.command.NameClaimCommand;
import com.robomwm.claimslistclassifier.command.TrustedClaimsListCommand;
import com.robomwm.claimslistclassifier.listener.ConfirmAbandonClaimListener;
import com.robomwm.claimslistclassifier.listener.ListenerBase;
import me.ryanhamshire.GriefPrevention.DataStore;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bstats.bukkit.Metrics;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.concurrent.Callable;

/**
 * Created on 1/1/2018.
 *
 * @author RoboMWM
 */
public class ClaimslistClassifier extends JavaPlugin
{
    private CommandExecutor claimsListCommand;
    private YamlConfiguration claimNames;

    public YamlConfiguration getClaimNames()
    {
        return claimNames;
    }

    public void onEnable()
    {
        GriefPrevention griefPrevention = (GriefPrevention)getServer().getPluginManager().getPlugin("GriefPrevention");
        DataStore dataStore = griefPrevention.dataStore;

        getConfig().addDefault("claimListSorting", true);
        getConfig().addDefault("ClickableClaimslist", false);
        getConfig().addDefault("claimNaming", true);
        getConfig().addDefault("claimExpireDelay", false);

        if (getConfig().getBoolean("claimListSorting", true))
        {
            claimsListCommand = new ClaimsListCommand(this, dataStore);
            getCommand("claimslist").setExecutor(claimsListCommand);
            getLogger().info("Enabled sorted claimslist");
        }

        if (getConfig().getBoolean("claimNaming", true))
        {
            File storageFile = new File(this.getDataFolder(), "names.data");
            claimNames = YamlConfiguration.loadConfiguration(storageFile);
            getCommand("nameclaim").setExecutor(new NameClaimCommand(this, dataStore));
            getLogger().info("Enabled claimNaming");
        }

        if (getConfig().getBoolean("claimExpireDelay", false))
        {
            getCommand("claimexpire").setExecutor(new ClaimExpireCommand(this, griefPrevention.config_claims_expirationDays, dataStore));
            getLogger().info("Enabled claimExpireDelay");
        }

        enableCommand(new TrustedClaimsListCommand(this, griefPrevention, dataStore));
        enableListener(new ConfirmAbandonClaimListener(this, griefPrevention, dataStore));

        getConfig().options().copyDefaults(true);
        saveConfig();

        try
        {
            Metrics metrics = new Metrics(this);
            metrics.addCustomChart(new Metrics.SimplePie("bukkit_impl", new Callable<String>()
            {
                @Override
                public String call() throws Exception
                {
                    return getServer().getVersion().split("-")[1];
                }
            }));

            for (String key : getConfig().getKeys(false))
            {
                metrics.addCustomChart(new Metrics.SimplePie(key.toLowerCase(), new Callable<String>()
                {
                    @Override
                    public String call() throws Exception
                    {
                        return getConfig().getString(key);
                    }
                }));
            }
        }
        catch (Throwable ignored){}
    }

    private void enableCommand(CommandBase commandBase)
    {
        getConfig().addDefault(commandBase.getClass().getSimpleName(), false);
        if (getConfig().getBoolean(commandBase.getClass().getSimpleName(), false))
        {
            commandBase.registerCommand();
            if (commandBase.registerListeners())
                getLogger().info("Enabled " + commandBase.getClass().getSimpleName() + " and applicable listeners.");
            else
                getLogger().info("Enabled " + commandBase.getClass().getSimpleName());
        }
    }

    private void enableListener(ListenerBase listenerBase)
    {
        getConfig().addDefault(listenerBase.getClass().getSimpleName(), false);
        if (getConfig().getBoolean(listenerBase.getClass().getSimpleName(), false))
        {
            listenerBase.registerListeners();
            getLogger().info("Enabled " + listenerBase.getClass().getSimpleName());
        }
    }

    public void onDisable()
    {
        saveClaimNames();
    }

    public boolean saveClaimNames()
    {
        File storageFile = new File(this.getDataFolder(), "names.data");
        try
        {
            claimNames.save(storageFile);
        }
        catch (Exception e)
        {
            this.getLogger().severe("Claim names could not be saved! Any newly-named claims will not persist a server restart.");
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
