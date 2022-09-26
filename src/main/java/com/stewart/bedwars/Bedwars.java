package com.stewart.bedwars;

import com.stewart.bedwars.command.ArenaCommand;
import com.stewart.bedwars.listeners.ConnectListener;
import com.stewart.bedwars.listeners.GameListener;
import com.stewart.bedwars.manager.ArenaManager;
import com.stewart.bedwars.manager.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

public final class Bedwars extends JavaPlugin {

    private ArenaManager arenaManager;

    @Override
    public void onEnable() {
        // setup config file
        ConfigManager.setupConfig(this);
        // get the arena manager, for bedwars it will only contain one arena.  Read the comments in the arenaManger class
        // for more information on why I did it like this
        arenaManager = new ArenaManager(this);

        // need this to be able to move players between servers using bungeecord
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        // this is the world the game is played on.
        World world= Bukkit.getWorld("world");
        world.setTime((long) 6000);
        world.setAutoSave(false);

        // register connect & game listener classes
        Bukkit.getPluginManager().registerEvents(new ConnectListener(this), this);
        Bukkit.getPluginManager().registerEvents(new GameListener(this), this);

        // register the pw command class
        getCommand("pw").setExecutor(new ArenaCommand(this));

        // update the hub server once this one has started, see the comments for the upgradeLobby function
        // in the arena class for more details on this.
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable(){
            @Override
            public void run(){
                System.out.println("Update lobby from main (server start)");
                arenaManager.getFirstArena().updateLobby();
            }
        }, 60L);
    }

    // returns the arena manager class instance
    public ArenaManager getArenaManager() {return arenaManager;}

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
