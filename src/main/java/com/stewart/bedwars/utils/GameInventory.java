package com.stewart.bedwars.utils;

import com.stewart.bedwars.Bedwars;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GameInventory {

    private Bedwars main;

    public GameInventory(Bedwars main) {
        this.main = main;
    }

    // the inventory that is returned to the listener class when it asks for the upgrade shop
    public Inventory getGameInventory(Player player) {

        Inventory inv = Bukkit.createInventory(player, 54, "" + ChatColor.DARK_GRAY + ChatColor.BOLD + "GAME MENU.");

        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta ism = compass.getItemMeta();
        ism.setDisplayName(ChatColor.RED + "Exit game and return to main lobby");
        compass.setItemMeta(ism);

        inv.setItem(22, compass);

        //FRAME
        ItemStack frame = new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.BLACK.getData());
        ItemMeta frameMeta = frame.getItemMeta();
        frameMeta.setDisplayName("");
        for (int i : new int[]{0,1,2,3,4,5,6,7,8,9,17,18,26,27,35,36,44,45,46,47,48,49,50,51,52,53})
            inv.setItem(i, frame);

        return inv;
    }

}
