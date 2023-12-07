package com.stewart.bedwars.team;

import com.stewart.bedwars.Bedwars;
import com.stewart.bedwars.instance.Arena;
import com.stewart.bedwars.instance.Summoner;
import com.stewart.bedwars.instance.SummonerItem;
import com.stewart.bedwars.utils.ChatUtils;
import com.stewart.bedwars.utils.GameUtils;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.*;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftIronGolem;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftSilverfish;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

// team class handles all team related stuff.
// when the server (re)starts all the team instances are created as a list in the arena class
// players are added to teams when the game starts.
// empty teams are removed when the game starts.
public class Team {
    private int ID;
    private final String teamName;
    private final Bedwars main;
    private Location spawnLocation;
    // the team summoner class instance
    private List<Summoner> summoners;
    // the players in the team
    private  List<UUID> players;
    private String worldName;
    private Boolean hasBed;
    private Location bedLocation;
    // team color for chat prefix stored in config, I couldnt get this to work well so have
    // multiple ways of getting team color
    private String colourPrefix;
    private int teamColorInt;
    private String teamColorString;
    // for team speed upgrade
    private int teamSpeed;
    private boolean goldSummonerActive;
    private boolean diamondSummonerActive;
    private boolean finalSummonerActive;
    // scoreboard is set in per team as it needs to show your team bed status
  //  private Scoreboard scoreboard;
    private Arena arena;
    // stores how many kills each player in the team has for use in the scoreboard
    private HashMap<UUID, Integer> playerKills;
    // team golems
    private List<CraftIronGolem> teamGolems;
    private List<CraftSilverfish> teamSilverfish;

    private boolean doubleSummonerItems = false;

    // constructor takes main class, arenat the team is in, the team index (used to create the team from the config)
    // and the world the arena uses.
    public Team(Bedwars main, Arena arena, int index, String world, FileConfiguration config) {
        this.players = new ArrayList<>();
        this.playerKills = new HashMap<>();
        this.teamGolems = new ArrayList<>();
        this.teamSilverfish = new ArrayList<>();
        this.summoners  = new ArrayList<>();
        this.worldName = world;
        this.arena = arena;
        this.main = main;
        this.ID = index;
        this.hasBed = true;
        this.teamSpeed = 0;
        this.goldSummonerActive = false;
        this.diamondSummonerActive = false;
        this.finalSummonerActive = false;
        setTeamSummoners(config, index );
        // set the following variables from the config file
        teamName = config.getString("teams." + index + ".name");
        spawnLocation = setLocation(config, index, "spawn");
       // summoner = new Summoner(main, setShortLocation(config, index, "summoner"));
        bedLocation = setShortLocation(config, index, "bed");
        // different ways of showing team color
        this.colourPrefix = config.getString("teams." + index + ".color");
        this.teamColorInt  = config.getInt("teams." + index + ".color-short");
        this.teamColorString  = config.getString("teams." + index + ".enum");
        // initialise the team scoreboard
       // this.scoreboard =  Bukkit.getScoreboardManager().getNewScoreboard();
    }

    // get location of named item for team with passed index with yaw and pitch.
    // used in the above class constructor  to prevent duplicate code.
    private Location setLocation(FileConfiguration config, int index, String name) {
        double y = config.getDouble("teams." + index + "." + name + "-y") + 1;
        return  new Location(
                Bukkit.getWorld(worldName),
                config.getDouble("teams." + index + "." + name + "-x"),
                y,
                config.getDouble("teams." + index + "." + name + "-z"),
                (float) config.getDouble("teams." + index + "." + name + "-yaw"),
                (float) config.getDouble("teams." + index + "." + name + "-pitch"));
    }

    // get location with no yaw or pitch
    private Location setShortLocation(FileConfiguration config, int index, String name) {
        return  new Location(
                Bukkit.getWorld(worldName),
                config.getDouble("teams." + index + "." + name + "-x"),
                config.getDouble("teams." + index + "." + name + "-y"),
                config.getDouble("teams." + index + "." + name + "-z"));
    }

