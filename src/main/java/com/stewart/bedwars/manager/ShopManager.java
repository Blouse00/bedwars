package com.stewart.bedwars.manager;

import com.stewart.bedwars.Bedwars;
import com.stewart.bedwars.instance.Arena;
import com.stewart.bedwars.utils.GameUtils;
import com.stewart.bedwars.utils.ShopEntities;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Witch;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

// the shop manager is an instance of the arena class and is used to spawn and remove the shop keepers
public class ShopManager {

    private Arena arena;
    private Bedwars main;
    private FileConfiguration config;

    // keep lists of all the shop people so they can be removed again, not so critical now we restart the server.
    private List<Villager> lstVillager = new ArrayList<>();
    private List<Blaze> lstBlaze = new ArrayList<>();
    private List<Witch> lstWitch = new ArrayList<>();
    private List<PigZombie> lstPigZombie = new ArrayList<>();

    public ShopManager(Arena arena, Bedwars bedwars, FileConfiguration config) {
        this.config = config;
        this.arena = arena;
        this.main = bedwars;
    }

    // spawn all the shop keepers, called from the arena class when the game starts (0.5 seconds after actually)
    public void spawnAll() {
        System.out.println("spawn all called");
        World world = arena.getWorld();
        // loop through each villager in the config reading their spawn location
        for (String str : config.getConfigurationSection("shops.items").getKeys(false)) {
            Location location = new Location(world,
                    config.getDouble("shops.items." + str + ".x"),
                    config.getDouble("shops.items." + str + ".y"),
                    config.getDouble("shops.items." + str + ".z"),
                    (float) config.getDouble("shops.items." + str + ".yaw"),
                    (float) config.getDouble("shops.items." + str + ".pitch"));

            spawnVillager(world.getName(), location);


        }
        // then same as for villagers but for the other shop types.
        // it's witten slightly differently below but does the same thing.
        for (String str : config.getConfigurationSection("shops.upgrades").getKeys(false)) {
            Location location = new Location(world,
                    config.getDouble("shops.upgrades." + str + ".x"),
                    config.getDouble("shops.upgrades." + str + ".y"),
                    config.getDouble("shops.upgrades." + str + ".z"),
                    (float) config.getDouble("shops.upgrades." + str + ".yaw"),
                    (float) config.getDouble("shops.upgrades." + str + ".pitch"));

            spawnBlaze(world.getName(), location);

        }
        for (String str : config.getConfigurationSection("shops.enchanter").getKeys(false)) {
            Location location = new Location(world,
                    config.getDouble("shops.enchanter." + str + ".x"),
                    config.getDouble("shops.enchanter." + str + ".y"),
                    config.getDouble("shops.enchanter." + str + ".z"),
                    (float) config.getDouble("shops.enchanter." + str + ".yaw"),
                    (float) config.getDouble("shops.enchanter." + str + ".pitch"));

            spawnEnchanter(world.getName(), location);
        }
        for (String str : config.getConfigurationSection("shops.armourer").getKeys(false)) {
            Location location = new Location(world,
                    config.getDouble("shops.armourer." + str + ".x"),
                    config.getDouble("shops.armourer." + str + ".y"),
                    config.getDouble("shops.armourer." + str + ".z"),
                    (float) config.getDouble("shops.armourer." + str + ".yaw"),
                    (float) config.getDouble("shops.armourer." + str + ".pitch"));

            spawnPigZombie(world.getName(), location);
        }
    }

    private void spawnVillager(String worldname, Location location) {
        if (GameUtils.isChunkLoaded(worldname, location)) {
            Villager villager =  ShopEntities.makeVillager(location);
            GameUtils.spawnArmourStand(location, "Shopkeeper");
            lstVillager.add(villager);
        } else {
            new BukkitRunnable() {
                @Override
                public void run() {
                    spawnVillager(worldname, location);
                }
            }.runTaskLater(main, 1);
        }
    }

    private void spawnBlaze(String worldname, Location location) {
        if (GameUtils.isChunkLoaded(worldname, location)) {
            Blaze blaze =  ShopEntities.makeBlaze(location);
            GameUtils.spawnArmourStand(location, "Upgrades");
            lstBlaze.add(blaze);
        } else {
            new BukkitRunnable() {
                @Override
                public void run() {
                    spawnBlaze(worldname, location);
                }
            }.runTaskLater(main, 1);
        }
    }

    private void spawnEnchanter(String worldname, Location location) {
        if (GameUtils.isChunkLoaded(worldname, location)) {
            Witch witch =  ShopEntities.makeWitch(location);
            GameUtils.spawnArmourStand(location, "The Enchantress");
            lstWitch.add(witch);
        } else {
            new BukkitRunnable() {
                @Override
                public void run() {
                    spawnEnchanter(worldname, location);
                }
            }.runTaskLater(main, 1);
        }
    }

    private void spawnPigZombie(String worldname, Location location) {
        if (GameUtils.isChunkLoaded(worldname, location)) {
            PigZombie zombie =  ShopEntities.makePigZombie(location);
            GameUtils.spawnArmourStand(location, "The Armourer");
            lstPigZombie.add(zombie);
        } else {
            new BukkitRunnable() {
                @Override
                public void run() {
                    spawnPigZombie(worldname, location);
                }
            }.runTaskLater(main, 1);
        }
    }


    // called to remove all shop people at the end of the game (not required now we're doing server restart)
    public void removeAll() {
        for (Villager villager :lstVillager) {
            villager.remove();
        }
        for (Blaze blaze :lstBlaze) {
            blaze.remove();
        }
        for (Witch witch :lstWitch) {
            witch.remove();
        }
        for (PigZombie pigZombie :lstPigZombie) {
            pigZombie.remove();
        }
    }





}
