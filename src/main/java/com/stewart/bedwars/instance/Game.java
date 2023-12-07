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
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// the game class handles much the same type of stuff as arena, i could probably have combined the two.
public class Game {

    private Bedwars main;
    private Arena arena;
    private Integer bedBreakSeconds = 2400;  // 2280 = 38 min
    private Integer gameEndSeconds = 2700;   // 3000 = 45 min
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
            arena.addHotbarNetherStar(Bukkit.getPlayer(uuid));

          arena.addWoodenSword(Bukkit.getPlayer(uuid));
        //    player.getInventory().addItem(new ItemStack(Material.EMERALD, 64));
         //   player.getInventory().addItem(new ItemStack(Material.DIAMOND, 64));
         //  player.getInventory().addItem(new ItemStack(Material.FIREBALL, 64));
          //  player.getInventory().addItem(new ItemStack(Material.TNT, 64));
         //   player.getInventory().addItem(new ItemStack(Material.WOOL, 64));
         //   player.getInventory().addItem(new ItemStack(Material.IRON_INGOT, 64));
         //   player.getInventory().addItem(new ItemStack(Material.GOLD_INGOT, 64));
           // ItemStack eggs = new ItemStack(Material.EGG, 20);
         //   ItemMeta eggsMeta  = eggs.getItemMeta();
          //  eggsMeta.setDisplayName("Golem spawn egg");
         //   eggs.setItemMeta(eggsMeta);
          //  player.getInventory().addItem(eggs);

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
        // I need to get the killers team from the arena class
        Team diedTeam = arena.getTeam(died.getUniqueId());
        ChatColor diedColour = ChatColor.RED;
        // could be in the lobby with no teams
        if (diedTeam != null) {
            diedColour = diedTeam.getTeamChatColor();
        }
        if (killer == null) {
            // no killer, just send a message that the player died.  The player damage event can tell you (as an enum)
            // how the damage was taken (void, fire, projectile etc) so in the future we could show a better message here.
            arena.sendMessage(diedColour + died.getName() + " died!");
        } else {

            // each players number of kills is stored in their team instance so to log the kill
            Team killerTeam = arena.getTeam(killer.getUniqueId());
            ChatColor killerColour = ChatColor.RED;
            // could be in the lobby with no teams
            if (killerTeam != null) {
                killerColour= killerTeam.getTeamChatColor();
                // call the addkill function in the players team instance (logs it to the player, not the team)
                killerTeam.addKill(killer.getUniqueId());
            }
            // player was killed by another player, show a message to the arena with killed and killer
            arena.sendMessage(killerTeam.getTeamChatColor() + killer.getName() + " killed " + killerColour + died.getName());

        }
        // stop them taking fall damage if teleported after falling
        died.setFallDistance(0F);

