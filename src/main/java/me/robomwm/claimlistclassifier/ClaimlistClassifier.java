package me.robomwm.claimlistclassifier;

import me.robomwm.claimlistclassifier.command.ClaimsListCommand;
import me.ryanhamshire.GriefPrevention.DataStore;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;

/**
 * Created on 1/1/2018.
 *
 * @author RoboMWM
 */
public class ClaimlistClassifier extends JavaPlugin implements Listener
{
    CommandExecutor claimsListCommand;
    public void onEnable()
    {
        DataStore dataStore = ((GriefPrevention)getServer().getPluginManager().getPlugin("GriefPrevention")).dataStore;
        claimsListCommand = new ClaimsListCommand(this, dataStore);
        getCommand("claimslist").setExecutor(claimsListCommand);
        getServer().getPluginManager().registerEvents(this, this);
    }

    //Other way is to hack into bukkit and remove the command from the commandmap
    public void interceptClaimsListCommand(Player player, String msg)
    {
        List<String> message = Arrays.asList(msg.split(" "));
        String command = message.get(0).toLowerCase().substring(1);

        switch (command)
        {
            case "claimslist":
            case "claimlist":
            case "listclaims":
                message.remove(0);
                String[] args = (String[])message.toArray();
                claimsListCommand.onCommand(player, null, command, args);
                break;
        }
    }

    @

}