    private void setTeamSummoners(FileConfiguration config, int index){
        for (String str : config.getConfigurationSection("teams." + index + ".summoners").getKeys(false)) {
            // look at the team class to see how teams are made.
            String j = "summoners." + str + ".summoner";
            summoners.add(new Summoner(main, setShortLocation(config, index, j)));
        }
    }

    // teleport player to team span location
    private void teleportToSpawn(Player player) {
        player.teleport(spawnLocation);
    }

    private boolean isSpawnChunkLoaded() {
        World world = Bukkit.getWorld(worldName);
        Chunk chunk =	world.getChunkAt(spawnLocation);
        if (chunk.isLoaded()) {
            System.out.println("chunk is loaded");
            return true;
        } else {
            System.out.println("chunk not loaded, loading chunk");
            chunk.load();
            return false;
        }
    }

    /* Public functions can be called from other classes */

    // called from the arena class when the game clock ticks
    public void onClockTick(int gameSeconds) {
        // get all this teams summoner items
        for (Summoner summoner : summoners) {
            List<SummonerItem> teamSummonerItems = summoner.getSummonerItems();
            // loop trough them all and and fire their tick() function, that will find out if they need to drop or not
            for (SummonerItem teamSummonerItem : teamSummonerItems) {
                teamSummonerItem.onTick(gameSeconds, doubleSummonerItems);
            }
          /*  // if the team has players, update the game time on their scoreboard
            if (players.size() > 0) {
                scoreboard.getTeam("sbTime").setSuffix(ChatColor.BLUE + arena.getGame().getGameTime());
            }*/
            // check golem target
            for (CraftIronGolem golem : teamGolems) {
                checkGolemTarget(golem);
            }
            // check golem target
            for (CraftSilverfish silverfish : teamSilverfish) {
                checkSilverfishTarget(silverfish);
            }
        }
    }

    private void checkGolemTarget(CraftIronGolem golem) {
        if (golem.getTarget() == null || golem.getTarget() instanceof Player == false) {
           // System.out.println("golem has no target");
            Player player = arena.getClosestEnemyPlayer(this, golem);
            if (player != null) {
                golem.setTarget(player);
            }
        } else {
          //  System.out.println("golem has target");
        }
    }

    private void checkSilverfishTarget(CraftSilverfish silverfish) {
        if (silverfish.getTarget() == null || silverfish.getTarget() instanceof Player == false) {
         //   System.out.println("silverfish has no target");
            Player player = arena.getClosestEnemyPlayer(this, silverfish);
            if (player != null) {
                silverfish.setTarget(player);
            }
        } else {
          //  System.out.println("silverfish has target");
        }
    }

    public void removePlayerAsTarget(Player player) {
        for (CraftIronGolem golem : teamGolems) {
            if (golem.getTarget() == player) {
                golem.setTarget(null);
              //  System.out.println("golem untargeting player");
            }
        }
        // check golem target
        for (CraftSilverfish silverfish : teamSilverfish) {
            if (silverfish.getTarget() == player) {
                silverfish.setTarget(null);
               // System.out.println("silverfish untargeting player");
            }
        }
    }

    // fired when player upgrades summoner from shop or command is used
    public void startGoldSummoner() {
        if (goldSummonerActive == false) {
            System.out.println("Gold summoner started");
            // in this teams summoner instance, fire the start gold summoner function
            // pass it the diamond summoneractive boolean so it knows to set iron to speed 2 or 3
            for (Summoner summoner : summoners) {
                summoner.startTeamGoldSummoner(diamondSummonerActive);
            }
            goldSummonerActive = true;
        }
    }

    // fired when player upgrades summoner from shop or command is used
    // option is only available if they already have diamond and gold summoner
    public void startFinalSummoner() {
        if (finalSummonerActive == false) {
            System.out.println("Speed summoner started");
            // in this teams summoner instance, fire the start gold summoner function
            // pass it the diamond summoneractive boolean so it knows to set iron to speed 2 or 3
            for (Summoner summoner : summoners) {
                summoner.startTeamFinalSummoner();
            }
            finalSummonerActive = true;
        }
    }

