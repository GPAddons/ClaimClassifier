package me.robomwm.claimslistclassifier;

import me.robomwm.claimslistclassifier.command.ClaimsListCommand;
import me.robomwm.claimslistclassifier.command.NameClaimCommand;
import me.ryanhamshire.GriefPrevention.DataStore;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created on 1/1/2018.
 *
 * @author RoboMWM
 */
public class ClaimslistClassifier extends JavaPlugin implements Listener
{
    private CommandExecutor claimsListCommand;
    private YamlConfiguration claimNames;

    public YamlConfiguration getClaimNames()
    {
        return claimNames;
    }

    public void onEnable()
    {
        saveConfig();

        File storageFile = new File(this.getDataFolder(), "names.data");
        if (!storageFile.exists())
        {
            try
            {
                storageFile.createNewFile();
            }
            catch (IOException e)
            {
                this.getLogger().severe("Could not create names.data! Claim naming will be disabled.");
                e.printStackTrace();
                return;
            }
        }
        claimNames = YamlConfiguration.loadConfiguration(storageFile);

        DataStore dataStore = ((GriefPrevention)getServer().getPluginManager().getPlugin("GriefPrevention")).dataStore;
        claimsListCommand = new ClaimsListCommand(this, dataStore);
        getCommand("claimslist").setExecutor(claimsListCommand);
        getCommand("nameclaim").setExecutor(new NameClaimCommand(this, dataStore));
        getServer().getPluginManager().registerEvents(this, this);
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
                claimsListCommand.onCommand(sender, null, command, args);
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

}
