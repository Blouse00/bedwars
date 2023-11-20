package com.stewart.bedwars.utils;

import com.stewart.bedwars.Bedwars;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class MapVoteInventory {


    private Bedwars main;

    public MapVoteInventory(Bedwars main) {
        this.main = main;
    }

    // the inventory that is returned to the listener class when it asks for the upgrade shop
    public Inventory getMapVoteInventory(Player player) {

        int teamSize = main.getArenaManager().getFirstArena().getTeamSize();

        Inventory inv = Bukkit.createInventory(player, 54, "" + ChatColor.DARK_GRAY + ChatColor.BOLD + "VOTE FOR A MAP.");

        FileConfiguration config = main.getConfig();
        String mapPath = "solo-maps";
        if (teamSize == 2) mapPath = "duo-maps";
        if (teamSize == 4) mapPath = "quad-maps";
        for (String str : config.getConfigurationSection(mapPath).getKeys(false)) {
            int teamColorInt = config.getInt( mapPath + "." + str + ".colour-int");
            int slotInt = config.getInt( mapPath + "." + str + ".slot");
            ItemStack inkSack = new ItemStack(new ItemStack(Material.INK_SACK, 1, (short) teamColorInt));
            ItemMeta voteMeta = inkSack.getItemMeta();
            voteMeta.setDisplayName("Vote for " + config.getString(mapPath + "."  + str + ".name") + " map.");
            inkSack.setItemMeta(voteMeta);
            inv.setItem(slotInt, inkSack);
        }

        //FRAME
        ItemStack frame = new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.BLACK.getData());
        ItemMeta frameMeta = frame.getItemMeta();
        frameMeta.setDisplayName("");
        for (int i : new int[]{0,1,2,3,4,5,6,7,8,9,17,18,26,27,35,36,44,45,46,47,48,49,50,51,52,53})
            inv.setItem(i, frame);

        return inv;
    }



}