    // same as above but for the team diamond summoner
    public void startDiamondSummoner() {
        if (diamondSummonerActive == false) {
            System.out.println("diamond summoner started");
            for (Summoner summoner : summoners) {
                summoner.startTeamDiamondSummoner(goldSummonerActive);
            }
            diamondSummonerActive = true;
        }
    }

    // returns if this teams gold/diamond summomner is active.
    public boolean isGoldSummonerActive() {return this.goldSummonerActive;}
    public boolean isDiamondSummonerActive() {return this.diamondSummonerActive;}
    public boolean isFinalSummonerActive() {return this.finalSummonerActive;}

    // returns the team name
    public String getTeamName() {return teamName;}

    // send a message to all players in this team
    public void sendMessage(String message) {
        for (UUID uuid : players) {
            Bukkit.getPlayer(uuid).sendMessage(ChatUtils.arenaChatPrefix  + message);
        }
    }

    // checks if the player is in this team.  Used when adding players to
    // teams to make sure they are not already in a team
    public boolean playerInTeam(UUID toFind) {
        for (UUID uuid : players) {
            if (uuid.equals(toFind)) {
                return true;
            }
        }
        return  false;
    }

    // checks if the golem is in this team.
    public boolean golemInTeam(CraftIronGolem golem) {
        for (CraftIronGolem golem1 : teamGolems) {
            if (golem1.equals(golem)) {
                return true;
            }
        }
        return  false;
    }

    // checks if the golem is in this team.
    public boolean silverFishInTeam(CraftSilverfish silverfish) {
        for (CraftSilverfish silverfish1 : teamSilverfish) {
            if (silverfish1.equals(silverfish)) {
                return true;
            }
        }
        return  false;
    }

    // return the number of players in this team
    public int getNumPlayers() { return players.size();}

    // add the passed player to this team
    // also sets their display name color but I'm not sure if this works.
    public  void addPlayer(UUID uuid) {
        players.add(uuid);
        Player player = Bukkit.getPlayer(uuid);
        String n = player.getDisplayName();
        playerKills.put(uuid, 0);
      //  player.setDisplayName(colourPrefix + n);
        player.setDisplayName(GameUtils.getChatColorFromString(this.teamColorString) + n);
        player.setPlayerListName(GameUtils.getChatColorFromString(this.teamColorString) + n);

    }

    // credit a kill to the passed player
    public void addKill(UUID uuid) {
        // get their current score from the player kills hashmap and plus it by one
        Integer score = playerKills.get(uuid) + 1;
        playerKills.put(uuid, score);
        // get th eplayer and update th ekills value n his scoreboard.
        Player player = Bukkit.getPlayer(uuid);
        player.getScoreboard().getTeam("sbKills").setSuffix(String.valueOf(score));
    }

    // remove a player from a team.  Happens when they die without a bed to respawn in.
    // retunrs true false to indicate if the team is now empty or not.
    public void removePlayer(UUID uuid) {
        int cntBefore = players.size();
        players.remove(uuid);
        int cntAfter = players.size();
        if (cntBefore == 1 && cntAfter == 0) {
            // removing this player emptied this team
        }
    }

    // remove a player from a team.  Happens when they die without a bed to respawn in.
    // retunrs true false to indicate if the team is now empty or not.
    public  boolean removeGolem(CraftIronGolem golem) {
        return teamGolems.remove(golem);
    }
    public  boolean removeSilverfish(CraftSilverfish silverfish) {
        return teamSilverfish.remove(silverfish);
    }

    // teleport all team players to their spawn location, called when the arena/game starts
    public void teleportPlayersToSpawn() {
        if (GameUtils.isChunkLoaded(worldName, spawnLocation)) {
            for (UUID uuid : players) {
                Player player = Bukkit.getPlayer(uuid);
                player.setFallDistance(0F);
                teleportToSpawn(player);
            }
        } else {
            new BukkitRunnable() {
                @Override
                public void run() {
                    teleportPlayersToSpawn();
                }
            }.runTaskLater(main, 1);
        }
    }

