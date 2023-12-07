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
import com.stewart.bedwars.utils.PartyTeamSort;
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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import org.bukkit.util.Vector;
import org.stewart.bb_api.instance.GameInfo;
import org.stewart.bb_api.manager.LeaderboardManager;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class Arena {

    private final Bedwars main;
    private int ID;

    private final Location spawn;
    private Location spectatorSpawn;
    private final FileConfiguration config;
    private int teamSize;

    private GameState state;
    private final List<UUID> players;
    private final List<UUID> outOfGamePlayers;
    private Countdown countdown;
    private PlayersRequiredRunnable playersRequiredRunnable;
    private GameClock gameClock;
    private final List<Team> teams = new ArrayList<>();
    private HashMap<UUID, UUID> playerDamage = new HashMap<>();
    private final HashMap<UUID, Integer> mapVotes = new HashMap<>();
    private DiaEmeSummoner diaEmeSummoners;
    private ShopManager shopManager;
    private final String worldName;
    private final Game game;
    private int maxPlayers;
    private final World world;
    private List<Block> playerBlocks = new ArrayList<>();
    private BorderCheck borderCheck;
    private int floorY;
    private final List<UUID> lstNewPlayers;
    private final List<UUID> lstPityPlayers;
    private final List<UUID> lstPlayersSeenDiaEmHelpMessage;
    private final GameInfo gameInfo;

    // Most of the game code is split between this class and the 'Game' class.  I could have
    // just had it all in this one just as easily.
    // this is created once when the server starts not at the beginning of each game.
    public Arena(Bedwars main, String worldName, String ID) {
        // this arenas ID, will only be one for this game but may be more in others.
        this.ID = Integer.parseInt(ID);
        this.lstNewPlayers = new ArrayList<>();
        this.lstPityPlayers = new ArrayList<>();
        this.lstPlayersSeenDiaEmHelpMessage = new ArrayList<>();
        // the world name, I'm not using multiple arenas at the moment but having this will help when if I do.
        this.worldName = worldName;
        // set team size to 0, will be updated when the first player joins
        this.teamSize = 0;
        // when the arena is created gamestate will be recruiting
        this.state = GameState.RECRUITING;
        // will hold all the players currently on the server, there is a separate player list
        // included in each team instance
        this.players = new ArrayList<>();
        this.outOfGamePlayers = new ArrayList<>();
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
        this.gameInfo = main.getBb_api().getGameInfo("bedwars");
        // the spawn location for players entering the lobby
        world = new WorldCreator(worldName).createWorld();
        spawn = new Location(
                world,
                config.getDouble("lobby-spawn.x"),
                config.getDouble("lobby-spawn.y"),
                config.getDouble("lobby-spawn.z"),
                (float) config.getDouble("lobby-spawn.yaw"),
                (float) config.getDouble("lobby-spawn.pitch"));
        setupLeaderboard();
    }

    public void sendMessageNewPlayers(String message) {
        for (UUID uuid: lstNewPlayers) {
            Bukkit.getPlayer(uuid).sendMessage(ChatUtils.arenaChatPrefix + "" + ChatColor.GREEN + message);
        }
    }

    public void sendMessagePityPlayers(String message) {
        for (UUID uuid: lstPityPlayers) {
            Bukkit.getPlayer(uuid).sendMessage(ChatUtils.arenaChatPrefix + "" + ChatColor.GREEN + message);
        }
    }

    public void mapVote(Player player, Integer slotInt) {
        // check if the map id exists
        String mapPath = "solo-maps";
        if (teamSize == 2) mapPath = "duo-maps";
        if (teamSize == 4) mapPath = "quad-maps";
        for (String str : config.getConfigurationSection(mapPath).getKeys(false)) {
            if (slotInt == config.getInt( mapPath + "." + str + ".slot")) {
                mapVotes.put(player.getUniqueId(), Integer.valueOf(str));
                String m = config.getString(mapPath + "." + str + ".name");
                player.sendMessage("You voted for the " + m + " map!");
            }
        }
        player.closeInventory();
    }




    // fired to start the game, either when countdown completes or the command is entered.
    public void start() {
        System.out.println("Game starting");
        // turn off the server messages in the api
        main.getBb_api().getMessageManager().toggleMessageSending(false);
        // this will be decided by voting at this point eventually
        String configName = getArenaConfigName();
        File file = new File(main.getDataFolder(), configName);
        YamlConfiguration mapConfig =  YamlConfiguration.loadConfiguration(file);
        setWorldTeamsSummoners(mapConfig);
        // add players to teams
        addPlayersToTeams();
        // get rid of any empty teams (need to keep them for their summoners)
        //  removeEmptyTeams();
        // teleports each teams players to their spawn point
        for (Team team : teams) {
            team.teleportPlayersToSpawn();
        }
        if (playersRequiredRunnable != null) {
            playersRequiredRunnable.cancel();
            playersRequiredRunnable = null;
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
        // game was started by command it will still be running & re-call arena.start when it completes
        if (countdown.isRunning()) {
            System.out.println("countdown was running, cancelling it");
            countdown.cancel();
        }
        // start the clock
        // the game clock will be started when the game starts and tick once per second
        // it is used to start & upgrade the diamond & emerald summoners at the right time
        // and will show the game clock in the ui. (& lots of other stuff too)
        this.gameClock = new GameClock(main, this);
        gameClock.start();
        // starts the game class, (sets the game start to live)
        game.start();
        // set the teams part of the scoreboard.
        updateTeamsScoreboard();
        // set team name in each players scoreboard
        for (Team team : teams) {
            team.setScoreboardTeamName();
        }
        addPityKit();
        setScoreboardGameStart();
        sendTitleSubtitle("Fight!", "", null, null);
        // update the sign in the hub server
        updateLobby();
    }

    private void addPityKit() {
        // only if gameInfo (from the api) is not null)
        if (gameInfo != null) {
            // loop through pity players
            for (UUID uuid : lstPityPlayers) {
                Player player = Bukkit.getPlayer(uuid);
                // Loop through pity items
                for (Map.Entry<Material,Integer> pair : gameInfo.getPityKit().entrySet()){
                    // add correct amount of item to player inventory;
                    ItemStack stack = new ItemStack(pair.getKey(), pair.getValue());
                    player.getInventory().addItem(stack);
                }
            }
            // and the new players as they are separate
            for (UUID uuid : lstNewPlayers) {
                Player player = Bukkit.getPlayer(uuid);
                // Loop through pity items
                for (Map.Entry<Material,Integer> pair : gameInfo.getPityKit().entrySet()){
                    // add correct amount of item to player inventory;
                    ItemStack stack = new ItemStack(pair.getKey(), pair.getValue());
                    player.getInventory().addItem(stack);
                }
            }
        }
    }


    //also called from game
    public void addHotbarNetherStar(Player player) {
        // this will open the game inventory menu - which will hold the exit game option.
        ItemStack star = new ItemStack(Material.NETHER_STAR);
        ItemMeta ism = star.getItemMeta();
        ism.setDisplayName(ChatColor.RED + "Open game menu");
        star.setItemMeta(ism);
        player.getInventory().setItem(8, star);
    }

    //also called from game
    public void addWoodenSword(Player player) {
        ItemStack itemStack = new ItemStack(Material.WOOD_SWORD);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.spigot().setUnbreakable(true);
        itemStack.setItemMeta(itemMeta);

        player.getInventory().addItem(itemStack);
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

            String mapPath = "solo-maps";
            if (teamSize == 2) mapPath = "duo-maps";
            if (teamSize == 4) mapPath = "quad-maps";

            // if mapID exists in the config use it
            if (config.contains(mapPath + "." + mapID + ".name")) {
                return config.getString(mapPath + "." + mapID + ".config-name");
            } else {
                System.out.println("Most popular map not found mapID " + mapID);
                // random map
                return getRandomMap();
            }
        }
    }

    private String getRandomMap() {
        System.out.println("Picking a random map");
        String mapPath = "solo-maps";
        if (teamSize == 2) mapPath = "duo-maps";
        if (teamSize == 4) mapPath = "quad-maps";
        ArrayList<String> arr = new ArrayList<>();
        for (String str : config.getConfigurationSection(mapPath).getKeys(false)) {
            arr.add(config.getString(mapPath + "." + str + ".config-name"));
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
            player.getScoreboard().getTeam("sbTime").setSuffix(ChatColor.BLUE + getGame().getGameTime());

        }

        // fires the clock tick function in the game class so it can do stuff too.
        game.clockTick();
        // update time on scoreboard for out of game players
        String gt = getGame().getGameTime();
        for (UUID uuid :outOfGamePlayers) {
            Player player = Bukkit.getPlayer(uuid);
            player.getScoreboard().getTeam("sbTime").setSuffix(ChatColor.BLUE + gt);
        }
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
        if (player != null) {
            if (!borderCheck.contains(player.getLocation(), true)) {
                // teleport the player back to spawn.
                Team team = getTeam(player.getUniqueId());
                if (team != null) {
                    team.teleportPlayerToSpawn(player);
                    player.sendMessage("Please stay within the game boundaries!");
                } else {
                    player.teleport(spectatorSpawn);
                }

            }
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
            if (IsPlayerOutOfGame(p.getUniqueId()) == false) { // target must not be out of the game
                if (team.equals(getTeam(p.getUniqueId()))) continue; // on same team
                double distanceto = player.getLocation().distance(entity.getLocation());
                if (distanceto > distance)
                    continue;
                distance = distanceto;
                target = (Player) entity;
            }
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
            // turn server messages back on
            main.getBb_api().getMessageManager().toggleMessageSending(true);
            // game in countdown state.
            sendTitleSubtitle("","", null, null);
            countdown.cancel();
            countdown = new Countdown(main, this);
            state = GameState.RECRUITING;
            // if no players reset server game mode
            if (players.size() == 0) {
                teamSize = 0;
            }
            System.out.println("update lobby on arena reset");
            updateLobby();
        }
    }

    // this fires just as the game starts putting each player in a team
    // taking into account team size and any parties
    private void addPlayersToTeams() {
        // shuffle the list of teams so they are random each game
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

        // do the party stuff
        PartyTeamSort partyTeamSort = new PartyTeamSort(players, teamSize);
        partyTeamSort.FillTeams();

        // remove teams that we don't need by removing the last team from the list
        // until numTeamsToRemove = 0
        int numTeamsToRemove = teams.size() - partyTeamSort.lstTeams.size();
        if (numTeamsToRemove > 0) {
            while (numTeamsToRemove > 0) {
                int s = teams.size() - 1;
                teams.get(s).removeBed();
                teams.remove(s);
                numTeamsToRemove --;
            }
        }

        // Party team sort will have a list of teams that need to be set as the
        // arena teams.
        int i = 0; // this will be the team iterator
        for (PartyTeamSort.PAFParty sortedTeam : partyTeamSort.lstTeams ) {
            // add the players from the sorted team to this team
            for (UUID uuid : sortedTeam.partyPlayers) {
                teams.get(i).addPlayer(uuid);
                Bukkit.getPlayer(uuid).sendMessage("You are in the " + teams.get(i).getTeamName() + " team");
            }
            i += 1; // increment the team iterator
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
        // for duo & quads maps we will double the amount of items dropped by the team summoner(s)
        boolean doubleSummonerSpeed = true;
        if (teamSize == 1) {
            doubleSummonerSpeed = false;
        }
        for (String str : mapConfig.getConfigurationSection("teams").getKeys(false)) {
            // look at the team class to see how teams are made.
            Team team = new Team(main, this, Integer.parseInt(str), worldName, mapConfig);
            team.setDoubleSummonerItems(doubleSummonerSpeed);
            teams.add(team);
        }
    }

    public void logGamePlayed() {
        for (UUID uuid:players) {
            main.getBb_api().getPlayerManager().increaseValueByOne(uuid, "bw_games");
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
        // log wins for each player in the team
        winningTeam.logWinInApi();
        // show the fireworks
        winningTeam.showWinFireWorks();
        // chat message etc
        sendMessage(ChatColor.GOLD + winningTeam.getTeamName() + " team Wins!");
        sendTitleSubtitle("" + winningTeam.getTeamChatColor() + winningTeam.getTeamName() + " wins!", "",
                "6", null);
        winningTeam.sendMessage( ChatColor.GOLD + "Your team won!");
        // after 10 seconds for fireworks reset(restart) the arena
        new BukkitRunnable() {
            @Override
            public void run() {
                reset();
            }
        }.runTaskLater(main, 300);
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
            Bukkit.getPlayer(uuid).sendMessage(ChatUtils.arenaChatPrefix +  message);
        }
    }

    // Called from the connect listener when a player joins the server
    public  void addPlayer(Player player) {
        // make sure they have no active potion effects
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
        // give them netherStar to open map vote inventory
        ItemStack netherStar = new ItemStack(Material.NETHER_STAR);
        ItemMeta netherStarMeta = netherStar.getItemMeta();
        netherStarMeta.setDisplayName(ChatColor.BLUE+ "Vote for a map");
        netherStar.setItemMeta(netherStarMeta);
        player.getInventory().setItem(0,netherStar);

        // add some extra stuff for OP
        if (player.isOp()) {
            addOPLobbyItems(player);
        }
        // add them to the arenas list of players
        players.add(player.getUniqueId());
        player.setGameMode(GameMode.SURVIVAL);

        setPlayerScoreBoard(player);

        // Already checked for Live or finishing, can only be recruiting or countdown here
        // start the countdown if we now have the minimum number of players as defined in the config file
        int minPlayers = ConfigManager.getRequiredPlayers(teamSize);
        if(state.equals(GameState.RECRUITING)) {
            if (players.size() >= minPlayers) {
                System.out.println("Player joined & put us at or over required min players: " + minPlayers + ", starting countdown");
                this.countdown = new Countdown(main, this);
                // start the countdown
                countdown.start();
            } else {
                sendPlayersNeededForCountdownMessage();
            }
        } else {
            // COUNTDOWN state
            if(players.size() < maxPlayers) {
                // not at max players yet
                countdown.playerJoined();
            } else if (players.size() == maxPlayers) {
                // this player puts us to max
                countdown.atMaxPlayers();
            }
        }
    }

    public void sendPlayersNeededForCountdownMessage() {
        if (state == GameState.RECRUITING) {
            int minPlayers = ConfigManager.getRequiredPlayers(teamSize);
            int playersNeeded = (minPlayers - players.size());
            sendMessage(ChatColor.AQUA + "**********************************************");
            sendMessage(ChatColor.GOLD + "Waiting for " +  playersNeeded + " more players to join");
            sendMessage(ChatColor.AQUA + "**********************************************");

            if (playersRequiredRunnable != null) {
                playersRequiredRunnable.cancel();
            }
            playersRequiredRunnable = new PlayersRequiredRunnable(main, this);
            playersRequiredRunnable.start();
        }


        Bukkit.getScheduler().scheduleSyncDelayedTask(main, new Runnable() {
            int delay = 0;
            @Override
            public void run() {

                delay++;
            }

        }, 140);
    }

    private void addOPLobbyItems(Player player) {
        ItemStack glass = new ItemStack(Material.GLASS);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(ChatColor.RED + "Simulate player join to speed up queue");
        glass.setItemMeta(glassMeta);
        player.getInventory().setItem(6, glass);

        ItemStack iron = new ItemStack(Material.IRON_INGOT);
        ItemMeta ironMeta = iron.getItemMeta();
        ironMeta.setDisplayName(ChatColor.RED + "Start the game");
        iron.setItemMeta(ironMeta);
        player.getInventory().setItem(7, iron);
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
        // golem & silverfish untarget this player
        untargetPlayer(player);
        UUID uuid = player.getUniqueId();
        System.out.println("player left server uuid = " + uuid.toString());
        // remove the player from the arena player list
        players.remove(uuid);
        lstNewPlayers.remove(player.getUniqueId());
        lstPityPlayers.remove(player.getUniqueId());
        // remove the player form any team they may be in
        for (Team team : teams) {
            team.removePlayer(uuid);
        }
        // if they are already out of the game remove them from that list,
        if (IsPlayerOutOfGame(uuid)) {
            outOfGamePlayers.remove(uuid);
        } else {
            // the player is still in the game
            if (state == GameState.COUNTDOWN || state == GameState.RECRUITING) {
                System.out.println("player left while recruiting or countdown");
                // if less than min players & countdown is running, reset the countdown.
                if (players.size() < ConfigManager.getRequiredPlayers(teamSize) && countdown.isRunning()) {
                    sendMessage(ChatColor.RED + "Not enough players countdown stopped");
                    // does not restart the server as game has not started, just resets the countdown
                    reset(); // reset also resets teamsize if no players.
                    return;
                } else {
                    int s = players.size();
                    System.out.println("Players left on server = " + s);
                    if (players.size() == 0) {
                        teamSize = 0;
                    }
                    updateLobby();
                }
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
        replyString = api.getServerName() + ".report-status." + state.toString() + "." + players.size() + "." + maxPlayers + "." + teamSize;

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
        // update team part of scoreboard for all players (even out of game)
        for (UUID uuid :players) {
            Player player = Bukkit.getPlayer(uuid);
            player.getScoreboard().getTeam("sbTeams1").setPrefix(scoreBoardTeams(0));
            player.getScoreboard().getTeam("sbTeams1").setSuffix(scoreBoardTeams(4));
            player.getScoreboard().getTeam("sbTeams2").setPrefix(scoreBoardTeams(8));
            player.getScoreboard().getTeam("sbTeams2").setSuffix(scoreBoardTeams(12));
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

    // returns the spectator spawn location
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
        return playerBlocks.contains(block);
    }

    // returns the team instance for the given team name
    public Team getTeam(String name) {
        for (Team team : teams) {
            if (team.getTeamName().equalsIgnoreCase(name)) {
                return team;
            }
        }
        return  null;
    }

    // returns the team instance for the team that contains the passed player GUID
    public Team getTeam(UUID uuid) {
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

    // send big red text to all players in the arena
    public void playSoundAllPlayers(int countdownSeconds) {
        World world = Bukkit.getWorld(worldName);
        for (UUID uuid : players) {
            if (countdownSeconds == 30 || countdownSeconds == 20 || countdownSeconds == 15 || countdownSeconds == 10) {
                world.playSound(Bukkit.getPlayer(uuid).getLocation(), Sound.NOTE_PLING, 100, (float) 1);
            }
            if (countdownSeconds < 6) {
                world.playSound(Bukkit.getPlayer(uuid).getLocation(), Sound.NOTE_PLING, 100, (float) (1.916667 - (0.0833333 * countdownSeconds)));
                world.playSound(Bukkit.getPlayer(uuid).getLocation(), Sound.NOTE_PLING, 100, (float) 1);
            }
        }
    }

    // send big red text to all players in the arena
    public void playGameStartSounds() {
        World world = Bukkit.getWorld(worldName);
        for (UUID uuid : players) {
            Location location = Bukkit.getPlayer(uuid).getLocation();
            world.playSound(location, Sound.NOTE_PLING, 20, (float) 2);
            world.playSound(location, Sound.NOTE_PLING, 20, (float) 1);
            world.playSound(location, Sound.ENDERDRAGON_GROWL, 100, (float) 1);
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
        if (teamsWithPlayers.isEmpty()) {
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
        List<Player> pclose = new ArrayList<>();
        for (Entity ps : entity.getNearbyEntities(10, 10, 10)) {
            if (ps instanceof Player) {
                // todo check if ps is out of game, if so do not add them
                if (!IsPlayerOutOfGame(ps.getUniqueId())) {
                    pclose.add((Player) ps);
                }
            }
        }
        // if no players
        if (pclose.isEmpty()) {
           // System.out.println("no players within 10 blocks of entity");
            return null;
        }
        // remove players on own team
        List<Player> sameTeamPlayer = new ArrayList<>();
        for (Player player : pclose) {
            if(team.playerInTeam(player.getUniqueId())) {
              //  System.out.println("player " + player.getName() + " is on same team as entity");
                sameTeamPlayer.add(player);
            }
        }
        pclose.removeAll(sameTeamPlayer);
        // if no players
        if (pclose.isEmpty()) {
          //  System.out.println("no enemy players within 10 blocks of entity");
            return null;
        }
        // get the closest player from the list

        double d = 100;
        Player closest = null;
        for (Player player : pclose) {
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
        StringBuilder str = new StringBuilder();
        // loop will run 4 times unless interrupted
        while (i < startTeam + 4) {
            // max is the current iterated team taking into account the starting index
            // this could be the fist run of the loop but if the starting index was (for example) 8 (team 9 as 0 indexed)
            // max will be 9, if we have gone above the number of teams we actually have with players we want to
            // stop looping
            int max = (i + 1);
            if (getTeamsWithPlayers().size() >= max) {
                // if there are still teams to return get the next one
                Team team = getTeamsWithPlayers().get(i);
                // add cross or square to the string depending on if the team has a bed or not
                if (team.hasBed()){
                    str.append(ChatUtils.sbSquare(team.getTeamChatColor()));
                } else {
                    str.append(ChatUtils.sbCross(team.getTeamChatColor()));
                }
            } else {
                // break out of the loop - see comment above
                i = startTeam + 5;
            }
            i++;
        }
        return str.toString();
    }

    public void golemDied(CraftIronGolem golem) {
        for (Team team : teams) {
            if (team.golemInTeam(golem)) {
             //  System.out.println("Killed golem belonged to " + team.getTeamName() + " team!");
               if  (!team.removeGolem(golem)) {
                   System.out.println("team.removeGolem returned false.");
               }
            }
        }
    }

    public void silverfishDied(CraftSilverfish silverfish) {
        for (Team team : teams) {
            if (team.silverFishInTeam(silverfish)) {
                if  (!team.removeSilverfish(silverfish)) {
                    System.out.println("team.removesilverfish returned false.");
                }
            }
        }
    }

    public boolean golemConfirmTarget(CraftIronGolem golem, Team targetTeam) {
        return !targetTeam.golemInTeam(golem);
    }

    public boolean silverfishConfirmTarget(CraftSilverfish silverfish, Team targetTeam) {
        return !targetTeam.silverFishInTeam(silverfish);
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
                int up = floorY + 1;
                player.teleport( new Location(
                        world,
                        playerLocation.getX(),
                        up,
                        playerLocation.getZ(),
                        playerLocation.getYaw(),
                        playerLocation.getPitch()));
            }
        }
    }

    public int GetFloorY() {
        return this.floorY;
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

    public void setTeamSize(int teamSize) {
        this.teamSize = teamSize;
        if (teamSize == 1) {
            this.maxPlayers = 12;
        } else {
            this.maxPlayers = 16;
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                ResetTeamSizeIfNoPlayers();
            }
        }.runTaskLater(main, 200);
    }

    public int getTeamSize() {
       return this.teamSize;
    }

    private void ResetTeamSizeIfNoPlayers() {

        if (players.isEmpty()) {
            System.out.println("Resetting team size to 0 as no players.");
            this.teamSize = 0;
            this.maxPlayers = 10;
            updateLobby();
        } else {
            System.out.println("Not resetting team size as " + players.size() + " player(s).");
        }
    }

    public void addOutOfGamePlayer(Player player) {
        outOfGamePlayers.add(player.getUniqueId());
    }

    public boolean IsPlayerOutOfGame(UUID uuid) {
        for (UUID oog : outOfGamePlayers) {
            System.out.println("-- find " + oog);
            if (oog.equals(uuid)) {
                return true;
            }
        }
        return false;
    }

    public void ifNewPlayerNotSeenDiaEmHelpMessage(Player player) {
        // is new player & has not already seen the message
        if (lstNewPlayers.contains(player.getUniqueId()) && !lstPlayersSeenDiaEmHelpMessage.contains(player.getUniqueId())) {
            player.sendMessage(ChatUtils.arenaChatPrefix + "" +
                    ChatColor.GREEN + "Diamonds & ems can be used for upgrades!  Right-click on a Blaze for upgrades.");
            lstPlayersSeenDiaEmHelpMessage.add(player.getUniqueId());
        }
    }

    private void setupLeaderboard() {
        System.out.println("setupLeaderboard");
        // get the leaderboard manager
        LeaderboardManager leaderboardManager = main.getBb_api().getLeaderboardManager();

        // I don't need the arena names for this game but the function does expect a list
        // I'm only needing the num games + wins/games leaderboards
        List<String> lstArenaNames = new ArrayList<>();

        // load the leaderboards for each arena as well as the wins and ration leaderboards
        leaderboardManager.loadGameLeaderboards(lstArenaNames, "bw");

        // should be up to 5 depending on if enough games have been played to trigger wins/games
        System.out.println("Leaderboards size " + leaderboardManager.getLeaderboardList().size());

        // I don't want to show the kills or deaths leaderboards

        // get the locations for leaderboards spawning from the config
        List<Location> lstLeaderboardLocation = ConfigManager.getLeaderboardSpawns(world);

        System.out.printf("Leaderboard spawn location size " +lstLeaderboardLocation.size());
        // spawn the armour stand leaderboards
        main.getBb_api().getLeaderboardManager().createHologramLeaderboards(lstLeaderboardLocation, 10);

    }

    private void setPlayerScoreBoard(Player player) {

        int numGames = main.getBb_api().getGenericQueries().getIntValue("bb_players", "player_uuid", player.getUniqueId().toString(), "bw_games");
        int numWins = main.getBb_api().getGenericQueries().getIntValue("bb_players", "player_uuid", player.getUniqueId().toString(), "bw_wins");

        if (gameInfo != null) {
            if (numGames < gameInfo.getNewPlayerNumGames()) {
                lstNewPlayers.add(player.getUniqueId());
            } else {
                // they are not a new player
                if (numGames > 0) {
                    // can only use pity system if gameInfo is not null, numGames should be > 0 but ignoring the
                    // player if that is the case
                    // if no wins add to pity list
                    if (numWins < 0) {
                        lstPityPlayers.add(player.getUniqueId());
                        System.out.println(player.getName() + " no wins adding to pity list");
                    } else {
                        // if the players win ratio is lower than defined in gameInfo add them to the pity list
                        float pityRatio = gameInfo.getPityRatio() / 100F;
                        float actualRatio = (float) (numWins/numGames);
                        if (actualRatio <= pityRatio) {
                            lstPityPlayers.add(player.getUniqueId());
                            System.out.println(player.getName() + " win ratio is " + actualRatio +
                                    " which is less than " + pityRatio + ", adding to pity list");
                        }
                    }
                }

            }
        } else {
            System.out.println("-----------------------------Game info is NULL -------------------------------");
        }

        int numKills = main.getBb_api().getGenericQueries().getIntValue("bb_players", "player_uuid", player.getUniqueId().toString(), "bw_kills");
        int numBeds = main.getBb_api().getGenericQueries().getIntValue("bb_players", "player_uuid", player.getUniqueId().toString(), "bw_beds");

        if (gameInfo != null) {

        }


        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = scoreboard.registerNewObjective("ObjectiveRef1", "Useless");
        obj.setDisplayName(ChatColor.GOLD.toString() + ChatColor.BOLD + "Bedwars");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        org.bukkit.scoreboard.Team sbTime = scoreboard.registerNewTeam("sbTime");
        sbTime.addEntry(ChatColor.BOLD.toString());
        sbTime.setPrefix(" ");
        sbTime.setSuffix(" ");
        obj.getScore(ChatColor.BOLD.toString()).setScore(11);

        org.bukkit.scoreboard.Team sbEmpty1 = scoreboard.registerNewTeam("sbEmpty1");
        sbEmpty1.addEntry(ChatColor.DARK_RED.toString());
        sbEmpty1.setPrefix(ChatColor.GOLD + "Protect your");
        sbEmpty1.setSuffix(ChatColor.GOLD + " bed");
        obj.getScore(ChatColor.DARK_RED.toString()).setScore(10);

        org.bukkit.scoreboard.Team sbTeamInfo = scoreboard.registerNewTeam("sbTeamInfo");
        sbTeamInfo.addEntry(ChatColor.STRIKETHROUGH.toString());
        sbTeamInfo.setPrefix(ChatColor.GOLD + "Destroy");
        sbTeamInfo.setSuffix(ChatColor.GOLD + " others!");
        obj.getScore(ChatColor.STRIKETHROUGH.toString()).setScore(9);

        // check out the scoreBoardTeams() function in arena class for comments on how the next 2 bits work.
        org.bukkit.scoreboard.Team sbTeams1 = scoreboard.registerNewTeam("sbTeams1");
        sbTeams1.addEntry(ChatColor.GOLD.toString());
        sbTeams1.setPrefix("  ");
        sbTeams1.setSuffix("  ");
        obj.getScore(ChatColor.GOLD.toString()).setScore(8);

        org.bukkit.scoreboard.Team sbTeams2 = scoreboard.registerNewTeam("sbTeams2");
        sbTeams2.addEntry(ChatColor.GRAY.toString());
        sbTeams2.setPrefix(ChatColor.GREEN + "Games: ");
        sbTeams2.setSuffix(ChatColor.BLUE + "" + numGames);
        obj.getScore(ChatColor.GRAY.toString()).setScore(7);

        org.bukkit.scoreboard.Team sbEmpty2 = scoreboard.registerNewTeam("sbEmpty2");
        sbEmpty2.addEntry(ChatColor.YELLOW.toString());
        sbEmpty2.setPrefix(ChatColor.GREEN + "Wins: ");
        sbEmpty2.setSuffix(ChatColor.BLUE + "" + numWins);
        obj.getScore(ChatColor.YELLOW.toString()).setScore(6);

        org.bukkit.scoreboard.Team sbTeamName = scoreboard.registerNewTeam("sbTeamName");
        sbTeamName.addEntry(ChatColor.GREEN.toString());
        sbTeamName.setPrefix(ChatColor.GREEN + "Kills: ");
        sbTeamName.setSuffix(ChatColor.BLUE + "" + numKills);
        obj.getScore(ChatColor.GREEN.toString()).setScore(5);

        org.bukkit.scoreboard.Team sbBedStatus = scoreboard.registerNewTeam("sbBedStatus");
        sbBedStatus.addEntry(ChatColor.DARK_PURPLE.toString());
        sbBedStatus.setPrefix(ChatColor.GREEN + "Beds broken: ");
        sbBedStatus.setSuffix(ChatColor.BLUE + "" + numBeds);
        obj.getScore(ChatColor.DARK_PURPLE.toString()).setScore(4);

        org.bukkit.scoreboard.Team sbKills = scoreboard.registerNewTeam("sbKills");
        sbKills.addEntry(ChatColor.AQUA.toString());
        sbKills.setPrefix("   ");
        sbKills.setSuffix("   ");
        obj.getScore(ChatColor.AQUA.toString()).setScore(3);

        // empty line
        Score line13 = obj.getScore("       ");
        line13.setScore(2);

        Score address = obj.getScore(ChatColor.GRAY + "play.bashybashy.com");
        address.setScore(1);

        player.setScoreboard(scoreboard);
    }

    private void setScoreboardGameStart() {
        for (UUID uuid :players) {
            Player player = Bukkit.getPlayer(uuid);
            player.getScoreboard().getTeam("sbTime").setPrefix(ChatColor.GREEN + "Game time: ");

            player.getScoreboard().getTeam("sbEmpty1").setPrefix("       ");
            player.getScoreboard().getTeam("sbEmpty1").setSuffix("       ");
            player.getScoreboard().getTeam("sbTeamInfo").setPrefix(ChatColor.GOLD + "Teams ");
            player.getScoreboard().getTeam("sbTeamInfo").setSuffix("remaining");
            player.getScoreboard().getTeam("sbEmpty2").setPrefix("      ");
            player.getScoreboard().getTeam("sbEmpty2").setSuffix("      ");
            player.getScoreboard().getTeam("sbBedStatus").setPrefix(ChatColor.GREEN + "Bed: ");
            player.getScoreboard().getTeam("sbBedStatus").setSuffix(ChatColor.GREEN + "Alive");
            player.getScoreboard().getTeam("sbKills").setPrefix(ChatColor.GREEN + "Kills: ");
            player.getScoreboard().getTeam("sbKills").setSuffix(ChatColor.BLUE + "0");
        }
    }

    public GameInfo getGameInfo() {return gameInfo;}


}
