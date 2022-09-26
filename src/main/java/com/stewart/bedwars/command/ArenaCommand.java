package com.stewart.bedwars.command;

import com.stewart.bedwars.Bedwars;
import com.stewart.bedwars.instance.Arena;
import com.stewart.bedwars.team.Team;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ArenaCommand implements CommandExecutor {

    private Bedwars main;

    // This is where the 'pw' commands are heard
    //  I don't really have much done via commands
    public ArenaCommand(Bedwars main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {

        if(commandSender instanceof Player) {
            Player player = (Player) commandSender;
            Arena arena = main.getArenaManager().getArena(player);
            // this is for updating a team summoner speed, I used it for testing
            // eg "/pw team red summoner g" - thats the 4 args (/pw isn't counted)
            if (args.length == 4 && args[0].equalsIgnoreCase("team")) {
                Team team = arena.getTeam(args[1]);
                if (team == null) {
                    player.sendMessage(ChatColor.RED + "Team not found.");
                } else {
                    if (args[2].equalsIgnoreCase("summoner")) {
                        // d, g and q is for diamond, gold and quarter speed
                        if (args[3].equalsIgnoreCase("d")) {
                            team.startDiamondSummoner();
                        } else if (args[3].equalsIgnoreCase("g")) {
                                team.startGoldSummoner();
                        } else if (args[3].equalsIgnoreCase("q")) {
                            team.setSummonerQuarterSpeed();
                        } else {
                            player.sendMessage(ChatColor.RED + args[3] + " is not a recognised command.");
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + args[2] + " is not a recognised command.");
                    }
                }

            } else if(args.length == 2 && args[0].equalsIgnoreCase("game")) {
                // this one is for '/pw game start' (or finish but i've never used that.
                if (args[1].equalsIgnoreCase("start")) {
                    arena.start();
                }
                if (args[1].equalsIgnoreCase("finish")) {
                    arena.reset();
                }
            } else {
                player.sendMessage("Invalid command use:");
            }
        }
        return false;
    }
}
