package com.stewart.bedwars.instance;

import com.stewart.bedwars.Bedwars;
import com.stewart.bedwars.GameState;
import com.stewart.bedwars.team.Team;
import com.stewart.bedwars.utils.GameUtils;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// the game class handles much the same type of stuff as arena, i could probably have combined the two.
public class Game {

    private Bedwars main;
    private Arena arena;
    // keeps a list of all people currently spawn protected and the game time it started
    private HashMap<UUID, Integer> playerSpawnProtect = new HashMap<>();
    // game time passed in seconds
    private int gameSeconds;
    // game time as a string (mm:ss)
    private String gameTime;
    // this stores which players are currently spectators due to having just died and waiting to respawn and the game time
    // it started
    private HashMap<UUID, Integer> playerSpectatorForRespawn = new HashMap<>();

    private  int particleIterator = 19; // Task will run 10 times.
    private BukkitTask particleTask = null;

    public Game(Bedwars main, Arena arena) {
        this.arena = arena;
        this.gameSeconds = 0;
        this.main = main;
        this.gameTime = "0:00";
    }

    // when the game starts, simialar thing in the arena class that fires this.
    public void start() {
        // set the game state & game time variables
        arena.setState(GameState.LIVE);
        gameSeconds = 0;
        gameTime = "0:00";
        // make sure these two lists are empty at the start of the game
        playerSpawnProtect = new HashMap<>();
        playerSpectatorForRespawn = new HashMap<>();
        // give all players dyed leather armour
        for(UUID uuid : arena.getPlayers()) {
            Player player = Bukkit.getPlayer(uuid);
            player.getInventory().clear();
         //  player.getInventory().addItem(new ItemStack(Material.DIAMOND, 64));
        //    player.getInventory().addItem(new ItemStack(Material.EMERALD, 64));
         //  player.getInventory().addItem(new ItemStack(Material.FIREBALL, 64));
          //  player.getInventory().addItem(new ItemStack(Material.TNT, 64));
         //   player.getInventory().addItem(new ItemStack(Material.WOOL, 64));
          //  player.getInventory().addItem(new ItemStack(Material.IRON_INGOT, 64));

            //  player.getInventory().addItem(new ItemStack(332, 20));
         //   player.getInventory().addItem(new ItemStack(Material.SNOW_BALL, 50));
         //   player.getInventory().addItem(new ItemStack(Material.CARPET, 50));
            // the teams are a property of the arena instance.  This class (game) is also a property of the arena instance.
            // To get the team instance from here we have to get it from the arena instance that was passed to this class
            // when it was made using the 'arena' public function getteam(UUID) then the 'teams' public function getTeamColor();
            Color teamColor = arena.getTeam(uuid).getTeamColor();
            player.getInventory().setHelmet(createItem(Material.LEATHER_HELMET, teamColor));
            player.getInventory().setChestplate(createItem(Material.LEATHER_CHESTPLATE, teamColor));
            player.getInventory().setLeggings(createItem(Material.LEATHER_LEGGINGS, teamColor));
            player.getInventory().setBoots(createItem(Material.LEATHER_BOOTS, teamColor));
            player.closeInventory();

            GameUtils.makePlayerArmourUnbreakable(player);


        }
    }

    // this is used by the above function to create each piece of armour.  Saves duplicating code.
    private static ItemStack createItem(Material leatherPiece, Color color) {
        ItemStack item = new ItemStack(leatherPiece);
        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
        meta.setColor(color);
        item.setItemMeta(meta);
        return item;
    }

    /* public functions, only these can be called from another class */

