package com.stewart.bedwars.instance;

import com.gmail.tracebachi.SockExchange.Spigot.SockExchangeApi;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.stewart.bedwars.Bedwars;
import com.stewart.bedwars.GameState;
import com.stewart.bedwars.manager.ConfigManager;
import com.stewart.bedwars.manager.ShopManager;
import com.stewart.bedwars.team.Team;
import com.stewart.bedwars.utils.BorderCheck;
import com.stewart.bedwars.utils.ChatUtils;
import com.stewart.bedwars.utils.GameUtils;
import com.sun.tools.javac.code.Attribute;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.*;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftIronGolem;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftSilverfish;
import org.bukkit.entity.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.io.File;
import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;

public class Arena {

    private Bedwars main;
    private int ID;

    private Location spawn;
    private Location spectatorSpawn;
    private FileConfiguration config;

    private GameState state;
    private List<UUID> players;
    private Countdown countdown;
    private GameClock gameClock;
    private List<Team> teams = new ArrayList<>();
    private HashMap<UUID, UUID> playerDamage = new HashMap<>();
    private HashMap<UUID, Integer> mapVotes = new HashMap<>();
    private DiaEmeSummoner diaEmeSummoners;
    private ShopManager shopManager;
    private String worldName;
    private Game game;
    private int maxPlayers;
    private World world;
    private List<Block> playerBlocks = new ArrayList<>();

    private BorderCheck borderCheck;


    private int floorY;

    // Most of the game code is split between this class and the 'Game' class.  I could have
    // just had it all in this one just as easily.
    // this is created once when the server starts not at the beginning of each game.
    public Arena(Bedwars main, String worldName, String ID) {


        // this arenas ID, will only be one for this game but may be more in others.
        this.ID = Integer.parseInt(ID);
        // the world name, I'm not using multiple arenas at the moment but having this will help when if I do.
        this.worldName = worldName;

        // when the arena is created gamestate will be recruiting
        this.state = GameState.RECRUITING;
        // will hold all the players currently on the server, there is a separate player list
        // included in each team instance
        this.players = new ArrayList<>();
        // the countdown for the lobby
        this.countdown = new Countdown(main, this);
        // the game class holds game related stuff, just the player killed and spawn protection stuff
        this.game = new Game(main,this);
        // Store a reference to the plugins main class so we can reference it in this one and pass it down
        // to subclasses of arena
        this.main = main;
        // read the gameID and server ID from config, this is used for updating the hub server (sign) with information
        // about the servers game state & number of players.
        this.config = main.getConfig();
      //  this.gameID = config.getInt("game-id");
       // this.serverID = config.getInt("portal-id");
        this.maxPlayers = config.getInt("max-players");
        // the spawn location for players entering the lobby
        world = new WorldCreator(worldName).createWorld();
        spawn = new Location(
                world,
                config.getDouble("lobby-spawn.x"),
                config.getDouble("lobby-spawn.y"),
                config.getDouble("lobby-spawn.z"),
                (float) config.getDouble("lobby-spawn.yaw"),
                (float) config.getDouble("lobby-spawn.pitch"));
    }

   /* private void fillMaps() {
        FileConfiguration config = main.getConfig();
        for (String str : config.getConfigurationSection("bedwars-maps").getKeys(false)) {
            mapVotes.put(Integer.parseInt(str), 0);
        }
    } */

    public void mapVote(Player player, Integer mapID) {
        // check if the map id exists
        if (config.contains("bedwars-maps." + mapID + ".name")) {
            mapVotes.put(player.getUniqueId(), mapID);
            String m = config.getString("bedwars-maps." + mapID + ".name");
            player.sendMessage("You voted for the " + m + " map!");
        } else {
            System.out.println("map vote map not found");
        }
        for (Map.Entry<UUID, Integer> entry : mapVotes.entrySet()) {
            UUID key = entry.getKey();
            Integer value = entry.getValue();
            System.out.println("UUID " + key.toString() + " id " + value);
        }

    }

