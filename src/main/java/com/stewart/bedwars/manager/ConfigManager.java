package com.stewart.bedwars.manager;

import com.stewart.bedwars.Bedwars;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;


// the config manager is an instance of the main class
// and has functions to return the games minimum players, lobby spawn location & starting countdown seconds.
public class ConfigManager {

    private static FileConfiguration config;

    // constructor takes the main class
    public static void setupConfig( Bedwars main) {
        ConfigManager.config = main.getConfig();
        main.saveDefaultConfig();
    }

    /* public functions can be called from other classes */
    public  static int getRequiredPlayers(int teamSize) {
        System.out.println("get required players called teamsize = " + teamSize);
        if (teamSize == 1) {
         //   System.out.println("1 get required players called teamsize = " + config.getInt("required-players-1"));
            return config.getInt("required-players-1");
        }
        if (teamSize == 2) {
         //   System.out.println("2 get required players called teamsize = " + config.getInt("required-players-2"));
            return config.getInt("required-players-2");
        }
        if (teamSize == 4) {
         //   System.out.println("4 get required players called teamsize = " + config.getInt("required-players-4"));
            return config.getInt("required-players-4");
        }
        return 10;
    }
  //  public  static int getMaxPlayers() { return config.getInt("max-players");}
    public  static int getCountdownSeconds() { return config.getInt("countdown-seconds");}
  //  public  static int getTeamSize() { return config.getInt("team-size");}
    public  static Location getLobbySpawn() {
        return new Location(
                Bukkit.getWorld(config.getString("lobby-spawn.world")),
                config.getDouble("lobby-spawn.x"),
                config.getDouble("lobby-spawn.y"),
                config.getDouble("lobby-spawn.z"),
                (float) config.getDouble("lobby-spawn.yaw"),
                (float) config.getDouble("lobby-spawn.pitch"));
    }


}