    // Called from the game listener player damage event when a player takes an amount of damage that would have killed
    // them.  This damage will have been cancelled.
    // If the player was killed by another player they will be in the killer variable, if not that will be null
    public void playerKilled(Player died, Player killer) {
        if (killer == null) {
            // no killer, just send a message that the player died.  The player damage event can tell you (as an enum)
            // how the damage was taken (void, fire, projectile etc) so in the future we could show a better message here.
            arena.sendMessage(died.getName() + " died!");
        } else {
            // player was killed by another player, show a message to the arena with killed and killer
            arena.sendMessage(killer.getName() + " killed " + died.getName());
            // each players number of kills is stored in their team instance so to log the kill
            // I need to get the killers team from the arena class
            Team killerTeam = arena.getTeam(killer.getUniqueId());
            // call the addkill function in the players team instance (logs it to the player, not the team)
            if (killerTeam != null) {
                killerTeam.addKill(killer.getUniqueId());
            }
        }
        // stop them taking fall damage if teleported after falling
        died.setFallDistance(0F);

        // where the killed player is spawned depends on the game state and if they have a bed or not
        if (arena.getState() == GameState.RECRUITING || arena.getState() == GameState.COUNTDOWN) {
            // game is still in recruiting phase respawn dead player at lobby
            died.teleport(arena.getSpawn());
        } else if (arena.getState() == GameState.FINISHING) {
            // game has been won and we are in the short finishing start, spawn them as a spectator
            GameUtils.dropInventory(died);
            died.setGameMode(GameMode.SPECTATOR);
            died.teleport(arena.getSpectatorSpawn());
        } else {
            // game state live
            // check if died player has team bed, first get the players team instance
            Team diedTeam = arena.getTeam(died.getUniqueId());
            GameUtils.dropInventory(died);
            if (diedTeam == null) {
                // this should not happen but just for debugging
                System.out.println("player " + died.getName() + " died but was not in a team!");
            } else {
                // golem & silverfish untarget killed player
                arena.untargetPlayer(died);
                if (diedTeam.hasBed()) {
                    // if so spawn them back at their base
                    // first spawn them as a spectator for 5 seconds
                    died.setGameMode(GameMode.SPECTATOR);
                    System.out.println("Player died, making spectator & teleporting to spactator");
                    died.teleport(arena.getSpectatorSpawn());
                    // add them to the playerSpectatorForRespawn list, this is checked every second to see if it is
                    // time spawn them back at their base
                    playerSpectatorForRespawn.put(died.getUniqueId(), gameSeconds);
                } else {
                    // No bed spawn them as a spectator
                    died.setGameMode(GameMode.SPECTATOR);
                    // remove them from their team
                    diedTeam.removePlayer(died.getUniqueId());
                    died.teleport(arena.getSpectatorSpawn());
                    // this should remove the scoreboard
                    died.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
                    // then check to see if the game has been won.
                    arena.checkGameWon();
                    if (diedTeam.getNumPlayers() == 0) {
                        diedTeam.setSummonerQuarterSpeed();
                        // update all teams scoreboards - teams alive
                        arena.updateTeamsScoreboard();
                    }
                }
            }
        }
    }

    // returns if the passed player is currently spawn protected or not, used in the game listener when a player takes
    // damage from another player
    public boolean playerSpawnProtected(Player player) {
        return playerSpawnProtect.containsKey(player.getUniqueId());
    }

