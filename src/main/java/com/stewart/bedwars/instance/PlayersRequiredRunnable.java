package com.stewart.bedwars.instance;

import com.stewart.bedwars.Bedwars;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayersRequiredRunnable  extends BukkitRunnable {
    private Bedwars main;
    private Arena arena;
    private boolean isRunning;

    // the countdown class for the lobby, should be fairy self explanatory,
    // pretty much as used in the udemy course
    public PlayersRequiredRunnable(Bedwars main, Arena arena) {
        this.main = main;
        this.arena = arena;
        this.isRunning = false;
    }

    // start the countdown
    public void start() {
        runTaskLater(main, 1200);
        isRunning = true;
    }

    public boolean isRunning() { return this.isRunning;}

    // fires every tick of the countdown
    @Override
    public void run() {
       arena.sendPlayersNeededForCountdownMessage();
    }
}
