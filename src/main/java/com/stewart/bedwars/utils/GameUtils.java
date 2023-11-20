package com.stewart.bedwars.utils;

import com.stewart.bedwars.Bedwars;
import com.stewart.bedwars.instance.Arena;
import com.stewart.bedwars.team.Team;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.*;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

// some random useful functions

public class GameUtils {
    // if passed a block that is a bed this will return the block that contains the other half of the bed
    public static Block getOtherBedBlock(Block block1) {
        // Peter made this
        Location block2Location = block1.getLocation();
        if (block1.getRelative(BlockFace.NORTH).getType().equals(Material.BED_BLOCK) || block1.getRelative(BlockFace.NORTH).getType().equals(Material.BED)) {block2Location.add( new Vector( 0, 0, -1 ) );}
        if (block1.getRelative(BlockFace.SOUTH).getType().equals(Material.BED_BLOCK) || block1.getRelative(BlockFace.SOUTH).getType().equals(Material.BED)) {block2Location.add( new Vector( 0, 0, 1 ) );}
        if (block1.getRelative(BlockFace.EAST).getType().equals(Material.BED_BLOCK) || block1.getRelative(BlockFace.EAST).getType().equals(Material.BED)) {block2Location.add( new Vector( 1, 0, 0 ) );}
        if (block1.getRelative(BlockFace.WEST).getType().equals(Material.BED_BLOCK) || block1.getRelative(BlockFace.WEST).getType().equals(Material.BED)) {block2Location.add( new Vector( -1, 0, 0 ) );}
        return  block2Location.getBlock();
    }