    // fired to start the game, either when countdown completes or the command is entered.
    public void start() {
        System.out.println("Game starting");
        // clears up any dropped items left in the map
        removeEntityItems();
        // this will be decided by voting at this point eventually
        String configName = getArenaConfigName();
        File file = new File(main.getDataFolder(), configName);
        YamlConfiguration mapConfig =  YamlConfiguration.loadConfiguration(file);
        setWorldTeamsSummoners(mapConfig);
        // add players to teams
        addPlayersToTeams();
        // get rid of any empty teams
      //  removeEmptyTeams();
        // teleports each teams players to their spawn point
        for (Team team : teams) {
            team.teleportPlayersToSpawn();
        }

        // spawn the villagers etc.  Probably had a good reason for doing it 1/2 second after the game starts
        // but can't remember it
        new BukkitRunnable() {
            @Override
            public void run() {
                setVillagers(mapConfig);
            }
        }.runTaskLater(main, 10);
        // clear the player damage hashmap * not needed now I do a server restart but cant hurt.
        playerDamage = new HashMap<>();
        // clear the player block list * not needed now I do a server restart but cant hurt.
        playerBlocks = new ArrayList<>();
        // stop the countdown, if it had stopped naturally it would have been stopped anyway but if
        // game was started by command it will still be running & recall arena.start when it completes
        if (countdown.isRunning()) {
            System.out.println("countdown was running, cancelling it");
            countdown.cancel();
        }
        // start the clock
        // the game clock will be started when the game starts and tick once per second
        // it is used to start & upgrade the diamond & emerald summoners at the right time
        // and will show the game clock in the ui.
        this.gameClock = new GameClock(main, this);
        gameClock.start();
        // starts the game class, (sets the game start to live)
        game.start();
        // scoreboards are mostly handled per team.  They not working fully at this point but this is
        // where they are added (in the team class)
        for (Team team : teams) {
            team.setScoreboard();
        }
        sendTitleSubtitle("Fight!", "", null, null);
        // update the sign in the hub server
        updateLobby();


    }

    private String getArenaConfigName() {
        if (mapVotes.size() == 0) {
            // random map
            return getRandomMap();
        } else {
            // put all the voted for ids in an array
            int a[] = new int[mapVotes.size()];
            int j = 0;
            for (Integer value : mapVotes.values()) {
                a[j] = value;
                j += 1;
            }
            Arrays.sort(a);

            // following code is copied from SO and gets the most popular id in the array.
            // todo look at what happens if maps get simialr votes, is it the same one that always gets picked in this case? that would be bad.
            int previous = a[0];
            int popular = a[0];
            int count = 1;
            int maxCount = 1;

            for (int i = 1; i < a.length; i++) {
                if (a[i] == previous)
                    count++;
                else {
                    if (count > maxCount) {
                        popular = a[i-1];
                        maxCount = count;
                    }
                    previous = a[i];
                    count = 1;
                }
            }

            int mapID = count > maxCount ? a[a.length-1] : popular;

            // if mapID exists in the config use it
            if (config.contains("bedwars-maps." + mapID + ".name")) {
                return config.getString("bedwars-maps." + mapID + ".config-name");
            } else {
                System.out.println("Most popular map not found mapID " + mapID);
                // random map
                return getRandomMap();
            }
        }
    }

    private String getRandomMap() {
        System.out.println("Picking a random map");
        ArrayList<String> arr = new ArrayList<>();
        for (String str : config.getConfigurationSection("bedwars-maps").getKeys(false)) {
            arr.add(config.getString("bedwars-maps." + str + ".config-name"));
            System.out.println("hey");
        }
        String picked = arr.get(new Random().nextInt(arr.size()));
        System.out.println("Picked =  " + picked);
        return picked;
    }


