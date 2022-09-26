package com.stewart.bedwars.manager;

import com.stewart.bedwars.Bedwars;
import com.stewart.bedwars.GameState;
import com.stewart.bedwars.instance.Arena;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

// the arena manager class isn't really needed for the bedwars game as we just have one arena in the world.
// even if we use my proposed  map vote thing with say 4 maps on the same server only one will be being used
// at a time.
// The reason I put it in here is for if we use the core of the bedewars code to build any other game taht does require
// multiple arenas the code is here.
// how it works is this sits between the main class and the game arena.  This would normall hava a list of all the
// arenas on the server but for this bedwars there is only ever one arena in the list.
// the arenas are defined in the config file.  The bedwars config has an arena list with one arena in it.
// if we go to have mutlpie bedwars maps on the server (for voting purposes) We would probably need to chage it
// so there is a config file for each map.  I currently have everything in one config file.
public class ArenaManager {

    private List<Arena> arenas = new ArrayList<>();

    // when created the arena manger fills the list of arenas by reading them from the config file
    public ArenaManager(Bedwars main) {
        FileConfiguration config = main.getConfig();
        for (String str : config.getConfigurationSection("arenas").getKeys(false)) {
            arenas.add(new Arena(main, config.getString("arenas." + str + ".world"), str));
        }
    }

    // return all the arenas, not used in bedwars
    public  List<Arena> getArenas() {return  arenas;}

    // get the arena the passed player is in
    public  Arena getArena(Player player) {
        // loop through all the arenas
        for(Arena arena : arenas) {
            // check if the arenas list of players conatins that player
            if (arena.getPlayers().contains(player.getUniqueId())) {
                return arena;
            }
        }
        System.out.println("arena not found for player " + player.getName());
        return  null;
    }

    // get an arena by its id, not used in bedwars
    public  Arena getArena(int id) {
        for(Arena arena : arenas) {
            if (arena.getId() == id) {
                return arena;
            }
        }
        return  null;
    }
    public Arena getFirstArena() {return arenas.get(0);}
}