    public void setScoreboardTeamName() {
            for (UUID uuid:players) {
                Player player = Bukkit.getPlayer(uuid);
            player.getScoreboard().getTeam("sbTeamName").setPrefix(ChatColor.GREEN + "Team: ");
            player.getScoreboard().getTeam("sbTeamName").setSuffix(getTeamChatColor() + teamName);
        }
    }

    // Creates the team scoreboard
  /*  public void setScoreboard() {
        if (players.size() > 0) {
            // create the scoreboard - see udemy tutorial for more details
            Objective obj = scoreboard.registerNewObjective("bwboard", "dummy");
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);
            obj.setDisplayName(ChatColor.BOLD.toString() + ChatColor.GOLD + "BedWars");

             // this is where the game time is shown and will be updated every second.
            org.bukkit.scoreboard.Team sbTime = scoreboard.registerNewTeam("sbTime");
            sbTime.addEntry(ChatColor.BOLD.toString());
            sbTime.setPrefix(" ");
            sbTime.setSuffix(" ");
            obj.getScore(ChatColor.BOLD.toString()).setScore(14);

            // empty line
            Score line13 = obj.getScore(" ");
            line13.setScore(13);

            Score line12= obj.getScore(ChatColor.BOLD.toString() + ChatColor.YELLOW + "Teams remaining");
            line12.setScore(12);

            // check out the scoreBoardTeams() function in arena class for comments on how the next 2 bits work.
            org.bukkit.scoreboard.Team sbTeams1 = scoreboard.registerNewTeam("sbTeams1");
            sbTeams1.addEntry(ChatColor.GOLD.toString());
            sbTeams1.setPrefix(arena.scoreBoardTeams(0));
            sbTeams1.setSuffix(arena.scoreBoardTeams(4));
            obj.getScore(ChatColor.GOLD.toString()).setScore(11);

            org.bukkit.scoreboard.Team sbTeams2 = scoreboard.registerNewTeam("sbTeams2");
            sbTeams2.addEntry(ChatColor.GRAY.toString());
            sbTeams2.setPrefix(arena.scoreBoardTeams(8));
            sbTeams2.setSuffix(arena.scoreBoardTeams(12));
            obj.getScore(ChatColor.GRAY.toString()).setScore(10);

            Score line9 = obj.getScore("  ");
            line9.setScore(9);

            Score line8= obj.getScore(ChatColor.BOLD.toString() + ChatColor.YELLOW + "Your team");
            line8.setScore(8);

            // show your team name
            Score line7= obj.getScore("Colour: " + getTeamChatColor() + teamName);
            line7.setScore(7);

            // show your bed status, should be updated on bed destroyed
            org.bukkit.scoreboard.Team sbTeamBed = scoreboard.registerNewTeam("sbTeamsBed");
            sbTeamBed.addEntry(ChatColor.GREEN.toString());
            sbTeamBed.setPrefix("Bed: ");
            sbTeamBed.setSuffix(getBedStatus());
            obj.getScore(ChatColor.GREEN.toString()).setScore(6);

            Score line5 = obj.getScore("   ");
            line5.setScore(5);

            Score line4= obj.getScore(ChatColor.BOLD.toString() + ChatColor.YELLOW + "Your stats");
            line4.setScore(4);

            // show player kills, set to 0 initially but updated per player from another function when they do a kill
            org.bukkit.scoreboard.Team sbTeamKills = scoreboard.registerNewTeam("sbTeamsKills");
            sbTeamKills.addEntry(ChatColor.BLUE.toString());
            sbTeamKills.setPrefix("Kills: ");
            sbTeamKills.setSuffix("0");
            obj.getScore(ChatColor.BLUE.toString()).setScore(2);

            for (UUID uuid : players) {
                Bukkit.getPlayer(uuid).setScoreboard(scoreboard);
            }
        }
    }*/

    // update all players scoreboards when a team is eliminated or a bed is destroyed.
    public void updateScoreBoard() {
        for (UUID uuid : players) {
            Player player = Bukkit.getPlayer(uuid);
            // team part handled in arean as not team specific
          //  player.getScoreboard().getTeam("sbTeams1").setPrefix(arena.scoreBoardTeams(0));
          //  player.getScoreboard().getTeam("sbTeams1").setSuffix(arena.scoreBoardTeams(4));
           // player.getScoreboard().getTeam("sbTeams2").setPrefix(arena.scoreBoardTeams(8));
           // player.getScoreboard().getTeam("sbTeams2").setSuffix(arena.scoreBoardTeams(12));
            player.getScoreboard().getTeam("sbBedStatus").setSuffix(getBedStatus());
        }
    }