    // this sets the teams, diamond summoners, lobby and spectator spawn points.
    // When I was unloading and reloading the 'world' at the end of each game
    // this function had to be called on each reset as the 'world' variable used
    // in the previous game was no longer valid.
    // Now I just restart the server it's not a problem but keeping it this way works fine.
    private void setWorldTeamsSummoners(FileConfiguration mapConfig) {

        // set the spectator spawn location.
        spectatorSpawn = new Location(
                world,
                mapConfig.getDouble("spectator-spawn.x"),
                mapConfig.getDouble("spectator-spawn.y"),
                mapConfig.getDouble("spectator-spawn.z"),
                (float) mapConfig.getDouble("spectator-spawn.yaw"),
                (float) mapConfig.getDouble("spectator-spawn.pitch"));

        floorY = mapConfig.getInt("feet-block-y");

        // turn off auto save for the world
        world.setAutoSave(false);
        // set the time
        Long t = mapConfig.getLong("time-ticks");
        world.setTime(t);
        // this creates an instance of each team.  Players will be added to teams when the lobby countdown
        // finishes, just as the game starts.  Later on, when parties join the server that wil be handled too.
        setTeams(mapConfig);
        // stores an instance of the diamond and emerald summoner class in this page.
        // this will be called at the appropriate times to start & upgrade those summoners.
        diaEmeSummoners = null;
        diaEmeSummoners = new DiaEmeSummoner(main, this, worldName, mapConfig);
        // set the map bounding co-ordinates
        Vector vector1 =   new Vector(
                mapConfig.getDouble("bounding-corner1.x"),
                mapConfig.getDouble("bounding-corner1.y"),
                mapConfig.getDouble("bounding-corner1.z"));

        Vector vector2 =   new Vector(
                mapConfig.getDouble("bounding-corner2.x"),
                mapConfig.getDouble("bounding-corner2.y"),
                mapConfig.getDouble("bounding-corner2.z"));

        borderCheck = new BorderCheck(vector1, vector2);
    }

    // this runs when the game starts & spawns all the shops guys (not just the villagers)
    private void setVillagers(FileConfiguration mapConfig) {
        if (shopManager != null) {
            // reset it if it does exist
            shopManager.removeAll();
            shopManager = null;
        }
        shopManager = new ShopManager(this, main, mapConfig);
        shopManager.spawnAll();
    }

    // remove all item entities left in the map. (titties heh)
    // not really required now that we do server restart but cant hurt.
    private void removeEntityItems() {
        List<Entity> entList = Bukkit.getWorld(worldName).getEntities();
        for(Entity current : entList){
            if (current instanceof Item || current instanceof Silverfish || current instanceof IronGolem){
                Item item = (Item) current;
                current.remove();
            }
        }
        if (shopManager != null) {
            shopManager.removeAll();
        }
    }

    // the GameClock class will fire this on every tick.
    // it's to be used for starting / upgrading the diamond and emerald summoners
    // the ui clock will be handled in the gameClock class
    public void clockTick(int gameSecondsElapsed) {
        // this is for the diamond and emerald summoners
       diaEmeSummoners.onClockTick(gameSecondsElapsed);
       // this is for the team summoners
        // will run a check each second to see if the summoner needs to drop anything
       for (Team team : teams) {
           team.onClockTick(gameSecondsElapsed);
       }
       // stop hunger
        for (UUID uuid : players)  {
            Player player = Bukkit.getPlayer(uuid);
            player.setFoodLevel(20);
            // check for out of bounds
            checkPlayerWithinArena(player);
            // update compass target
            updateCompass(player);
        }

        // fires the clock tick function in the game class so it can do stuff too.
        game.clockTick();
    }

    private void  updateCompass(Player player) {
        Inventory inventory = player.getInventory();
        if (inventory.contains(Material.COMPASS)) {
            Team team = getTeam(player.getUniqueId());
            Player target = getNearestEnemy(player, team, 250.0);
            if (target != null) {
                player.setCompassTarget(target.getLocation());
            }
        }
    }

    // check if the player is outside the arena boundary
    public void checkPlayerWithinArena(Player player) {
        if (borderCheck.contains(player.getLocation(), true) == false) {
            // teleport the player back to spawn.
            Team team = getTeam(player.getUniqueId());
            team.teleportPlayerToSpawn(player);
            player.sendMessage("Please stay within the game boundaries!");
        }
    }

    public  Player getNearestEnemy(Player player, Team team, Double range) {
        double distance = Double.POSITIVE_INFINITY; // To make sure the first
        // player checked is closest
        Player target = null;

        for (Entity entity : player.getNearbyEntities(range, range, range)) {
            if (!(entity instanceof Player))
                continue;
            if(entity == player) continue; //Added this check so you don't target yourself.
            Player p = (Player) entity;
            if(team.equals(getTeam(p.getUniqueId()))) continue; // on same team
            double distanceto = player.getLocation().distance(entity.getLocation());
            if (distanceto > distance)
                continue;
            distance = distanceto;
            target = (Player) entity;
        }
        return target;
    }

    // this is fired at the end of the game (after a short FINISHING gamestate for winners fireworks)

