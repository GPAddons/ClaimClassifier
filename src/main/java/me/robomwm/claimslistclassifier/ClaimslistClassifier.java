package me.robomwm.claimslistclassifier;

import me.robomwm.claimslistclassifier.command.ClaimExpireCommand;
import me.robomwm.claimslistclassifier.command.ClaimsListCommand;
import me.robomwm.claimslistclassifier.command.NameClaimCommand;
import me.ryanhamshire.GriefPrevention.DataStore;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

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
        getConfig().addDefault("claimNaming", true);
        getConfig().addDefault("claimExpireDelay", false);

        if (getConfig().getBoolean("claimListSorting", true))
        {
            claimsListCommand = new ClaimsListCommand(this, dataStore);
            getCommand("claimslist").setExecutor(claimsListCommand);
        }

        if (getConfig().getBoolean("claimNaming", true))
        {
            File storageFile = new File(this.getDataFolder(), "names.data");
            claimNames = YamlConfiguration.loadConfiguration(storageFile);
            getCommand("nameclaim").setExecutor(new NameClaimCommand(this, dataStore));
        }

        if (getConfig().getBoolean("claimExpireDelay", false))
        {
            getCommand("claimexpire").setExecutor(new ClaimExpireCommand(this, griefPrevention.config_claims_expirationDays, dataStore));
        }

        Class clazz = ClaimExpireCommand.class;
    }

    private void initConfig()
    {


        getConfig().options().copyDefaults(true);
        saveConfig();
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