        // Need to remove any detrimental efects the player may have been under
        // but if they had team speed, put that back
        for (PotionEffect effect : died.getActivePotionEffects()) {
            died.removePotionEffect(effect.getType());
        }

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
            // log death and kill in the api
            main.getBb_api().getPlayerManager().increaseValueByOne(died.getUniqueId(), "bw_deaths");
            if (killer != null) {
                main.getBb_api().getPlayerManager().increaseValueByOne(killer.getUniqueId(), "bw_kills");
            }
            // check if died player has team bed, first get the players team instance
            GameUtils.dropInventory(died);
            arena.addHotbarNetherStar(died);
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
                    System.out.println("Player died, making spectator & teleporting to spectator");
                    died.teleport(arena.getSpectatorSpawn());
                    // add them to the playerSpectatorForRespawn list, this is checked every second to see if it is
                    // time spawn them back at their base
                    playerSpectatorForRespawn.put(died.getUniqueId(), gameSeconds);
                } else {
                    // No bed spawn them as a spectator
                   // died.setGameMode(GameMode.SPECTATOR);
                    GameUtils.makePlayerInvisibleGameEnd(died);
                    arena.addOutOfGamePlayer(died);
                    // remove them from their team
                    diedTeam.removePlayer(died.getUniqueId());
                    died.teleport(arena.getSpectatorSpawn());
                    // leaving scoreboard active for players out of the game
                    // this should remove the scoreboard
                 //   died.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
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

        if (!playerSpawnProtect.isEmpty()) {
            // remove any players from spawn protect list that may have left the game
            playerSpawnProtect.entrySet().removeIf(e->  Bukkit.getPlayer(e.getKey()) == null );
            // show particles around spawn protected players
            particleIterator = 19;
            particleTask = null;
            showPlayerParticles();
        }

        // NEW PLAYER MESSAGES
        if(gameSeconds == 5) {
            // sent new player first message
            arena.sendMessageNewPlayers(arena.getGameInfo().getNewPlayerMessage());
            arena.sendMessagePityPlayers(arena.getGameInfo().getPityMessage());
        }
        if(gameSeconds == 10) {
            // sent new player first message
            arena.sendMessageNewPlayers("Protect your bed!  Your bed lets you respawn.  Destroy other beds!");
        }
        if(gameSeconds == 15) {
            // sent new player first message
            arena.sendMessageNewPlayers("3 iron buys wool, use wool to bridge to other islands.");
        }
        if(gameSeconds == 20) {
            // sent new player first message
            arena.sendMessageNewPlayers("Right-click on the shopkeeper to buy weapons, materials and tools.");
        }
        if(gameSeconds == 25) {
            // sent new player first message
            arena.sendMessageNewPlayers("Diamonds and ems can be used for upgrades! Find ems on the middle island.");
        }
        if(gameSeconds == 120) {
            // sent new player first message
            arena.sendMessageNewPlayers("Get your weapons enchanted by the Enchantress at the middle island!");
        }
        if(gameSeconds == 125) {
            // sent new player first message
            arena.sendMessageNewPlayers("Buy special items from the Armourer at the middle island!");
        }

        // LOG GAME played after 1 minute
        if (gameSeconds == 60) {
            arena.logGamePlayed();
        }

        // BEDS DESTROYED
        if (gameSeconds == (bedBreakSeconds - 120)) {  // 2 mins before bed break
            // countdown 2 mins till all beds broken
            arena.sendTitleSubtitle("2 minutes","until all beds broken!", null, "4");
            arena.sendMessage(ChatColor.RED + "2 minutes until all beds broken!");
        }

        if (gameSeconds == (bedBreakSeconds - 60 )) {  // 1 min before bed break
            // countdown 1 mins till all beds broken
            arena.sendTitleSubtitle("1 minute","until all beds broken!", null, "4");
        }

        // last 30 seconds every 10 seconds
        if (gameSeconds == (bedBreakSeconds - 30) || gameSeconds == (bedBreakSeconds - 20) || gameSeconds == (bedBreakSeconds - 10)) {
            int seconds = bedBreakSeconds - gameSeconds; //
            arena.sendMessage(ChatColor.GOLD + "" + seconds + " seconds until all beds broken!");
            arena.sendTitleSubtitle(seconds + " seconds","until all beds broken", null, "4");
        }

        if (gameSeconds >= (bedBreakSeconds - 5) && gameSeconds < bedBreakSeconds) {  // last 5 secs before bed break time
            // countdown last 5 seconds till bed broken
            int seconds = bedBreakSeconds - gameSeconds; //
            if (seconds > 1) {
                arena.sendMessage(ChatColor.GOLD + "" + seconds + " seconds until all beds broken!");
            } else {
                arena.sendMessage(ChatColor.GOLD + "1 second until all beds broken!");
            }
            arena.sendTitleSubtitle(String.valueOf(seconds),"", null, String.valueOf(seconds));
        }

        if (gameSeconds == bedBreakSeconds) {  //  bed break time
            // show red tile & break all beds.
            arena.sendTitleSubtitle("All beds broken!", "", null, null);
            arena.breakAllBeds();
            arena.sendMessage(ChatColor.RED + "All beds broken!");
        }

        // GAME END TIME

        if (gameSeconds == (gameEndSeconds - 120)) {  // 2 mins before game ends
            // countdown 2 mins till game ends
            arena.sendTitleSubtitle("2 minutes","until game ends!", null, "4");
            arena.sendMessage(ChatColor.RED + "2 minutes until game ends!");
        }

        if (gameSeconds == (gameEndSeconds - 60)) {  // 1 min before game ends
            // countdown 1 mins till game ends
            arena.sendTitleSubtitle("1 minute", "until game ends!", null, "4");
            arena.sendMessage(ChatColor.RED + "1 minute until game ends!");
        }

        // last 30 seconds every 10 seconds
        if (gameSeconds == (gameEndSeconds - 30) || gameSeconds == (gameEndSeconds - 20) || gameSeconds == (gameEndSeconds - 10)) {
            int seconds = gameEndSeconds - gameSeconds; //
            arena.sendMessage(ChatColor.GOLD + "" + seconds + " seconds until game ends!");
            arena.sendTitleSubtitle(seconds + " seconds","until game ends!", null, "4");
        }

        if (gameSeconds >= (gameEndSeconds - 5) && gameSeconds < gameEndSeconds) {  //  last 5 game seconds
            // countdown last 5 seconds till game ends
            int seconds = gameEndSeconds - gameSeconds; //
            if (seconds > 1) {
                arena.sendMessage(ChatColor.GOLD + "" + seconds + " seconds until game ends!");
            } else {
                arena.sendMessage(ChatColor.GOLD + "1 second until game ends!");
            }
            arena.sendTitleSubtitle(String.valueOf(seconds), "", null, null);
        }

        if (gameSeconds == gameEndSeconds) {  //  game end time
            // show red tile & put game to finishing phase.
            if (arena.getState() == GameState.LIVE) { // not if it is finishing already due to a win
                arena.sendTitleSubtitle("It's a draw!", "", "6", null);
                arena.sendMessage(ChatColor.RED + "It's a draw!");
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
                    arena.addWoodenSword(player);
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

                    if (player != null) {
                        Location location = player.getLocation();

                        PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(EnumParticle.DRIP_LAVA,
                                true, (float) location.getX(), (float) (location.getY()), (float) location.getZ(),
                                (float) 0.5, (float) 0.5, (float) 0.5, 0, 10);
                        for (Player online : Bukkit.getOnlinePlayers()) {
                            ((CraftPlayer) online).getHandle().playerConnection.sendPacket(packet);
                        }
                        particleIterator--;
                    } else {
                        System.out.println("player is null, cancelling particles");
                        particleIterator = 0;
                        if (particleTask != null) {
                            particleTask.cancel();
                        }
                    }
                } else {
                    // If "i" is zero, we cancel the task.
                    if (particleTask != null) {
                        particleTask.cancel();
                    }
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