    public void reset() {
        System.out.println("Game resetting");
        // update this servers sign in the hub server.

        // should only be FINISHING at th eend of the game but just in case
        if (state == GameState.LIVE || state == GameState.FINISHING) {
            System.out.println("Game was live or finishing");
            // stop and remove the game clock, it needs to be recreated for each game.
            if (gameClock != null) {
                gameClock.stop();
                gameClock = null;
            }
            // loop through all the players
            for (UUID uuid : players) {
                Player player = Bukkit.getPlayer(uuid);
                // remove any potion effect they may have
                for (PotionEffect effect : player.getActivePotionEffects()) {
                    player.removePotionEffect(effect.getType());
                }
                // teleport them to the hub server
                teleportPlayerToHub(player);
            }

            // wait 2 seconds (could probably reduce this to 0.5) then restart the server.
            // one of the config files identifies which file needs to be run to restart the server
            // in windows it will be the start.bat file, on the live (linux) server its start.sh (or something like that)
            Bukkit.getScheduler().scheduleSyncDelayedTask(main, new Runnable() {
                public void run() {
                    Bukkit.spigot().restart();
                }
            }, 40L);

        } else {
            System.out.println("Game was countdown");
            // game in countdown state.
            sendTitleSubtitle("","", null, null);
            countdown.cancel();
            countdown = new Countdown(main, this);
            state = GameState.RECRUITING;
            System.out.println("update lobby on arena reset");
            updateLobby();
        }
    }

    // this fires just as the game starts putting each player in a team
    // after doing this the function to remove empty teams should be called.
    private void addPlayersToTeams() {
        // the number of players per team in this server
        int teamSize = ConfigManager.getTeamSize();
        // shuffle the list
        Collections.shuffle(teams);
        // put black at the end
        for (int i=0; i<teams.size(); i++){
            Team item = teams.get(i);
            if (item.getTeamName().equals("Black")) {
                // if it's black remove it
                teams.remove(item);
                // but add it in to the end of the list.
                teams.add(item);
            }
        }
        // Let's see how many teams we need
       // num teams = num players/team size round up
        int numTeamsRequired = (int) Math.ceil((double)players.size() / teamSize);
        // remove teams that we don't need by removing the last team from the list
        // until numTeamsToRemove = 0
        int numTeamsToRemove = teams.size() - numTeamsRequired;
        if (numTeamsToRemove > 0) {
            while (numTeamsToRemove > 0) {
                int s = teams.size() - 1;
                teams.get(s).removeBed();
                teams.remove(s);
                numTeamsToRemove --;
            }
        }

        for (UUID uuid : players) {
            // need to check the player is not already in a team
            boolean inTeam = false;
            for (Team team : teams) {
                if (team.playerInTeam(uuid)) {
                    inTeam = true;
                    break;
                }
            }

            if (inTeam == false) {
                // they are not already in a team, sort the teams so one with lowest number
                // of players is first.
                // if it's the
                teams.sort(Comparator.comparing(Team::getNumPlayers));
                // add the player to the first (emptiest) team.
                teams.get(0).addPlayer(uuid);
                Bukkit.getPlayer(uuid).sendMessage("You are in the " + teams.get(0).getTeamName() + " team");
            }
        }
    }

    // this uses bungee to move the player to the hub server.
    public void teleportPlayerToHub(Player player) {
        player.sendTitle("","");
        player.getInventory().clear();
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF("Lobby");
        player.sendPluginMessage(main, "BungeeCord", out.toByteArray());

    }

    // this sets all the team instances when the server starts.
    // players will be added just as the game starts and empty teams removed again.
    private void setTeams(FileConfiguration mapConfig) {
       // FileConfiguration config = main.getConfig();
        for (String str : mapConfig.getConfigurationSection("teams").getKeys(false)) {
            // look at the team class to see how teams are made.
            teams.add(new Team(main, this, Integer.parseInt(str), worldName, mapConfig));
        }
    }

    // fired when the game is won
    private void gameWon(Team winningTeam) {
        // game state finishing only lasts for about 3 seconds
        state = GameState.FINISHING;
        // stop the clock to prevent game end countdown continuing
        if (gameClock != null) {
            gameClock.stop();
            gameClock = null;
        }

        // show the fireworks
        winningTeam.showWinFireWorks();
        // chat message etc
        sendMessage(ChatUtils.arenaChatPrefix + ChatColor.GOLD + winningTeam.getTeamName() + " team Wins!");
        sendTitleSubtitle("" + winningTeam.getTeamChatColor() + winningTeam.getTeamName() + " wins!", "",
                "6", null);
        winningTeam.sendMessage( ChatColor.GOLD + "Your team won!");
        // after 10 seconds for fireworks reset(restart) the arena
        new BukkitRunnable() {
            @Override
            public void run() {
                reset();
            }
        }.runTaskLater(main, 200);
    }

