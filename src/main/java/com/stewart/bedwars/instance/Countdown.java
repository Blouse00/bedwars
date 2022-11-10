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
    // this is the minimum it can go down to through people joining & the start of bigger player messages.
    private int finalSeconds = 30;
    private boolean isRunning;

    // the countdown class for the lobby, should be fairy self explanatory,
    // pretty much as used in the udemy course
    public Countdown(Bedwars main, Arena arena) {
        this.main = main;
        this.arena = arena;
        this.countdownSeconds = ConfigManager.getCountdownSeconds();  // 180 initially
        this.isRunning = false;
    }

    // start the countdown
    public void start() {
        arena.setState(GameState.COUNTDOWN);
        runTaskTimer(main, 0, 20);
        isRunning = true;
    }

    // if a player joins I want to reduce the countdown seconds by 33%
    public void playerJoined() {
        // only adjust the countdown seconds if the time remaining is more than 5 seconds over the final countdown time.
        if (countdownSeconds > (finalSeconds + 5)) {
            System.out.println("player joined at countdown secods: " + countdownSeconds);
            int secondsBetweenNowAndFinal = countdownSeconds - finalSeconds;
            int thirdRemoved = secondsBetweenNowAndFinal/3;
            countdownSeconds = countdownSeconds - thirdRemoved;
            System.out.println("Seconds till final is: " + secondsBetweenNowAndFinal);
            System.out.println("Third of that is: " + thirdRemoved);
            System.out.println("Making new coundown seconds: " + countdownSeconds);
            arena.sendMessage(ChatColor.GREEN + "Game will start in " + String.format("%02d:%02d",  (countdownSeconds % 3600) / 60, countdownSeconds % 60));
        } else {
            System.out.println("Countdown - player joined but countdown " + countdownSeconds + " is less than final seconds + 5 " + (finalSeconds + 5));
        }
    }

    // when no more can join set the timer to its final countdown value (+1 to cover for sync issues)
    public void atMaxPlayers() {
        countdownSeconds = (finalSeconds + 1);
        System.out.println("Countdown - FINAL player joined, setting countdown to " + countdownSeconds);
    }

    public boolean isRunning() { return this.isRunning;}

    // fires every tick of the countdown
    @Override
    public void run() {
        if (countdownSeconds == 0) {
            cancel();
            arena.start();
            return;
        }

        // final countdown -  big writing middle of screen
        if (countdownSeconds <= 30) {
            if (countdownSeconds == 30 || countdownSeconds == 20 || countdownSeconds == 15 || countdownSeconds == 10 ) {
                arena.sendTitleSubtitle( countdownSeconds + " second" + (countdownSeconds ==1 ? "." : "s."),
                         "until game starts", "9", "5");
            }
        } else {
            boolean isDivisibleBy10 = countdownSeconds % 10 == 0;
            if (isDivisibleBy10) {
                arena.sendMessage(ChatColor.GREEN + "Game will start in " + String.format("%02d:%02d",  (countdownSeconds % 3600) / 60, countdownSeconds % 60));

            }
        }

        // for the last 5 seconds have a big number on the screen and a chat message.
        if (countdownSeconds <= 5 && countdownSeconds > 0) {
            arena.sendMessage(ChatColor.GREEN + "Game will start in " + countdownSeconds + " second" + (countdownSeconds ==1 ? "." : "s."));
            arena.sendTitleSubtitle(String.valueOf(countdownSeconds), "", String.valueOf(countdownSeconds), null);
        }
        countdownSeconds --;
    }
}
