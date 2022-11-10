package com.stewart.bedwars;

import com.gmail.tracebachi.SockExchange.Messages.ReceivedMessage;
import com.gmail.tracebachi.SockExchange.Messages.ReceivedMessageNotifier;
import com.gmail.tracebachi.SockExchange.Spigot.SockExchangeApi;
import com.google.common.io.ByteArrayDataInput;
import com.stewart.bedwars.command.ArenaCommand;
import com.stewart.bedwars.instance.Arena;
import com.stewart.bedwars.listeners.ConnectListener;
import com.stewart.bedwars.listeners.GameListener;
import com.stewart.bedwars.manager.ArenaManager;
import com.stewart.bedwars.manager.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.Consumer;

public final class Bedwars extends JavaPlugin {

    private ArenaManager arenaManager;
    private SockExchangeApi sockExchangeApi;
    private ReceivedMessageNotifier messageNotifier;

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

        // this is used to receive sock exchange messages from the lobby
        sockExchangeApi = SockExchangeApi.instance();

        // Get the request notifier which will run a provided Consumer when
        // there is a new message on a specific channel
        messageNotifier = sockExchangeApi.getMessageNotifier();

        Consumer<ReceivedMessage> requestConsumer = rm -> {
            System.out.println("Message received");
            try {
                ByteArrayDataInput in = rm.getDataInput();
                String s = in.readLine();
                if (s != null) {
                    System.out.println("SockExchange message received " + s);
                    // s will be the message
                    String[] arrReceived = s.split("\\.");
                    if (arrReceived[1].equals("request-status")) {
                        Arena arena = arenaManager.getFirstArena();
                        arena.updateLobby();
                    }

                } else {
                    System.out.println("Received message is null");
                }

                // Pass the message to the Game class which handles keeping track of server statuses

                // if the message

            } catch (Exception ex) {
                System.out.println("Sock exchange received message error");
                ex.printStackTrace();
            }
        };

        // this registers the listener for messages from other servers
        messageNotifier.register("LobbyChannel", requestConsumer);
    }

    // returns the arena manager class instance
    public ArenaManager getArenaManager() {return arenaManager;}

    // returns the arena manager class instance
    public SockExchangeApi getSockExchangeApi() {return sockExchangeApi;}

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