    // fired when the game is a draw
    public void gameDraw() {
        // game state finishing only lasts for about 3 seconds
        state = GameState.FINISHING;
        for (UUID uuid :players) {
            world.playSound(Bukkit.getPlayer(uuid).getLocation(), Sound.ENDERDRAGON_GROWL, 100, (float) 1);
        }
        // after 5 seconds reset(restart) the arena
        new BukkitRunnable() {
            @Override
            public void run() {
                reset();
            }
        }.runTaskLater(main, 100);
    }


    /* Public functions - only these can be accessed by classes outside the arena class */

   // send all players in the arena a message
    public void sendMessage(String message) {
        for (UUID uuid : players) {
            Bukkit.getPlayer(uuid).sendMessage(ChatUtils.arenaChatPrefix + ChatColor.WHITE + message);
        }
    }

  /*  // send all players in the arena a title
    public void sendTitle(String title, String subtitle) {
        for (UUID uuid : players) {
            Bukkit.getPlayer(uuid).sendTitle(title, subtitle);
      }
    } */

    // Called from the connect listener when a player joins the server
    public  void addPlayer(Player player) {
        // make sure they have no active potion effects
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
        // add some estra stuff for OP
        if (player.isOp()) {
            addOPLobbyItems(player);
        }
        // add them to the arenas list of players
        players.add(player.getUniqueId());
        player.setGameMode(GameMode.SURVIVAL);

        // Already checked for Live or finishing, can only be recruiting or countdown here
        // start the countdown if we now have the minimum number of players as defined in the config file
        if(state.equals(GameState.RECRUITING)) {
            if (players.size() >= ConfigManager.getRequiredPlayers()) {
                this.countdown = new Countdown(main, this);
                // start the countdown
                countdown.start();
                System.out.println("foined");
            }
        } else {
            // COUNTDOWN state
            if(players.size() <= ConfigManager.getMaxPlayers()) {
                // not at max players yet
                countdown.playerJoined();
                System.out.println("foined again");
            } else if (players.size() == ConfigManager.getMaxPlayers()) {
                // this player puts us to max
                countdown.atMaxPlayers();
            }
        }
    }

    private void addOPLobbyItems(Player player) {
        ItemStack iron = new ItemStack(Material.IRON_INGOT);
        ItemMeta ironMeta = iron.getItemMeta();
        ironMeta.setDisplayName(ChatColor.RED + "Start the game");
        iron.setItemMeta(ironMeta);
        player.getInventory().setItem(7, iron);

        ItemStack glass = new ItemStack(Material.GLASS);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(ChatColor.RED + "Simulate player join for queue time test");
        glass.setItemMeta(glassMeta);
        player.getInventory().setItem(6, glass);
    }

    // called from connect listener when player has left the server.
    // this happens when the player exits the game (escape > leave)
    // it is also fired at the end of the game when all players are
    // removed from the server back to the lobby.
    // This is called from the player quit event in the connect listener class
    // regardless o how the player left. This is because as we are using bungee the players will
    // always leave the server at game end, when quitting etc so the onplayerquit event will catch all
    // leaving players.
    // We just decide what to do below when a player leaves depending on the current game state etc.
    public void playerLeftServer(Player player) {
        updateLobby();
        // golem & silverfish untarget this player
        untargetPlayer(player);
        UUID uuid = player.getUniqueId();
        System.out.println("player left server uuid = " + uuid.toString());
        // remove the player from the arena player list
        players.remove(uuid);
        // remove the player form any team they may be in
        for (Team team : teams) {
            team.removePlayer(uuid);
        }
        // if they are a spectator no need to do anything regarding check for game win.
        // not this checks for NOT a spectator first
        if (player.getGameMode() != GameMode.SPECTATOR) {
            if (state == GameState.COUNTDOWN && players.size() < ConfigManager.getRequiredPlayers()) {
                sendMessage(ChatColor.RED + "Not enough players countdown stopped");
                // does not restart the server as game has not started, just resets the countdown
                reset();
                return;
            }

            if (state == GameState.LIVE) {
                // if the player quit the server manually I'll need to make sure that all teams without players
                // don't have beds.
                for (Team team : teams) {
                    if (team.getNumPlayers() == 0 ) {
                        if (team.hasBed()) {
                            System.out.println("found a team with no players but a bed, removing the bed");
                            team.removeBed();
                        }
                    }
                }
                // update the scoreboard, this is not required everytime someone leaves the sever but is required
                // if the last player on a team quits.  It's easier doing it everytime than figuring out if that
                // is the case.
                updateTeamsScoreboard();
                // this stage will be reached if a non spectator player leaves the game during its LIVE phase
                // the only way a player can have left the game in this case is if they actually decided to
                // quit (close the game) or use any inventory item that causes them to leave the game.
                // At this point they will have eithre left the game or be back in the hub world.
                checkGameWon();

            }
        }

    }

