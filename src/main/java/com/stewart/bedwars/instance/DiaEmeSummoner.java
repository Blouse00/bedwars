package com.stewart.bedwars.instance;

import com.stewart.bedwars.Bedwars;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public class DiaEmeSummoner {

    private Bedwars main;
    private String worldName;
    private Arena arena;

    private int diaStartSeconds;
    private int diaUpgradeSeconds;
    private int diaStartSpeed;
    private int diaUpgradeSpeed;

    private int emeStartSeconds;
    private int emeUpgradeSeconds;
    private int emeStartSpeed;
    private int emeUpgradeSpeed;
    private FileConfiguration config;

    private boolean diaUpgraded;
    private boolean emeUpgraded;

    private List<SummonerItem> diaSummonerItems;
    private List<SummonerItem> emeSummonerItems;

    // an instance of this class is used in the arena class
    // this class handles the mid diamond & emerald summoners
    // each summoner is a will be a summoner class instance (created by me, nothing to do with spigot etc)
    // and each summoner has summonerItem class instances for each item it drops (diaomnd iron etc) only the team
    // summoners have more than one item and they are handled in the team class.
    public DiaEmeSummoner(Bedwars main, Arena arena, String worldName, FileConfiguration config) {
        this.main = main;
        this.arena = arena;
        this.worldName = worldName;
        this.config = config;
        diaSummonerItems = new ArrayList<>();
        emeSummonerItems = new ArrayList<>();
        diaUpgraded = false;
        emeUpgraded = false;
        getSummonerValues();
        getSummonerItems();
    }

    // Loops through the diamond & emerald summoner information in the config file
    // and make a list of diamond summoner instances and emerald summoner instances for each one.
    private void getSummonerItems() {
        for (String str : config.getConfigurationSection("diamond-summoner.summoners").getKeys(false)) {
            Material material = Material.DIAMOND;
            org.bukkit.Location location = new org.bukkit.Location(
                    Bukkit.getWorld(worldName),
                    config.getDouble("diamond-summoner.summoners." + str + ".x"),
                    config.getDouble("diamond-summoner.summoners." + str + ".y"),
                    config.getDouble("diamond-summoner.summoners." + str + ".z"));
            diaSummonerItems.add(new SummonerItem(main, material, diaStartSpeed, location, Integer.parseInt(str), true));
            //System.out.println("diamond summoner added");
        }

        for (String str : config.getConfigurationSection("emerald-summoner.summoners").getKeys(false)) {
            Material material = Material.EMERALD;
            org.bukkit.Location location = new org.bukkit.Location(
                    Bukkit.getWorld(worldName),
                    config.getDouble("emerald-summoner.summoners." + str + ".x"),
                    config.getDouble("emerald-summoner.summoners." + str + ".y"),
                    config.getDouble("emerald-summoner.summoners." + str + ".z"));
          //  System.out.println("Emerald summoner location = " + location.toString());
            emeSummonerItems.add(new SummonerItem(main, material, emeStartSpeed, location, Integer.parseInt(str), true));
           // System.out.println("emerald summoner added");
        }
    }

    // this gets the diamond and emerald summoner variables from the config and sets them as class variables so the
    // are easier to use.
    private void getSummonerValues() {
        diaStartSeconds = config.getInt("diamond-summoner.start-after");
        diaUpgradeSeconds = config.getInt("diamond-summoner.upgrade-after");
        diaStartSpeed = config.getInt("diamond-summoner.start-speed");
        diaUpgradeSpeed = config.getInt("diamond-summoner.upgrade-speed");

        emeStartSeconds = config.getInt("emerald-summoner.start-after");
        emeUpgradeSeconds = config.getInt("emerald-summoner.upgrade-after");
        emeStartSpeed = config.getInt("emerald-summoner.start-speed");
        emeUpgradeSpeed = config.getInt("emerald-summoner.upgrade-speed");
    }

    // called when the diamond summoner needs to be upgraded
    private void upgradeDiamondSummoners() {
        if (diaUpgraded == false) {
            // loop through all the diamond summoner items
            for (SummonerItem item : diaSummonerItems) {
                // change their speed to the upgraded speed
                item.upgradeSummonerSpeed(diaUpgradeSpeed);
            }
            diaUpgraded = true;
        }
    }

    // called when the emerals summoner needs to be upgraded
    private void upgradeEmeraldSummoners() {
        if (emeUpgraded == false) {
            // loop through all the emerald summoner items
            for (SummonerItem item : emeSummonerItems) {
                // change their speed to the upgraded speed
                item.upgradeSummonerSpeed(emeUpgradeSpeed);
            }
            emeUpgraded = true;
        }
    }

    // fires every second from the start of the game
    public void onClockTick(int currentGameSeconds) {
        // check if it is time to upgrade the diamond summoners
        if (diaUpgraded == false && currentGameSeconds > diaUpgradeSeconds) {
            upgradeDiamondSummoners();
            arena.sendMessage(ChatColor.BLUE + "Diamond summoners upgraded!");
        }
        // check if it is time to start the diamond summoners
        if (currentGameSeconds == diaStartSeconds) {
         //   System.out.println("cgs " + currentGameSeconds + " // diastartseconds " + diaStartSeconds);
            arena.sendMessage(ChatColor.BLUE + "Diamond summoners started!");
        }
        // if it's after the diamond start time send a tick to the summoner item
        // the summoner item class will decide if it an item should be summoned or not
        // as it stores whe one was last dropped
        if (currentGameSeconds > diaStartSeconds) {
            for (SummonerItem item : diaSummonerItems) {
                item.onTick(currentGameSeconds, false);
            }
        } else {
            // still want to countdown armourstand till start
            for (SummonerItem item : diaSummonerItems) {
                item.tickBeforeStart(currentGameSeconds, diaStartSeconds);
            }
        }
        // same as above 3 checks but for emeralds
        if (emeUpgraded == false && currentGameSeconds > emeUpgradeSeconds) {
                upgradeEmeraldSummoners();
                arena.sendMessage(ChatColor.GREEN + "Emerald summoners upgraded!");
            }
        if (currentGameSeconds == emeStartSeconds) {
        //    System.out.println("cgs " + currentGameSeconds + " // emeStartSeconds " + emeStartSeconds);
            arena.sendMessage(ChatColor.GREEN + "Emerald summoners started!");
        }
        if (currentGameSeconds > emeStartSeconds) {
            for (SummonerItem item : emeSummonerItems) {
                item.onTick(currentGameSeconds, false);
            }
        } else {
            // still want to countdown armourstand till start
            for (SummonerItem item : emeSummonerItems) {
                item.tickBeforeStart(currentGameSeconds, emeStartSeconds);
            }
        }
    }
}