    // set this teams bed variable to broken.
    public void setBedBroken() {
        this.hasBed = false;
        sendMessage(ChatColor.RED + "Your bed has been broken!");
        main.getArenaManager().getFirstArena().sendMessage(teamName + " team bed broken!");
    }

    // teleport passed player to team spawn
    public void teleportPlayerToSpawn(Player player) {
        // if team has speed give it to the player
        if (this.getTeamSpeed() == 1) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200000, 0));
        }
        if (this.getTeamSpeed() == 2) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200000, 1));
        }
        if (GameUtils.isChunkLoaded(worldName, spawnLocation)) {
            teleportToSpawn(player);
        } else {
            new BukkitRunnable() {
                @Override
                public void run() {
                    teleportPlayersToSpawn();
                }
            }.runTaskLater(main, 1);
        }
    }

    // return if team has bed or not
    public boolean hasBed() {return hasBed;}

    // return if either of the passed locations is this teams stored bed location
    public boolean isTeamBedLocation(Location location1, Location location2) {
        if  (location1.equals(bedLocation) || location2.equals(bedLocation)) {
            return true;
        }
        return false;
    }

    // show win foreworks for all players in this team
    public void showWinFireWorks() {
        for(UUID uuid : players) {
            // win fireworks is in the GameUtils class
            GameUtils.winFireworks(Bukkit.getPlayer(uuid), main);
        }
    }

    public void logWinInApi() {
        for (UUID uuid:players) {
            main.getBb_api().getPlayerManager().increaseValueByOne(uuid, "bw_wins");
        }
    }

    // get the team color as an int as used to dye wool etx
    public int getTeamColorInt() {return this.teamColorInt;}

    // gets the team color as a Color  as returned from a function in the Gameutils class, used for dyeing their armour
    public Color getTeamColor() {return GameUtils.getColorFromString(this.teamColorString);}
    // gets the team color as a chatColor  as returned from a function in the Gameutils class,
    public ChatColor getTeamChatColor() {return GameUtils.getChatColorFromString(this.teamColorString);}

    // apply first team speed upgrade
    public void applySpeed1() {
        for(UUID uuid : players) {
            Bukkit.getPlayer(uuid).addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200000, 0));
        }
        this.teamSpeed = 1;
        sendMessage(ChatColor.GOLD + " This team now has speed-1!");
    }

    // apply second team speed upgrade
    public void applySpeed2() {
        for(UUID uuid : players) {
            Bukkit.getPlayer(uuid).removePotionEffect(PotionEffectType.SPEED);
            Bukkit.getPlayer(uuid).addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200000, 1));
        }
        this.teamSpeed = 2;
        sendMessage(ChatColor.GOLD + " This team now has speed-2!");
    }

    // get current team speed
    public int getTeamSpeed() {return this.teamSpeed;}

    // returns this teams summoner instance
    public void setSummonerQuarterSpeed() {
        for (Summoner summoner :summoners) {
            summoner.setQuarterSpeed();
        }
    }

    // get the teams bed status as a string to show in the scoreboards 'bed-status' section
    public String getBedStatus() {
        if (hasBed) {
            return ChatColor.GREEN + "Alive";
        } else {
            return ChatColor.RED + "Destroyed";
        }
    }

    // sets both the team bed blocks to air.
    // called when removing empty teams at the start of the game
    public void removeBed() {
        // I only store one of the bed block locations but have a util that finds the other one
        Block bedBlock1 = bedLocation.getBlock();
        Block bedBlock2 = GameUtils.getOtherBedBlock(bedBlock1);
        System.out.println("Removing team bed");
        bedBlock1.setType(Material.AIR, false);
        bedBlock2.setType(Material.AIR, false);
        this.hasBed = false;
    }

    public String getColourPrefix() { return this.colourPrefix;}

    public void spawnTeamGolem(Location location, String playerName) {
        final CraftIronGolem craftGolem = (CraftIronGolem) Bukkit.getWorld("world").spawnEntity(location, EntityType.IRON_GOLEM);
        final EntityIronGolem golem = craftGolem.getHandle();

        List goalBz = (List)getPrivateField("b", PathfinderGoalSelector.class, golem.goalSelector); goalBz.clear();
        List goalCz = (List)getPrivateField("c", PathfinderGoalSelector.class, golem.goalSelector); goalCz.clear();
        List targetBz = (List)getPrivateField("b", PathfinderGoalSelector.class, golem.targetSelector); targetBz.clear();
        List targetCz = (List)getPrivateField("c", PathfinderGoalSelector.class, golem.targetSelector); targetCz.clear();

        // swim up if in water
        golem.goalSelector.a(0, new PathfinderGoalFloat(golem));
        //   primary goal is to melee attack player target
        golem.goalSelector.a(1, new PathfinderGoalMeleeAttack(golem, EntityHuman.class, 1.0D, false));
        // secondary goal random stroll
        golem.goalSelector.a(4, new PathfinderGoalRandomStroll(golem, 1.0F));

        // primary target is one that attacks golem (will check for team in listener)
        golem.targetSelector.a(0, new PathfinderGoalHurtByTarget(golem, true, new Class[0]));
        // secondary target is the closest attack-able player target (cancel if same team in listener)
    //    golem.targetSelector.a(1, new PathfinderGoalNearestAttackableTarget(golem, EntityHuman.class,  true));


        golem.setCustomNameVisible(true);
        golem.setCustomName(playerName + "'s golem");

        teamGolems.add(craftGolem);
    }

    public void setDoubleSummonerItems(boolean doubleSummonerItems) {this.doubleSummonerItems = doubleSummonerItems;}

    public void spawnTeamSilverfish(Location location, String playerName) {
        final CraftSilverfish craftSilverfish = (CraftSilverfish) Bukkit.getWorld("world").spawnEntity(location, EntityType.SILVERFISH);
        final EntitySilverfish silverfish = craftSilverfish.getHandle();

        List goalBz = (List)getPrivateField("b", PathfinderGoalSelector.class, silverfish.goalSelector); goalBz.clear();
        List goalCz = (List)getPrivateField("c", PathfinderGoalSelector.class, silverfish.goalSelector); goalCz.clear();
        List targetBz = (List)getPrivateField("b", PathfinderGoalSelector.class, silverfish.targetSelector); targetBz.clear();
        List targetCz = (List)getPrivateField("c", PathfinderGoalSelector.class, silverfish.targetSelector); targetCz.clear();


        // swim up if in water
        silverfish.goalSelector.a(0, new PathfinderGoalFloat(silverfish));
        //   primary goal is to melee attack player target
        silverfish.goalSelector.a(1, new PathfinderGoalMeleeAttack(silverfish, EntityHuman.class, 1.0D, false));
        // secondary goal random stroll
        silverfish.goalSelector.a(4, new PathfinderGoalRandomStroll(silverfish, 1.0F));

        // primary target is one that attacks golem (will check for team in listener)
        silverfish.targetSelector.a(0, new PathfinderGoalHurtByTarget(silverfish, true, new Class[0]));
        // secondary target is the closest attack-able player target (cancel if same team in listener)
      //  silverfish.targetSelector.a(1, new PathfinderGoalNearestAttackableTarget(silverfish, EntityHuman.class,  true));

        silverfish.setCustomNameVisible(true);
        silverfish.setCustomName(playerName + "'s silverfish");


        teamSilverfish.add(craftSilverfish);
    }

    public static Object getPrivateField(String fieldName, Class clazz, Object object)
    {
        Field field;
        Object o = null;

        try
        {
            field = clazz.getDeclaredField(fieldName);

            field.setAccessible(true);

            o = field.get(object);
        }
        catch(NoSuchFieldException e)
        {
            e.printStackTrace();
        }
        catch(IllegalAccessException e)
        {
            e.printStackTrace();
        }

        return o;
    }

}