    // this uses the Sockexchange Plugin for talking between servers to let the hub server know the status of this server.
    public void updateLobby() {
        // arr 0 = requester sock name, only lobby for now
        // 1 = request
        // 2 = the sock name of this server
        SockExchangeApi api = main.getSockExchangeApi();
        String replyString = "";
        String gameName = config.getString("game-name");
        // need to return this servers sock name, event that caused response, game status, num players, max players
        replyString = api.getServerName() + ".report-status." + gameName + "." + state.toString() + "." + players.size() + "." + maxPlayers;

        System.out.println("Reply string:" + replyString);
        // not sure what this was for.  probably trying to fix an issue, could probably be removed.
        System.out.flush();

        byte[] byteArray = replyString.getBytes();
        api.sendToServer("LobbyChannel", byteArray, "Lobby");
    }

    /* TEAMS */

    // This is called from the even listener when a bed is broken, both bed locations are passed
    public  void teamBedBroken(Location location1, Location location2) {
        // this loops through each team running a function to see if either of the locations
        // is that team beds location.  I only store one location for each team bed in the config.
        for (Team team : teams) {
            if (team.isTeamBedLocation(location1,location2)) {
                // set this team instances bed to broken
                team.setBedBroken();
                // send a message to players in that team.
                sendMessage("Bed broken");
            }
        }
        // update all teams scoreboards - not currently working
        updateTeamsScoreboard();
    }

    public void breakAllBeds() {
        for (Team team : teams) {
           team.removeBed();
        }
        for (UUID uuid :players) {
            world.playSound(Bukkit.getPlayer(uuid).getLocation(), Sound.ENDERDRAGON_GROWL, 100, (float) 1);
        }
        // update all teams scoreboards - not currently working
        updateTeamsScoreboard();
    }

    // update the team scoreboard (which beds/teams still alive)
    // might be working now as i noticed when making these comments that
    // my num player check was > 1 rather than > 0 so it would never have been called.
    // d'oh
    // this is called from the arena class when a bed is broken and from the game class
    // when a team is eliminated.
    public void updateTeamsScoreboard() {
        for (Team team : teams) {
            if (team.getNumPlayers() > 0 ) {
                team.updateScoreBoard();
            }
        }
    }

    /* INFO */

    // returns the current game start
    public GameState getState() { return state;}

    // returns the current list of teams.
    // when a team is eliminated all its players are removed but I don't remove the team instance
    // as I want to keep it's summoner going at 1/4 speed and if I remove the team I would remove
    // the summoner associated with it.
    public List<Team> getTeams() { return teams;}

    // return just teams with players for the scoreboard.

    public List<Team> getTeamsWithPlayers() {
        // this just filters the list of team instances and returns the ones who have more than 0 players.
        return teams.stream()
                .filter(a -> a.getNumPlayers() > 0).collect(Collectors.toList());
    }

    // returns the list of all the players in the game
    public List<UUID> getPlayers() {return players;}

    // returns the game class
    public Game getGame() {return game;}

    // updates the game start
    public void  setState(GameState newState) {state = newState;}

    // returns the arena ID
    public int getId() {return  ID;}

    // returns the arenas world
    public World getWorld() {return world;}

    // returns the lobby spawn location
    public Location getSpawn() {return spawn;}

    // returns the specator spawn location
    public Location getSpectatorSpawn() {return spectatorSpawn;}
    public void addPlayerBlock(Block block) {
        if (block != null) {
            playerBlocks.add(block);
        }
    }

