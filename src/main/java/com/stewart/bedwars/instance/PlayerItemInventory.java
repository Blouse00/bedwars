package com.stewart.bedwars.instance;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;

// this is a class I made to more simply check how muck of each currency item
// a given player has in his inventory.  It also has functions to remove currency items.

public class PlayerItemInventory {

    private Player player;
    private int numIron;
    private int numGold;
    private int numDiamond;
    private int numEmerald;

    // the constructor only needs the player whos inventory we want to use
    public PlayerItemInventory(Player player) {
        this.player = player;
        checkInventory();
    }

    // fired on instance creation & sets variables showing how much of each currency item the
    // player has
    private void checkInventory() {
        Inventory inventory = player.getInventory();
        ItemStack[] items = inventory.getContents();
        numIron = checkNumItems(Material.IRON_INGOT, items);
        numGold = checkNumItems(Material.GOLD_INGOT, items);
        numDiamond = checkNumItems(Material.DIAMOND, items);
        numEmerald = checkNumItems(Material.EMERALD, items);
    }

    // this does the actual inventory checking for the item passed to it.
    // material is what we are checking for and items is the items to check through (the players inventory)
    private int checkNumItems(Material material, ItemStack[] items) {
        int val = 0;
        for (ItemStack item : items) {
            if ((item != null) && (item.getType() == material) && (item.getAmount() > 0)) {
                val += item.getAmount();
            }
        }
        return val;
    }

    // public variables used to return the number of each item the player has
    public int getNumIron() {return numIron;}
    public int getNumGold() {return numGold;}
    public int getNumDiamond() {return numDiamond;}
    public int getNumEmerald() {return numEmerald;}

    // public functions to remove items from the players inventory
    public void removeIron(int numToRremove) {
        player.getInventory().removeItem(new ItemStack(Material.IRON_INGOT, numToRremove));
    }
    public void removeGold(int numToRremove) {
        player.getInventory().removeItem(new ItemStack(Material.GOLD_INGOT, numToRremove));
    }
    public void removeEmerald(int numToRremove) {
        player.getInventory().removeItem(new ItemStack(Material.EMERALD, numToRremove));
    }
    public void removeDiamond(int numToRremove) {
        player.getInventory().removeItem(new ItemStack(Material.DIAMOND, numToRremove));
    }

    public boolean hasAmount(FileConfiguration pricesConfig, String configItem, boolean takePayment) {
        String currency = pricesConfig.getString(configItem + ".currency");
        int cost = pricesConfig.getInt(configItem + ".price");
        System.out.println(configItem + ".price");
        System.out.println("Take payment called, currency = " + currency + ", price = " + cost);
        if (currency.equals("iron")) {
            if (numIron >= cost) {
                if (takePayment) {
                    removeIron(cost);
                }
                return true;
            } else {
                return false;
            }
        }
        if (currency.equals("gold")) {
            if (numGold >= cost) {
                if (takePayment) {
                    removeGold(cost);
                }
                return true;
            } else {
                return false;
            }
        }
        if (currency.equals("diamond")) {
            if (numDiamond >= cost) {
                if (takePayment) {
                    removeDiamond(cost);
                }
                return true;
            } else {
                return false;
            }
        }
        if (currency.equals("emerald")) {
            if (numEmerald >= cost) {
                if (takePayment) {
                    removeEmerald(cost);
                }
                return true;
            } else {
                return false;
            }
        }
        System.out.println("currency not matching");
        return false;
    }


}
