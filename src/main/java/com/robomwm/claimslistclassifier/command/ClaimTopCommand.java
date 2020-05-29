package com.robomwm.claimslistclassifier.command;

import com.robomwm.claimslistclassifier.ClaimslistClassifier;
import me.ryanhamshire.GriefPrevention.DataStore;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created on 5/28/2020.
 *
 * @author RoboMWM
 */
public class ClaimTopCommand extends CommandBase implements CommandExecutor
{
    ClaimslistClassifier plugin;

    public ClaimTopCommand(ClaimslistClassifier plugin, GriefPrevention griefPrevention, DataStore dataStore)
    {
        super(plugin, griefPrevention, dataStore);
        this.plugin = plugin;
    }

    @Override
    public void registerCommand()
    {
        plugin.getCommand("trustedclaimslist").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                sender.sendMessage("Claimblock totals");

                File playerDataFolder = new File("plugins" + File.separator + "GriefPreventionData" +
                        File.separator + "PlayerData");

                Map<String, Integer> uuidClaimblockMap = new HashMap<>();

                for (File file : playerDataFolder.listFiles())
                {
                    try
                    {
                        List<String> lines = Files.readAllLines(file.toPath());
                        Iterator<String> iterator = lines.iterator();
                        iterator.next();
                        int totalBlocks = Integer.parseInt(iterator.next()) + Integer.parseInt(iterator.next());
                        uuidClaimblockMap.put(file.getName(), totalBlocks);
                    }
                    catch (IOException | NumberFormatException e)
                    {
                        e.printStackTrace();
                        continue;
                    }
                }

                //Thank you EssX baltop for preventing me from doing something complicated
                //lambdas are interesting
                List<Map.Entry<String, Integer>> sorted = new ArrayList<>(uuidClaimblockMap.entrySet());
                sorted.sort((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));

                for (Map.Entry<String, Integer> entry : sorted)
                {
                    sender.sendMessage(entry.getKey() + ": " + entry.getValue());
                }
            }
        }.runTaskAsynchronously(plugin);
        return true;
    }
}