    // if the passed block was added by a player remove it from the list of player blocks & return true
    public boolean removePlayerBlock(Block block) {
        if (playerBlocks.contains(block)) {
            playerBlocks.remove(block);
            return true;
        }
        return false;
    }

    // check if the block was placed by a player
    public boolean isPlayerBlock(Block block) {
        if (playerBlocks.contains(block)) {
            return true;
        }
        return false;
    }

    // returns the team instance for the given team name
    // returns null if the team is not found
    public Team getTeam(String name) {
        if (teams == null) {
            return null;
        }
        for (Team team : teams) {
            if (team.getTeamName().equalsIgnoreCase(name)) {
                return team;
            }
        }
        return  null;
    }

    // returns the team instance for the team that contains the passed player GUID
    // returns null if not found
    public Team getTeam(UUID uuid) {
        if (teams == null) {
            return null;
        }
        for (Team team : teams) {
            if (team.playerInTeam(uuid)) {
                return team;
            }
        }
        return  null;
    }

    // send big red text to all players in the arena
    public void sendTitleSubtitle(String titleMessage, String subtitleMessage, String titleColor, String subTitleColour) {
        if (titleColor == null) {
            titleColor = "4"; // dark red
        }
        if (subTitleColour == null) {
            subTitleColour = "6"; // gold
        }
        for (UUID uuid : players) {

            PacketPlayOutTitle title = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE,
                        IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + "§" + titleColor + titleMessage + "\"}"), 5, 100, 10);
                ((CraftPlayer) Bukkit.getPlayer(uuid)).getHandle().playerConnection.sendPacket(title);