    // Pass this a player and it will display the win fireworks around the player
    // this was made by Peter,  I just shortened the timers so it all happens in 3 seconds.
    public static void winFireworks(Player player, Bedwars main) {
        Location location = player.getLocation();
        World world = player.getWorld();
        //  Note - I have NOT tested the initial Firework Twinkle sounds below, we might need to try a few out until we find ones that sound “right”.  AssaultCourse used a ghast death mixed with a silverfish death mixed with a Wither death and Anvil Ring, for the final portal.
        world.playSound(location, Sound.FIREWORK_TWINKLE, 100, (float) 0.5);
        world.playSound(location, Sound.FIREWORK_TWINKLE2, 100, (float) 0.5);
        world.playSound(location, Sound.WITHER_DEATH, 100, (float) 1);

        Firework firework = world.spawn(location, Firework.class);
        FireworkMeta meta = firework.getFireworkMeta();
        meta.addEffects(FireworkEffect.builder().withColor(Color.RED).withColor(Color.AQUA).with(FireworkEffect.Type.BALL_LARGE).withFlicker().build());
        meta.setPower(1);
        firework.setFireworkMeta(meta);

        Firework firework2 = world.spawn(location, Firework.class);
        FireworkMeta meta2 =  firework2.getFireworkMeta();
        meta2.addEffects(FireworkEffect.builder().withColor(Color.GREEN).withColor(Color.YELLOW).with(FireworkEffect.Type.BALL).withFlicker().build());
        meta2.setPower(1);
        firework2.setFireworkMeta(meta2);

        //  WE NEED A MEANS OF GETTING THE NAME OF THE WINNING TEAM (producing e.g. a variable “winteam”), WHICH WOULD BE USED TO ANNOUNCE THE WINNER, IN THE CHAT AND RED-TITLE LINES BELOW.

        Bukkit.getScheduler().runTaskLater((main), new Runnable() {
            @Override
            public void run() {
                Firework firework3 = player.getWorld().spawn(location, Firework.class);
                FireworkMeta meta3 =  firework3.getFireworkMeta();
                meta3.addEffects(FireworkEffect.builder().withColor(Color.GREEN).withColor(Color.YELLOW).with(FireworkEffect.Type.BALL_LARGE).withFlicker().build());
                meta3.setPower(1);
                firework3.setFireworkMeta(meta3);

                Firework firework4 = player.getWorld().spawn(location, Firework.class);
                FireworkMeta meta4 =  firework4.getFireworkMeta();
                meta4.addEffects(FireworkEffect.builder().withColor(Color.RED).withColor(Color.AQUA).with(FireworkEffect.Type.BURST).withFlicker().build());
                meta4.setPower(1);
                firework4.setFireworkMeta(meta4);}
        }, 21L);

        Bukkit.getScheduler().runTaskLater(main, () -> {
            Firework firework3 = world.spawn(location, Firework.class);
            FireworkMeta meta3 = firework3.getFireworkMeta();
            meta3.addEffects(FireworkEffect.builder().withColor(Color.GREEN).withColor(Color.FUCHSIA).with(FireworkEffect.Type.BALL_LARGE).withFlicker().build());
            meta3.setPower(1);
            firework3.setFireworkMeta(meta3);

            Firework firework4 = world.spawn(location, Firework.class);
            FireworkMeta meta4 = firework4.getFireworkMeta();
            meta4.addEffects(FireworkEffect.builder().withColor(Color.ORANGE).withColor(Color.BLUE).with(FireworkEffect.Type.STAR).withFlicker().build());
            meta4.setPower(1);
            firework4.setFireworkMeta(meta4);
            }, 80L);

        Bukkit.getScheduler().runTaskLater(main, () -> {

            Firework firework3 = world.spawn(location, Firework.class);
            FireworkMeta meta3 = firework3.getFireworkMeta();
            meta3.addEffects(FireworkEffect.builder().withColor(Color.RED).withColor(Color.PURPLE).with(FireworkEffect.Type.BALL).withFlicker().build());
            meta3.setPower(1);
            firework3.setFireworkMeta(meta3);

            Firework firework4 = world.spawn(location, Firework.class);
            FireworkMeta meta4 = firework4.getFireworkMeta();
            meta4.addEffects(FireworkEffect.builder().withColor(Color.OLIVE).withColor(Color.BLACK).with(FireworkEffect.Type.BURST).withFlicker().build());
            meta4.setPower(1);
            firework4.setFireworkMeta(meta4);
            }, 130L);

        Bukkit.getScheduler().runTaskLater(main, () -> {

            Firework firework3 = world.spawn(location, Firework.class);
            FireworkMeta meta3 = firework3.getFireworkMeta();
            meta3.addEffects(FireworkEffect.builder().withColor(Color.BLUE).withColor(Color.WHITE).with(FireworkEffect.Type.BALL_LARGE).withFlicker().build());
            meta3.setPower(1);
            firework3.setFireworkMeta(meta3);

            Firework firework4 = world.spawn(location, Firework.class);
            FireworkMeta meta4 = firework4.getFireworkMeta();
            meta4.addEffects(FireworkEffect.builder().withColor(Color.RED).withColor(Color.AQUA).with(FireworkEffect.Type.BURST).withFlicker().build());
            meta4.setPower(1);
            firework4.setFireworkMeta(meta4);

            }, 200L);


    }

    // this is used for getting the armour dye colour from the passed colour string.
    public static Color getColorFromString(String string) {
        switch (string) {
            case "RED":
                return Color.RED;
                case "BLACK":
                    return  Color.BLACK;
                case "WHITE":
                    return Color.WHITE;
                case "BLUE":
                    return Color.BLUE;
                case "LIME":
                    return  Color.LIME;
                case "GRAY":
                    return Color.GRAY;
                case "FUCHSIA":
                    return Color.FUCHSIA;
                case "NAVY":
                    return  Color.NAVY;
                case "GREEN":
                    return Color.GREEN;
                case "YELLOW":
                    return Color.YELLOW;
                case "ORANGE":
                    return  Color.ORANGE;
                case "PURPLE":
                    return Color.PURPLE;
                default:
                    return Color.SILVER;
            }
        }


    // This returns the chat colour for the given passed String colour
    public static ChatColor getChatColorFromString(String string) {
        switch (string) {
            case "RED":
                return ChatColor.RED;
            case "BLACK":
                return  ChatColor.BLACK;
            case "WHITE":
                return ChatColor.WHITE;
            case "BLUE":
                return ChatColor.BLUE;
            case "LIME":
                return  ChatColor.GREEN;
            case "GRAY":
                return ChatColor.GRAY;
            case "FUCHSIA":
                return ChatColor.LIGHT_PURPLE;
            case "NAVY":
                return  ChatColor.DARK_BLUE;
            case "GREEN":
                return ChatColor.DARK_GREEN;
            case "YELLOW":
                return ChatColor.YELLOW;
            case "ORANGE":
                return  ChatColor.GOLD;
            case "PURPLE":
                return ChatColor.DARK_PURPLE;
            default:
                return ChatColor.GRAY;
        }
    }

