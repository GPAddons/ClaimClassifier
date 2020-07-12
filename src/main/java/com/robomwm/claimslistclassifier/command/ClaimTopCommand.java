package com.robomwm.claimslistclassifier.command;

import co.aikar.taskchain.BukkitTaskChainFactory;
import co.aikar.taskchain.TaskChain;
import co.aikar.taskchain.TaskChainFactory;
import com.robomwm.claimslistclassifier.ClaimslistClassifier;
import me.ryanhamshire.GriefPrevention.DataStore;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Created on 5/28/2020.
 *
 * @author RoboMWM
 */
public class ClaimTopCommand extends CommandBase implements CommandExecutor
{
    ClaimslistClassifier plugin;
    List<Map.Entry<String, Integer>> sorted;
    TaskChainFactory taskChainFactory;

    public ClaimTopCommand(ClaimslistClassifier plugin, GriefPrevention griefPrevention, DataStore dataStore)
    {
        super(plugin, griefPrevention, dataStore);
        this.plugin = plugin;
        taskChainFactory = BukkitTaskChainFactory.create(plugin);
    }

    @Override
    public void registerCommand()
    {
        plugin.getCommand("claimtop").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (args.length > 0 && sorted != null)
            return parseAndPrintPage(sender, args[0], label);

        File playerDataFolder = new File("plugins" + File.separator + "GriefPreventionData" +
                File.separator + "PlayerData");

        File[] files = playerDataFolder.listFiles();

        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Ordering claimblock totals of &c" + files.length + " &6players, please wait..."));

        TaskChain chain = taskChainFactory.newChain();

        chain.async(() ->
                {
                    Map<String, Integer> uuidClaimblockMap = new HashMap<>();
                    long total = 0;

                    for (File file : playerDataFolder.listFiles())
                    {
                        try
                        {
                            List<String> lines = Files.readAllLines(file.toPath());
                            Iterator<String> iterator = lines.iterator();
                            iterator.next();
                            int totalBlocks = Integer.parseInt(iterator.next()) + Integer.parseInt(iterator.next());
                            uuidClaimblockMap.put(file.getName(), totalBlocks);
                            total += totalBlocks;
                        }
                        catch (IOException | NumberFormatException | NoSuchElementException e)
                        {
                            plugin.getLogger().warning("Skipping file " + file.getName());
                            //e.printStackTrace();
                            continue;
                        }

                        List<Map.Entry<String, Integer>> list = new ArrayList<>(uuidClaimblockMap.entrySet());
                        //Thank you EssX baltop
                        //lambdas are interesting
                        list.sort((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));

                        chain.setTaskData("list", list);
                        chain.setTaskData("total", total);
                    }
                }).sync(() ->
                {
                    sorted = (List<Map.Entry<String, Integer>>)(chain.getTaskData("list"));

                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&6Server Total: &c" + chain.getTaskData("total")));
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&6Top claimblock totals"));
                    print(sender, 1, label);
                }).execute();
        return true;
    }

    public boolean parseAndPrintPage(CommandSender sender, String pageNumber, String label)
    {
        int page;

        try
        {
            page = Math.abs(Integer.parseInt(pageNumber));
        }
        catch (NumberFormatException e)
        {
            return false;
        }

        print(sender, page, label);
        return true;
    }

    public void print(CommandSender sender, int page, String label)
    {
        int maxPerPage = 10;
        sender.sendMessage(ChatColor.translateAlternateColorCodes( '&'," &e---- &6Claimtop &e-- &6Page &c" + page + "&6/&c" + (int)Math.ceil((double)sorted.size() / maxPerPage) + " &e----"));
        int start = (page - 1) * maxPerPage;
        for (int i = start; i < Math.min(start + maxPerPage, sorted.size() - 1); i++)
        {
            Map.Entry<String, Integer> entry = sorted.get(i);
            sender.sendMessage(i + 1 + ". " + entry.getKey() + ": " + entry.getValue());
        }
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&6Type &c/" + label + " " + ++page + " &6to read the next page."));
    }
}