            PacketPlayOutTitle subtitle = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE,
                        IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + "§" + subTitleColour + subtitleMessage + "\"}"), 5, 100, 10);
                ((CraftPlayer) Bukkit.getPlayer(uuid)).getHandle().playerConnection.sendPacket(subtitle);
        }
    }

    // fired from game class when a team is eliminated & from arena when a player leaves the server
    // checks to see if the game is won
    public void checkGameWon() {
        System.out.println("Checking if game won");
        // game can be won if only one team has players
        List<Team> teamsWithPlayers = new ArrayList<>();
        for (Team team : teams) {
            if (team.getNumPlayers() > 0) {
                teamsWithPlayers.add(team);
            }
        }
        if (teamsWithPlayers.size() == 0) {
            // this should not happen but for debugging I'll check anyway.
            // i'll return the first team
            System.out.println("No teams with players when checking for winner.");
            gameWon(teams.get(0));
        } else if (teamsWithPlayers.size() == 1) {
            // fire the game won function passing the winning team
            gameWon(teamsWithPlayers.get(0));
        }
    }

    // fired from game listener when player damages another player
    // I keep a list of the most recent person to damage any other person in order to credit kills
    // as kills are not handled normally (no one actually dies)
    public void logPlayerLastDamage(UUID damaged, UUID damager) {
            playerDamage.put(damaged, damager);
    }

    // return the last person to damage the given player (null if no player has damaged them)
    public UUID getPlayerLastDamage(UUID damaged) {
        if (playerDamage.containsKey(damaged)) {
            return playerDamage.get(damaged);
        }
        return null;
    }

    // checks if two players are on the same team, used to prevent friendly damage
    public boolean playersOnSameTeam(Player p1, Player p2) {
        for (Team team : teams) {
            if (team.playerInTeam(p1.getUniqueId()) && team.playerInTeam(p2.getUniqueId())) {
                return true;
            }
        }
        return false;
    }


    public  Player getClosestEnemyPlayer(Team team, Entity entity) {
        // get all players within 10 blocks of entity
        List<Player> players = new ArrayList<>();
        for (Entity ps : entity.getNearbyEntities(10, 10, 10)) {
            if (ps instanceof Player) {
                players.add((Player) ps);
            }
        }
        // if no players
        if (players.size() == 0) {
           // System.out.println("no players within 10 blocks of entity");
            return null;
        }
        // remove players on own team
        List<Player> sameTeamPlayer = new ArrayList<>();
        for (Player player : players) {
            if(team.playerInTeam(player.getUniqueId())) {
              //  System.out.println("player " + player.getName() + " is on same team as entity");
                sameTeamPlayer.add(player);
            } else {
              //  System.out.println("player " + player.getName() + " is NOT on same team as entity");
            }
        }
        players.removeAll(sameTeamPlayer);
        // if no players
        if (players.size() == 0) {
          //  System.out.println("no enemy players within 10 blocks of entity");
            return null;
        }
        // get the closest player from the list

        double d = 100;
        Player closest = null;
        for (Player player : players) {
          double dist =   entity.getLocation().distance(player.getLocation());
          if (dist < d) {
              d = dist;
             // System.out.println("player is " + d + " from entity, this is closest so far, setting as target");
              closest = player;
          }
        }
        return closest;
    }

    // this is  a fairly clever (i think) bit of code that returns the squares & crosses to show the teams still
    // in the game on the scoreboard.
    // the score board can show a maximum of 4 blocks in each prefix and suffix so a maaximum of 16teams can be shown
    // over two lines.  This function is called 4 times and returns 4 teams worth of blocks (or crosses) each time to fill the two
    // lines.  What is passed is the starting index of the teams to be returned so (0 then 4 then 8 then 12)
    public String scoreBoardTeams(int startTeam) {
        int i = startTeam;
        // str is what is going to be returned
        String str = "";
        // loop will run 4 times unless interupted
        while (i < startTeam + 4) {
            // max is the current iterated team taking into account the starting index
            // this could be the fist run of the loop but if the starting index was (for example) 8 (team 9 as 0 indexed)
            // max will be 9, if we have gone above the number of teams we actually have with players we want to
            // stop looping
            int max = (i + 1);
            if (getTeamsWithPlayers().size() >= max) {
                // if there are still teams to return get the next one
                Team team = getTeamsWithPlayers().get(i);
                // add corss or square to the string depending on if the team has a bed or not
                if (team.hasBed()){
                    str += ChatUtils.sbSquare(team.getTeamChatColor());
                } else {
                    str += ChatUtils.sbCross(team.getTeamChatColor());
                }
            } else {
                // break out of the loop - see comment above
                i = startTeam + 5;
            }
            i++;
        }
        return str;
    }

    public void golemDied(CraftIronGolem golem) {
        for (Team team : teams) {
            if (team.golemInTeam(golem)) {
             //  System.out.println("Killed golem belonged to " + team.getTeamName() + " team!");
               if  (team.removeGolem(golem)) {
             //      System.out.println("golem removed");
               } else {
                   System.out.println("team.removeGolem returned false.");
               }
            }
        }
    }

    public void silverfishDied(CraftSilverfish silverfish) {
        for (Team team : teams) {
            if (team.silverFishInTeam(silverfish)) {
            //    System.out.println("Killed silverfish belonged to " + team.getTeamName() + " team!");
                if  (team.removeSilverfish(silverfish)) {
              //      System.out.println("silverfish removed");
                } else {
                    System.out.println("team.removesilverfish returned false.");
                }
            }
        }
    }

    public boolean golemConfirmTarget(CraftIronGolem golem, Team targetTeam) {
        if (targetTeam.golemInTeam(golem)) {
            return false;
        }
        return true;
    }

    public boolean silverfishConfirmTarget(CraftSilverfish silverfish, Team targetTeam) {
        if (targetTeam.silverFishInTeam(silverfish)) {
            return false;
        }
        return true;
    }

    public void untargetPlayer(Player player) {
        for (Team team : teams) {
            team.removePlayerAsTarget(player);
        }
    }

    public void checkPlayersAboveFloorLevel() {
        for (UUID uuid : players) {
            Player player = Bukkit.getPlayer(uuid);
            int y = player.getLocation().getBlockY();
            System.out.println("floor level is " + floorY + ", player level is " + y);
            if (y < floorY) {
                System.out.println("Teleporting player up");
                Location playerLocation = player.getLocation();
                player.teleport( new Location(
                        world,
                        playerLocation.getX(),
                        floorY,
                        playerLocation.getZ(),
                        playerLocation.getYaw(),
                        playerLocation.getPitch()));
            }
        }
    }

    public int getMaxPlayers() {return this.maxPlayers;}

    public void simulatePlayerJoinForQueue(Player player) {
        if (state.equals(GameState.RECRUITING)) {
            countdown.start();
            player.sendMessage("Game was recruiting, started the countdown");
        } else {
            countdown.playerJoined();
            player.sendMessage("Game was in countdown, simulated player join in countdown");
        }
    }

}
