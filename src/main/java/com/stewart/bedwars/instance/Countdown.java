package com.stewart.bedwars.instance;

import com.stewart.bedwars.Bedwars;
import com.stewart.bedwars.GameState;
import com.stewart.bedwars.manager.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

public class Countdown extends BukkitRunnable {

    private Bedwars main;
    private Arena arena;
    private int countdownSeconds;

    // the countdown class for the lobby, should be fairy self explanatory,
    // pretty much as used in the udemy course
    public Countdown(Bedwars main, Arena arena) {
        this.main = main;
        this.arena = arena;
        this.countdownSeconds = ConfigManager.getCountdownSeconds();
    }

    // start the countdown
    public void start() {
        arena.setState(GameState.COUNTDOWN);
        runTaskTimer(main, 0, 20);
    }

    // fires every tick of the countdown
    @Override
    public void run() {
        if (countdownSeconds == 0) {
            cancel();
            arena.start();
            return;
        }

        if (countdownSeconds <= 10 || countdownSeconds % 15 == 0) {
            arena.sendMessage(ChatColor.GREEN + "Game will start in " + countdownSeconds + " second" + (countdownSeconds ==1 ? "." : "s."));
        }
        arena.sendTitle(ChatColor.GREEN.toString()  + countdownSeconds + " second" + (countdownSeconds ==1 ? "." : "s."), ChatColor.GRAY + "until game starts");

        countdownSeconds --;
    }
}
