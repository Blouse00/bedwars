package com.stewart.bedwars.listeners;

import com.stewart.bedwars.Bedwars;
import com.stewart.bedwars.instance.Arena;
import com.stewart.bedwars.manager.ConfigManager;
import com.stewart.bedwars.utils.GameUtils;
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
        // when a player joins clear their inventory and enderchest
        Player player = e.getPlayer();
        player.getInventory().clear();
        player.getEnderChest().clear();
        // use my GameUtils class remove armour function to remove any armour they may have
        GameUtils.removeArmour(player);
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta ism = compass.getItemMeta();
        ism.setDisplayName(ChatColor.RED + "Return to main hub");
        compass.setItemMeta(ism);
        player.getInventory().setItem(8,compass);

        // make sure they have full health on join
        player.setHealth(20.0);
        player.setFoodLevel(20);


        FileConfiguration config = main.getConfig();
        for (String str : config.getConfigurationSection("bedwars-maps").getKeys(false)) {
            int teamColorInt = config.getInt("bedwars-maps." + str + ".colour-int");
            ItemStack wool = new ItemStack(new ItemStack(Material.WOOL, 1, (short) teamColorInt));
            ItemMeta voteMeta = wool.getItemMeta();
            voteMeta.setDisplayName("Vote for " + config.getString("bedwars-maps." + str + ".name") + " map.");
            wool.setItemMeta(voteMeta);
            player.getInventory().setItem(Integer.parseInt(str), wool);
        }

        player.getInventory().setHeldItemSlot(0);



        // teleport them to the lobby spawn poin
        player.teleport(ConfigManager.getLobbySpawn());
        // at the moment there is only one arena world, if we add more we will need to
        // have a way to direct players to the correct one.
        main.getArenaManager().getFirstArena().addPlayer(player);
        System.out.println("Update lobby on player join");
        // update the hub world to pass the new player cont on this server
        main.getArenaManager().getFirstArena().updateLobby();
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