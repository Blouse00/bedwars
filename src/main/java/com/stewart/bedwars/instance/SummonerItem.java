package com.stewart.bedwars.instance;
import com.stewart.bedwars.Bedwars;
import com.stewart.bedwars.utils.ItemMetadata;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.bukkit.Sound.CHICKEN_EGG_POP;

// each summoner item instance will denote a material type that drops from a summoner
// team summoners will have more than one summoner item
public class SummonerItem  {

    private int id;
    private Bedwars main;
    // the speed in seconds this item drops
    private int itemSpeedSeconds;
    // the item to drop
    private Material material;
    // drop location
    private Location location;
    // effect & prticle locations
    private Location effectLocation;
    private Location particleLocation;
    // for limiting number of items
    private Chunk chunk;
    // the last 'game seconds' this item was dropped
    private int previousDropSeconds;
    // determines in this summoner item has an armour stand or not to show arrow scale until nex item summon.
    private boolean hasArmourStand;
    // the armourstand, can be null
    private ArmorStand armorStand;
    // a list of unique values for each item summoned
    private List<String> summonedItems;
    private int currentNumArrows;

    public SummonerItem(Bedwars main, Material material, int itemSpeedTicks, Location location, int id, boolean hasArmourStand) {
        this.id = id;
        // speed is stored in ticks in the config, convert it to seconds for this class
        this.itemSpeedSeconds = (itemSpeedTicks / 20);
        this.location = location;
        this.material = material;
        this.main = main;
        this.previousDropSeconds = 0;
        this.currentNumArrows = 0;
        this.chunk = location.getChunk();
        this.hasArmourStand = hasArmourStand;
        setLocations();
        summonedItems = new ArrayList<>();
        if (hasArmourStand) {
            addArmourStand();
        }
    }

    private void addArmourStand() {
        armorStand = (ArmorStand) effectLocation.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        armorStand.setVisible(false);
       // armorStand.setCustomName(ChatColor.GOLD + "ho");
        armorStand.setCustomNameVisible(true);
    }

    // sets the effect and particular location relative to the spawn location
    private void setLocations() {
        effectLocation = location.clone().add(0,-2,0);
        particleLocation  = location.clone().add(0,-1,0);
    }

    /* Public functions can be called from other classes */

    // is fired from the team/diaEmeSummoner class each clock tick
    public void onTick(int currentGameSeconds, boolean dropDoubleItems) {

        // items with a speed of 0 are not to be dropped (for example gold needs to be upgraded before it will drop)
        if (itemSpeedSeconds > 0) {

            // if current game seconds is greater than previous drop seconds plus item speed
            // OR
            // previous drop seconds is 0 (the summoner has just started)
            // drop an item
            if (hasArmourStand) {
                // need seconds since last and interval seconds
                int secondsSincelast = currentGameSeconds - previousDropSeconds;
                tickBeforeStart(secondsSincelast, itemSpeedSeconds);
            }


            // for mid summoners (has armour stand), it can start on its first tick.
            if ((currentGameSeconds >= (previousDropSeconds + itemSpeedSeconds)) || (previousDropSeconds == 0 && hasArmourStand)) {

               // String matType = material.toString();
              //  if (!hasArmourStand) {
              //      System.out.println("Summoner: " + matType + ", previousDropSeconds: = " + previousDropSeconds +
              //              ", itemSpeedSeconds " + itemSpeedSeconds + ", currentGameSeconds: " + currentGameSeconds);
               // }

                // get the numer of this items already spawned within 4 blocks of the summoner
                int numItems = getNumItems(material);

                // if over a certain amount don't spawn anymore.
                if (material.equals(Material.IRON_INGOT) && numItems < 151 ||
                        material.equals(Material.GOLD_INGOT) && numItems < 25 ||
                        material.equals(Material.DIAMOND) && numItems < 13 ||
                        material.equals(Material.EMERALD) && numItems < 5 ) {

                    previousDropSeconds = currentGameSeconds;

                  //  String rand = UUID.randomUUID().toString();
                 //   summonedItems.add(rand);
                    int amount = dropDoubleItems ? 2 : 1;
                    ItemStack dropItem = new ItemStack(material, amount);
                  //  ItemMeta itemMeta = dropItem.getItemMeta();
                  //  ItemMetadata.setMetadata(dropItem, "sid",  rand);
                  //  dropItem.setItemMeta(itemMeta);

                    World thisWorld = location.getWorld();
                    Item ingot = thisWorld.dropItem(location, dropItem);
                    ingot.setVelocity(ingot.getVelocity().zero());

                    // add the item to the list so we can keep track of it

                    thisWorld.playEffect(effectLocation, Effect.MOBSPAWNER_FLAMES, 200);
                    //   thisWorld.playSound(location, CHICKEN_EGG_POP, (float)0.2, (float)2);

                    PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(EnumParticle.ENCHANTMENT_TABLE,
                            true, (float) particleLocation.getX(), (float) (particleLocation.getY()), (float) particleLocation.getZ(),
                            (float) 0.2, (float) 1.5, (float) 0.2, 0, 300);
                    for (Player online : Bukkit.getOnlinePlayers()) {
                        ((CraftPlayer) online).getHandle().playerConnection.sendPacket(packet);
                    }
                } else {
                   // System.out.println("summoner id: " + id + ", too many " + material.toString() + " currently " + numItems + " not spawning any more");
                }

            }
        }
    }

