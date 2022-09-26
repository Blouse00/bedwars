package com.stewart.bedwars.utils;

import org.bukkit.ChatColor;

// a simple utility class for returning chat symbols
public class ChatUtils {
    // the grey rectangle chat prefix
    public  static  String arenaChatPrefix = "" + ChatColor.DARK_GRAY + (char)('\u258D') +" ";

    // the square symbol used in the scoreboard - pass it the chat colour to use
    public  static  String sbSquare(ChatColor chatColor) {
        return "" + chatColor + (char) ('\u2588') + " ";
    }

    // the cross used in the scoreboard
    public  static  String sbCross(ChatColor chatColor) {
        return "" + chatColor + (char) ('\u2716') + " ";
    }
}