    // called each time the game click ticks one second
    public void clockTick() {
        // add one onto the game seconds
        this.gameSeconds += 1;
        // this sets the game time (mm:ss) from the game senconds.
        this.gameTime =  String.format("%02d:%02d",  (gameSeconds % 3600) / 60, gameSeconds % 60);

        if (playerSpawnProtect.size() > 0) {
            // show particles around spawn protected players
            particleIterator = 19;
            particleTask = null;
            showPlayerParticles();
        }

        // BEDS DESTROYED

        if (gameSeconds == 2280) {  // 38 mins will be 2280 seconds
            // countdown 2 mins till all beds broken
            arena.sendSubTitle("2 minutes until all beds broken!");
        }

        if (gameSeconds == 2340) {  // 39 mins will be 2340 seconds
            // countdown 1 mins till all beds broken
            arena.sendSubTitle("1 minute until all beds broken!");
        }

        if (gameSeconds >= 2395 && gameSeconds < 4000) {  //  >=2395 <2400 for last 5 seconds before 40 min
            // countdown last 5 seconds till bed broken
            int seconds = 4000 - gameSeconds; //
            if (seconds > 1) {
                arena.sendMessage(ChatColor.GREEN + "" + seconds + " seconds until all beds broken!");
            } else {
                arena.sendMessage(ChatColor.GREEN + "1 second until all beds broken!");
            }
        }

        if (gameSeconds == 2400) {  //  2400 for 40 min
            // show red tile & break all beds.
            arena.sendRedTitle("All beds broken!");
            arena.breakAllBeds();
        }

        // GAME END TIME

        if (gameSeconds == 2880) {  // 48 mins will be 2880 seconds
            // countdown 2 mins till game ends

            arena.sendSubTitle("2 minutes until all game ends!");
        }

        if (gameSeconds == 2940) {  // 49 mins will be 2940 seconds
            // countdown 1 mins till game ends
            arena.sendSubTitle("1 minute until game ends!");
        }

        if (gameSeconds >= 2955 && gameSeconds < 3000) {  //  >=2955 <3000 for last 5 seconds before 50 min
            // countdown last 5 seconds till game ends
            int seconds = 3000 - gameSeconds; //
            if (seconds > 1) {
                arena.sendMessage(ChatColor.GREEN + "" + seconds + " seconds until game ends!");
            } else {
                arena.sendMessage(ChatColor.GREEN + "1 second until all game ends!");
            }
        }

        if (gameSeconds == 3000) {  //  3000 for 50 min
            // show red tile & put game to finishing phase.
            if (arena.getState() == GameState.LIVE) { // not if it is finishing already due to a win
                arena.sendRedTitle("It's a draw!");
                arena.gameDraw();
            }
        }

        // removes players from the spawn protect list who has been there 5 seconds or more
        playerSpawnProtect.entrySet().removeIf(e -> e.getValue() + 4 < gameSeconds );

        // if in the first 4 seconds of the game check everyone is above floor level
        if (this.gameSeconds < 5) {
            arena.checkPlayersAboveFloorLevel();
        }

        // loops through the playerSpecatorForRespawn list
        for (Map.Entry<UUID, Integer> entry : playerSpectatorForRespawn.entrySet()) {
            // the key is the players uuid
            UUID key = entry.getKey();
            // the value is the game seconds at which they were added to the list (died)
            Integer value = entry.getValue();
            if ((value + 4) < gameSeconds) {
                // if 5 or more seconds hanve passed since they wee spawned as spectator
                // add then to the spawn protect list
                playerSpawnProtect.put(key, gameSeconds);
                // get the players team instance so we can se where to spawn them
                Team diedTeam = arena.getTeam(key);
                // get the player
                Player player = Bukkit.getPlayer(key);
                if (player != null && diedTeam != null) {
                    //   set them back to survival and respawn then at their base
                    player.setGameMode(GameMode.SURVIVAL);
                    diedTeam.teleportPlayerToSpawn(player);
                }
            }
        }
        // this works with the same spectator list as the previous bit of code but removes players tah have been in it
        // more than 5 seconds.  I couldn't do this in the bit of code above as removing items from a list while you are
        // iterating over it can have unintended effects.
        playerSpectatorForRespawn.entrySet().removeIf(e -> e.getValue() + 4 < gameSeconds );

    }

    private void showPlayerParticles() {
        for (UUID uuid : playerSpawnProtect.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            particleTask = Bukkit.getScheduler().runTaskTimer(main, () -> {
                if (particleIterator != 0) {

                        Location location = player.getLocation();

                        PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(EnumParticle.DRIP_LAVA,
                                true, (float) location.getX(), (float) (location.getY()), (float) location.getZ(),
                                (float) 0.5, (float) 0.5, (float) 0.5, 0, 10);
                        for (Player online : Bukkit.getOnlinePlayers()) {
                            ((CraftPlayer) online).getHandle().playerConnection.sendPacket(packet);
                        }
                    particleIterator--;
                } else {
                    // If "i" is zero, we cancel the task.
                    particleTask.cancel();
                }
            }, 0, 1);
        }
    }

    // returns the game time as used in the scoreboard
    public String getGameTime() { return this.gameTime;}

    public void removeSpawnProtect(Player player) {
        if(playerSpawnProtect.containsKey(player.getUniqueId())){
            System.out.println("Player caused damage that cancelled their spawn protection");
            playerSpawnProtect.remove(player.getUniqueId());
        }
    }

}