    private int getNumItems(Material material) {
        int num = 0;
        for (Entity entity : chunk.getEntities()) {
        //    System.out.println("checking entity");
            if (entity instanceof Item){//make sure we aren't deleting mobs/players
           //     System.out.println("entity is item");
                Item item = (Item) entity;
                if (item.getItemStack().getType().equals(material)) {
                 //   System.out.println("entity is " + material.toString());
                    double distance = entity.getLocation().distance(location);
                 //   System.out.println("distance is " + distance);
                    if (distance < 4) {
                        num += item.getItemStack().getAmount();
                    }
                }
            }
        }
      //  System.out.println("num = " + num);
        return num;
    }

    // fires for dia & eme mid summoners on tick before they start so can be counted down
    public void tickBeforeStart(int currentSeconds, int startSeconds) {
        // get current as number of 1/8ths of start
        double eighth = startSeconds/8.0;
      //  if (material.equals(Material.DIAMOND)) {
       //     System.out.println("eihth : " + eighth + " startSeconds : " + startSeconds + " currentSeconds : " + currentSeconds);
      //  }
        int numArrows = (int) ((int) currentSeconds / eighth);
      //  System.out.println("Num arrows " + numArrows);
        if (numArrows != currentNumArrows) {
            //update the armourstand text and make a noise
            updateArmourStand(numArrows);
            currentNumArrows = numArrows;
        }

    }

    private void updateArmourStand(int numArrows) {
        char arrowFL = '\u27A4';
       // int soundNum = 0;
        switch(numArrows) {
            case 0:
                // code block
                armorStand.setCustomName(ChatColor.DARK_GRAY + "" + arrowFL + arrowFL + arrowFL + arrowFL + arrowFL + arrowFL + arrowFL + arrowFL);
                break;
            case 1:
                // code block
                armorStand.setCustomName(ChatColor.GREEN + "" + arrowFL + ChatColor.DARK_GRAY + "" + arrowFL + arrowFL + arrowFL + arrowFL + arrowFL + arrowFL + arrowFL);
                break;
            case 2:
                armorStand.setCustomName(ChatColor.GREEN + "" + arrowFL + arrowFL + ChatColor.DARK_GRAY + "" + arrowFL + arrowFL + arrowFL + arrowFL + arrowFL + arrowFL);
                break;
            case 3:
                armorStand.setCustomName(ChatColor.GREEN + "" + arrowFL + arrowFL + arrowFL + ChatColor.DARK_GRAY + "" + arrowFL + arrowFL + arrowFL + arrowFL + arrowFL);
                break;
            case 4:
                armorStand.setCustomName(ChatColor.GREEN + "" + arrowFL + arrowFL + arrowFL + arrowFL + ChatColor.DARK_GRAY + "" + arrowFL + arrowFL + arrowFL + arrowFL);
                break;
            case 5:
                armorStand.setCustomName(ChatColor.GREEN + "" + arrowFL + arrowFL + arrowFL + arrowFL + arrowFL + ChatColor.DARK_GRAY + "" + arrowFL + arrowFL + arrowFL);
                break;
            case 6:
                armorStand.setCustomName(ChatColor.GREEN + "" + arrowFL + arrowFL + arrowFL + arrowFL + arrowFL + arrowFL + ChatColor.DARK_GRAY + "" + arrowFL + arrowFL);
                break;
            case 7:
                armorStand.setCustomName(ChatColor.GREEN + "" + arrowFL + arrowFL + arrowFL + arrowFL + arrowFL + arrowFL + arrowFL + ChatColor.DARK_GRAY + "" + arrowFL);
                break;
            case 8:
                // code block
                armorStand.setCustomName(ChatColor.GREEN + "" + arrowFL + arrowFL + arrowFL + arrowFL + arrowFL + arrowFL + arrowFL + arrowFL);
                break;
            default:
                // code block
        }
        // make a noise that
        if (numArrows == 8) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.getWorld().playSound(armorStand.getLocation(), Sound.SUCCESSFUL_HIT, (float) 1.4, (float) (float) (1.916667 ));
            }
        }
    }

    // updates the summoner speed to the passed speed
    public void upgradeSummonerSpeed(int speedTicks) {
        itemSpeedSeconds = speedTicks / 20;
        System.out.println(material.toString() + " speed seconds " + itemSpeedSeconds);
    }

    // set this summoner item to 1/4 speed
    public void setQuarterSpeed() {itemSpeedSeconds = itemSpeedSeconds * 4;}

    // get tis summoner items id
    public int getID() { return this.id; }
}
