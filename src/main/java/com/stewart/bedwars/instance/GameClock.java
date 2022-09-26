package com.stewart.bedwars.instance;

import com.stewart.bedwars.Bedwars;
import org.bukkit.scheduler.BukkitRunnable;

// this is the game clock that runs when the game starts and 'ticks' every second
public class GameClock  extends BukkitRunnable  {

    private Arena arena;
    public Bedwars main;
    private boolean isRunning;
    private int currentSeconds;

    public GameClock(Bedwars main, Arena arena) {
        this.currentSeconds = 0;
        this.arena = arena;
        this.main = main;
        isRunning = false;
    }

    public void start(){
        runTaskTimer(main, 0, 20);
        isRunning = true;
    }

    public void stop(){
        if (isRunning) {
            isRunning = false;
            currentSeconds = 0;
            cancel();
        }
    }

    // this is what happens every second, it causes the arena instances clockTick() function to fire.
    // that is where all the checks are handled
    @Override
    public void run() {
        arena.clockTick(currentSeconds);
        currentSeconds += 1;
    }
}