    // This returns the chat colour for the given passed String colour
    public static DyeColor getDyeColorFromInt(Integer integer) {
        switch (integer) {
            case 14:
                return DyeColor.RED;
            case 15:
                return  DyeColor.BLACK;
            case 0:
                return DyeColor.WHITE;
            case 3:
                return DyeColor.LIGHT_BLUE;
            case 5:
                return  DyeColor.LIME;
            case 8:
                return DyeColor.GRAY;
            case 6:
                return DyeColor.PINK;
            case 11:
                return  DyeColor.BLUE;
            case 13:
                return DyeColor.GREEN;
            case 4:
                return DyeColor.YELLOW;
            case 1:
                return  DyeColor.ORANGE;
            case 10:
                return DyeColor.PURPLE;
            default:
                return DyeColor.GRAY;
        }
    }

    public static boolean isChunkLoaded(String worldName, Location location) {
        World world = Bukkit.getWorld(worldName);
        Chunk chunk =	world.getChunkAt(location);
        if (chunk.isLoaded()) {
            return true;
        } else {
            System.out.println("chunk not loaded, loading chunk");
            chunk.load();
            return false;
        }
    }

    // used to remove a players equipped armour
    public  static void removeArmour(Player player) {
        player.getInventory().setHelmet(null);
        player.getInventory().setChestplate(null);
        player.getInventory().setLeggings(null);
        player.getInventory().setBoots(null);
    }

    public static void spawnArmourStand(Location location, String name) {
        ArmorStand armorStand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        armorStand.setVisible(false);
        armorStand.setCustomName(ChatColor.GOLD + name);
        armorStand.setCustomNameVisible(true);
    }

    // drop all the player items
    // this could be updated as discussed to drop at their last location if they fall into the void.
    // it works by getting the died players location and inventory, and dropping all items in the inventory
    // at that location.
    public static void dropInventory(Player player) {
        Location loc = player.getLocation().clone();
        Inventory inv = player.getInventory();
        for (ItemStack item : inv.getContents()) {
            if (item != null) {
                loc.getWorld().dropItemNaturally(loc, item.clone());
            }
        }
        // then make sure the inventory is empty, does not affect armour
        inv.clear();
    }

    public static void reduceInHandByOne(Player player) {
        if (player.getInventory().getItemInHand().getAmount() == 1) {
            //  If the player only has one item in his hand set it to null
            player.getInventory().setItemInHand(null);
            System.out.println("last block placed setting to null");
        } else {
            //  If more than one item reduces your inventory amount by one.
            System.out.println("not last block placed, reducing by one");
            player.getInventory().getItemInHand().setAmount(player.getItemInHand().getAmount() - 1);
        }
    }

    public static void makePlayerArmourUnbreakable(Player player) {
        ItemStack is = player.getInventory().getChestplate();
        makeItemUnbreakable(is);
     //   System.out.println("is now unbreakable " + player.getInventory().getChestplate().getItemMeta().spigot().isUnbreakable());

        ItemStack is2 = player.getInventory().getBoots();
        makeItemUnbreakable(is2);
     //   System.out.println("getBoots is now unbreakable " + player.getInventory().getBoots().getItemMeta().spigot().isUnbreakable());

        ItemStack is3 = player.getInventory().getHelmet();
        makeItemUnbreakable(is3);
     //   System.out.println("getHelmet is now unbreakable " + player.getInventory().getHelmet().getItemMeta().spigot().isUnbreakable());

        ItemStack is4 = player.getInventory().getLeggings();
        makeItemUnbreakable(is4);
     //   System.out.println("getLeggings is now unbreakable " + player.getInventory().getLeggings().getItemMeta().spigot().isUnbreakable());
    }

