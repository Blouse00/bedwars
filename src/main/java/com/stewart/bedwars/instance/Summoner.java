package com.stewart.bedwars.instance;

import com.stewart.bedwars.Bedwars;
import com.stewart.bedwars.team.Team;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

// this summoner class is used for the team summoners
// it has a list of summonerItems, one for each different item the summoner drops.
// each summoneritem holds data about drop its frequency etc separate from this summoner class.
// the diamond and emerald summoners use the diaEmeSummoner class as they are different in the
// way they are upgraded.
public class Summoner {

    private Bedwars main;
    private Location location;

    private List<SummonerItem> summonerItems;

    // the constructor takes only the main class and location as parameters.
    public Summoner(Bedwars main, Location location) {
        summonerItems = new ArrayList<>();
        this.main = main;this.location = location;

        setInitialSummonerItems();
    }

    // this set the list of summoner items for this summoner
    // as its for team summoners there are 3 possible items
    // they are all there from the start but the speed for gold and diamonds is initially set to 0
    private void setInitialSummonerItems() {
        if (summonerItems != null) {
            summonerItems.clear();
        }
        // get the initial iron speed from the config
        FileConfiguration config = main.getConfig();
        Integer speed = config.getInt("team-summoner-items.iron.speed-1");
        // Add the three summonerItems (class instances) to the list.
        // summoner item cunstructor takes main class, material, speed and id (so I can get it later for upgrade)
        summonerItems.add(new SummonerItem(main, Material.IRON_INGOT, speed, location, 1, false) );
        summonerItems.add(new SummonerItem(main, Material.GOLD_INGOT, 0, location, 2, false) );
        summonerItems.add(new SummonerItem(main, Material.DIAMOND, 0, location, 3, false) );
    }

    // used below in the public functions to return a summoner item given its ID
    // 1 = iron, 2 = gold, 3 = diamond
    private SummonerItem getSummonerItem(int id) {
        for(SummonerItem summonerItem : summonerItems) {
            if(summonerItem.getID() == id) {
                return summonerItem;
            }
        }
        return null;
    }

    // called from the team class when the gold upgrade is bought
    // it is passed if the diamond summoner is bought too or not as that effects the iron speed
    public void startTeamGoldSummoner(boolean diamondActive) {
        // 2 is the gold summoner, get it
        SummonerItem summonerItem = getSummonerItem(2);
        // get the gold speed from the config
        FileConfiguration config = main.getConfig();
        Integer goldSpeed = config.getInt("team-summoner-items.gold.speed");
        // apply this new speed to the summoner item
        summonerItem.upgradeSummonerSpeed(goldSpeed);
        // this is a ternary operator, if diamondActive the iron speed will be 3 (fully maxed)
        // if not it will be 2.
        int ironSpeed = diamondActive ? 3 : 2;
        // upgrade the iron summoner to the appropriate speed
        upgradeIronSpeed(ironSpeed);
    }

    // same as above but for diamond
    public void startTeamDiamondSummoner(boolean goldActive) {
        // 3 is the diamond summoner
        SummonerItem summonerItem = getSummonerItem(3);
        FileConfiguration config = main.getConfig();
        Integer diamondSpeed = config.getInt("team-summoner-items.diamond.speed");
        summonerItem.upgradeSummonerSpeed(diamondSpeed);
        int ironSpeed = goldActive ? 3 : 2;
        upgradeIronSpeed(ironSpeed);
    }

    // same as above but for diamond
    public void startTeamFinalSummoner() {
        FileConfiguration config = main.getConfig();
        // 3 is the diamond summoner
        SummonerItem diamondSummoner = getSummonerItem(3);
        Integer diamondSpeed = config.getInt("team-summoner-items.diamond.speed-2");
        diamondSummoner.upgradeSummonerSpeed(diamondSpeed);

        // 2 is the diamond summoner
        SummonerItem goldSummoner = getSummonerItem(2);
        Integer goldSpeed = config.getInt("team-summoner-items.gold.speed-2");
        goldSummoner.upgradeSummonerSpeed(goldSpeed);

        System.out.println("Final summpner started gold " + goldSpeed + ", diamomnd " + diamondSpeed);
        upgradeIronSpeed(4);
    }

    // this upgrades the iron summoner speed and is called when the diamond or gold upgrade is fired.
    private void upgradeIronSpeed(int ironSpeed) {
        // 1 is the iron summoner, get it from the list
        SummonerItem summonerItem = getSummonerItem(1);
        // get the passed speed iron (1,2,3) from the config
        FileConfiguration config = main.getConfig();
        Integer speed = config.getInt("team-summoner-items.iron.speed-" + ironSpeed);
        // set the iron summoner item to that speed
        summonerItem.upgradeSummonerSpeed(speed);
    }

    // returns all this summoners summoner items
    public List<SummonerItem> getSummonerItems() { return summonerItems;}

    // used to set all the summoners summoneritems to quarter speed.
    public void setQuarterSpeed() {
        for(SummonerItem summonerItem : summonerItems) {
           summonerItem.setQuarterSpeed();
        }
    }

}

