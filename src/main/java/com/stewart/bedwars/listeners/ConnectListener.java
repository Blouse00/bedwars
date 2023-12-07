package com.stewart.bedwars.listeners;

import com.stewart.bedwars.Bedwars;
import com.stewart.bedwars.GameState;
import com.stewart.bedwars.instance.Arena;
import com.stewart.bedwars.manager.ConfigManager;
import com.stewart.bedwars.utils.GameUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

// handles only people leaving and joining the servre
public class ConnectListener implements Listener {

    private Bedwars main;

    public ConnectListener(Bedwars main) {
        this.main = main;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {

        Player player = e.getPlayer();

        Arena arena =   main.getArenaManager().getFirstArena();
        // if the game has already started send the player back to the lobby
        if (arena.getState().equals(GameState.LIVE) || arena.getState().equals(GameState.FINISHING) ||
            arena.getPlayers().size() >= arena.getMaxPlayers()) {
            System.out.println("Game was started or at max players when player joined, returning them to lobby");
            arena.teleportPlayerToHub(player);
        } else {
            // when a player joins clear their inventory and enderchest
            player.getInventory().clear();
            player.getEnderChest().clear();
            // use my GameUtils class remove armour function to remove any armour they may have
            GameUtils.removeArmour(player);
            ItemStack compass = new ItemStack(Material.COMPASS);
            ItemMeta ism = compass.getItemMeta();
            ism.setDisplayName(ChatColor.RED + "Return to main hub");
            compass.setItemMeta(ism);
            player.getInventory().setItem(8, compass);
            player.getInventory().setHeldItemSlot(4);

            // make sure they have full health on join
            player.setHealth(20.0);
            player.setFoodLevel(20);
            // teleport them to the lobby spawn poin
            player.teleport(ConfigManager.getLobbySpawn());
            // at the moment there is only one arena world, if we add more we will need to
            // have a way to direct players to the correct one.
            main.getArenaManager().getFirstArena().addPlayer(player);
        }

        // update the hub world to pass the new player cont on this server
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(main, new Runnable(){
            @Override
            public void run(){
                main.getArenaManager().getFirstArena().updateLobby();
            }
        }, 4L);

    }

    // when a player leaves we fire the arena playerleftserver function
    // read more about what heppens there.
    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Arena arena = main.getArenaManager().getFirstArena();
        if (arena != null) {
            arena.playerLeftServer(e.getPlayer());
        }
    }

}