    private static void makeItemUnbreakable(ItemStack itemStack) {
        if (itemStack != null) {
       //     System.out.println("is unbreakable " + itemStack.getItemMeta().spigot().isUnbreakable());
            ItemMeta meta = itemStack.getItemMeta();
            meta.spigot().setUnbreakable(true);
            itemStack.setItemMeta(meta);
        }

    }

    public static void makeMagicCarpet(BlockPlaceEvent placeEvent, Bedwars main) {
        Player player = placeEvent.getPlayer();
        Team team = main.getArenaManager().getArena(player).getTeam(player.getUniqueId());
        if (team != null) {
            int teamColorInt = team.getTeamColorInt();

            Location locPlayerNoPitch = player.getLocation();
            locPlayerNoPitch.setPitch(0F);
            Vector vectorNoPitch = locPlayerNoPitch.getDirection();
            Location pigLoc = placeEvent.getBlock().getLocation();
            pigLoc.add(new Vector(0.5, 0.5, 0.5));
            Arena arena = main.getArenaManager().getArena(player);

            Block bridgeBlock = placeEvent.getBlock();
            pigLoc = bridgeBlock.getLocation().add(new Vector(0.5, 0.5, 0.5));
            bridgeBlock.setType(Material.WOOL);
            bridgeBlock.setData((byte) teamColorInt);
            arena.addPlayerBlock(bridgeBlock);

            final boolean[] cont = {true};
            for (int i = 0; i < 15; ) {
                long time = (i * 4);
                Location finalPigLoc1 = pigLoc;
                int finalI = i;
                Location finalPigLoc2 = pigLoc;
                Bukkit.getScheduler().runTaskLater(main, new Runnable() {
                    @Override
                    public void run() {
                        if (cont[0]) {
                            finalPigLoc1.add(vectorNoPitch);
                            Block pigBlock = finalPigLoc1.getBlock();
                            if (finalI < 5) {
                                player.getWorld().playSound(finalPigLoc2, Sound.SUCCESSFUL_HIT, (float) 1.4, (float) (1.916667 - (0.0833333 * (5 - finalI - 1))));
                            }

                            if (pigBlock.getType() == Material.AIR) {
                                pigBlock.setType(Material.WOOL);
                                pigBlock.setData((byte) teamColorInt);
                                arena.addPlayerBlock(pigBlock);

                                Location effectLocation = finalPigLoc1.clone().add(0, 0.5, 0);
                                player.getWorld().playEffect(effectLocation, Effect.PARTICLE_SMOKE, 300);
                                PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(EnumParticle.ENCHANTMENT_TABLE, true, (float) (effectLocation.getX()), (float) (effectLocation.getY()), (float) (effectLocation.getZ()), (float) .8, (float) .01, (float) .8, 0, 200);
                                for (Player online : Bukkit.getOnlinePlayers()) {
                                    ((CraftPlayer) online).getHandle().playerConnection.sendPacket(packet);
                                }

                                if (finalI % 4 == 0) {
                                    finalPigLoc1.getWorld().playSound(finalPigLoc1, Sound.CHICKEN_EGG_POP, (float) 1.4, (float) (1.916667 - (0.0833333 * 3)));
                                }

                                if (finalI == 14) {
                                    finalPigLoc1.getWorld().playSound(finalPigLoc1, Sound.EXPLODE, (float) 1.4, (float) 1.5);
                                    player.sendMessage(ChatColor.GOLD + "Your bridge builder has placed all its blocks.");
                                }


                            } else {
                                if (pigBlock.getType() != Material.WOOL) {
                                    // hit another block, stop the bridge
                                    finalPigLoc1.getWorld().playSound(finalPigLoc1, Sound.EXPLODE, (float) 1.4, (float) 1.5);
                                    player.sendMessage(ChatColor.RED + "Bridge hit an obstruction and ended.");
                                    cont[0] = false;
                                }
                            }
                        }
                    }
                }, time);
                i++;
            }
        }
    }

    public static void makePlayerInvisibleGameEnd(Player player) {
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            if (p != player)
                p.hidePlayer(player);
        }
        player.setAllowFlight(true);
        player.setFlying(true);
        player.getInventory().clear();
    }

}






