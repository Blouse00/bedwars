package com.stewart.bedwars.listeners;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.stewart.bedwars.Bedwars;
import com.stewart.bedwars.GameState;
import com.stewart.bedwars.instance.Arena;
import com.stewart.bedwars.team.Team;
import com.stewart.bedwars.utils.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftIronGolem;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftSilverfish;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftTNTPrimed;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;

// this class handles all gam erelated listener events, there are a lot,
public class GameListener implements Listener {

    private Bedwars main;

    public GameListener(Bedwars main) {
        this.main = main;
    }

    // Listen for when a player receives an amount of damage that would cause them to die,
    // if so cancel the event, we don't want them to actauly die and face the respawn screen.
    // To the players it will look like he died though.
    @EventHandler
    public void damage(EntityDamageEvent ev) //Listens to EntityDamageEvent
    {
            if (ev.getEntity() instanceof Villager || ev.getEntity() instanceof PigZombie ||
                    ev.getEntity() instanceof Blaze || ev.getEntity() instanceof Witch) {
                // prevents damage to shop peaple
                ev.setCancelled(true);
                return;
            }
        System.out.println("Damage type = " + ev.getCause().toString());


        if (ev instanceof EntityDamageByEntityEvent) {

         //   System.out.println("Entity damage entity event");
            EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) ev;
          //  System.out.println("Damager = " + event.getDamager().toString());
            // damaged by an entity
            // each time a player damages a player I need to log who damaged who in a hashmap in the arena
            // this allows me to determine who killed a player in the EntityDamageEntity event above.
            if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
                Player damaged =  (Player) event.getEntity();
                Player damager =  (Player) event.getDamager();
                Arena arena = main.getArenaManager().getArena(damaged);
                // either player is out of the game
                if ((arena.IsPlayerOutOfGame(damaged.getUniqueId())) || (arena.IsPlayerOutOfGame(damaged.getUniqueId()))) {
                    event.setCancelled(true);
                    return;
                }
                // make sure the damager is no longer spawn protected.
                arena.getGame().removeSpawnProtect(damager);
                // check if the damaged player is currently spawn protected or the damager is out of the game
                if (arena.getGame().playerSpawnProtected(damaged)) {
                    event.setCancelled(true);
                    return;
                }
                // In lobby cancel pvp damage
                if (arena.getState() == GameState.RECRUITING || arena.getState() == GameState.COUNTDOWN) {
                    event.setCancelled(true);
                    return;
                }
                if (arena.playersOnSameTeam(damaged, damager)) {
                    // if both on the same team cancel event.
                    event.setCancelled(true);
                    return;
                } else {
                    // if different teams log player last damaged by
                    arena.logPlayerLastDamage(damaged.getUniqueId(), damager.getUniqueId());
                    CheckPlayerDies(damaged, ev);
                }
                return;
            }
            // out of game players should not be able to cause damage
            if (event.getDamager() instanceof Player) {
                Player player = (Player) event.getDamager();
                Arena arena = main.getArenaManager().getFirstArena();
                if (arena.IsPlayerOutOfGame(player.getUniqueId())) {
                    System.out.println("Out of game player damaging another!");
                    ev.setCancelled(true);
                    return;
                }
            }
            // if player damaged by silverfish or golem
            if (event.getEntity() instanceof Player &&
                    (event.getDamager() instanceof IronGolem || event.getDamager() instanceof Silverfish)) {
                Player damaged =  (Player) event.getEntity();
                Arena arena = main.getArenaManager().getArena(damaged);
                // check if the player is currently spawn protected.
                if (arena.getGame().playerSpawnProtected(damaged)) {
                    event.setCancelled(true);
                    return;
                }
                CheckPlayerDies(damaged, ev);
                return;
            }
            // the projectile stuff is kind of untested
            if (event.getDamager() instanceof Arrow && event.getEntity() instanceof Player) {
                Arrow proj = (Arrow) event.getDamager();
                if (proj.getShooter() instanceof Player) {
                    Player damaged = (Player) event.getEntity();
                    Player damager = (Player) proj.getShooter();
                    FireballOrTNTExplosionPlayerHit(event, damager, damaged);
                }
                return;
            }
            if (event.getDamager() instanceof Fireball && event.getEntity() instanceof Player) {
                Fireball proj = (Fireball) event.getDamager();
                if (proj.getShooter() instanceof Player) {
                    Player damager = (Player) proj.getShooter();
                    Player damaged = (Player) ev.getEntity();
                    FireballOrTNTExplosionPlayerHit(event, damager, damaged);
                }
                return;
            }
            if (event.getDamager() instanceof CraftTNTPrimed && event.getEntity() instanceof Player) {
                CraftTNTPrimed tnt = (CraftTNTPrimed) event.getDamager();
                if (tnt.hasMetadata("placer")) {
                    System.out.println("tnt placed by player");
                    Player damager = getBlockPlacedPlayer(tnt);
                    Player damaged = (Player) ev.getEntity();
                    System.out.println("dmager = " + damager.getDisplayName() + ", damaged = " + damaged.getDisplayName());
                    FireballOrTNTExplosionPlayerHit(event, damager, damaged);
                } else {
                    System.out.println("tnt not placed by palyer!");
                    ev.setCancelled(true);
                }
                return;
            }

            if(event.getDamager() instanceof Snowball) {
                Snowball sn = (Snowball)event.getDamager();
                if(sn.getShooter() instanceof Player) {
                    Player shooter = (Player)sn.getShooter();
                    System.out.println("player hit by snowball fired by " + shooter.getName());
                    event.setCancelled(true);
                }
            }

        } else {
            // dont want to handle entity explosion here
            if (ev.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION) {
                System.out.println("cancelling block_explosion");
                ev.setCancelled(true);
            } else {
                System.out.println("NOT Entity damage entity event");
                if (ev.getEntity() instanceof Player) {
                    // get the player that took damage
                    Player player = (Player) ev.getEntity();
                    Arena arena = main.getArenaManager().getFirstArena();
                    if (arena.IsPlayerOutOfGame(player.getUniqueId())) {
                        ev.setCancelled(true);
                        return;
                    }
                    // need to find out if another player killed this player, this event does not expose
                    // who caused the damage it can only tell us if it was an ENTITY_ATTACK or not.
                    CheckPlayerDies(player, ev);
                }
            }
        }

    }

    public Player getBlockPlacedPlayer(CraftTNTPrimed tnt) {
        List<MetadataValue> metaDataValues = tnt.getMetadata("placer");
        for (MetadataValue value : metaDataValues) {
            String name = value.asString();
            return Bukkit.getPlayer(name);
        }
        return null;
    }

    public Player getBlockPlacedPlayer(Block block) {
        List<MetadataValue> metaDataValues = block.getMetadata("player");
        for (MetadataValue value : metaDataValues) {
            String name = value.asString();
            return Bukkit.getPlayer(UUID.fromString(name));
        }
        return null;
    }

    private void FireballOrTNTExplosionPlayerHit(EntityDamageByEntityEvent ev, Player damager, Player damaged) {
        Arena arena = main.getArenaManager().getArena(damaged);
        if (arena.playersOnSameTeam(damaged, damager)) {
            // if both on the same team cancel event.
            ev.setCancelled(true);
            damaged.setVelocity(damaged.getVelocity().zero());
            System.out.println("Friendly projectile fire cancelled");
        } else {
            // if different teams log player last damaged by
            arena.logPlayerLastDamage(damaged.getUniqueId(), damager.getUniqueId());
            System.out.println("Hostile projectile damage logged");
            CheckPlayerDies(damaged, ev);
        }
    }

    private void CheckPlayerDies(Player player, EntityDamageEvent ev) {
      //  System.out.println("Check player dies fired damage = " + ev.getFinalDamage());
      //  System.out.println("player health = " + player.getHealth());
        if (player.getHealth() - ev.getFinalDamage() <= 0) {
            System.out.println("Player would have died");
            // cancel death
            ev.setCancelled(true);
            Player damager = null;

            // Need to get the arena instance for the player
            Arena arena = main.getArenaManager().getArena(player);
            // check if the dmage was cused by another entitiy (Player), cant get actual player in this event
            if (ev.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
                // get the player killer by checking the damage hashmap to see who last damaged this player
                UUID duuid = arena.getPlayerLastDamage(player.getUniqueId());
                if (duuid != null) {
                    damager = Bukkit.getPlayer(duuid);
                }
            }
            // bring the player back to 'life'
            player.setHealth(20.0);
            player.setFoodLevel(20);
            // handles checking if game won etc & respawning 'killed' player.
            // damager may be null
            arena.getGame().playerKilled(player, damager);
        }
    }

    @EventHandler
    public void blockDamage(BlockDamageEvent event)
    {
        // if block is a bed
        if(event.getBlock().getType() == Material.BED_BLOCK || event.getBlock().getType() == Material.BED ) {
            Player player = event.getPlayer();
            Block bedBlock1 = event.getBlock();
            // i have a helper function in the gameutils class that will find the other bedblock for a given bed block
            Block bedBlock2 = GameUtils.getOtherBedBlock(bedBlock1);
            Arena arena = main.getArenaManager().getArena(player);
            // only remove the bed if it does not belong to the players team.
            Team team = arena.getTeam(player.getUniqueId());
            if (team != null) {
                if (team.isTeamBedLocation(bedBlock1.getLocation(), bedBlock2.getLocation()))  {
                    event.setCancelled((true));
                }
            } else {
                    // team is null can happen at the end of the game
                event.setCancelled((true));
            }

        }
    }

    @EventHandler
    public void NoPearlDamage(PlayerTeleportEvent event){
        Player p = event.getPlayer();
        if(event.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL){
            event.setCancelled(true);
            p.setNoDamageTicks(1);
            p.teleport(event.getTo());
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event){

        Arena arena = main.getArenaManager().getFirstArena();

        if (arena.getState() == GameState.RECRUITING || arena.getState() == GameState.COUNTDOWN) {
            event.setCancelled(true);
        } else {
            arena.addPlayerBlock(event.getBlock());
            // for placing tnt cancel the evenet and drop some primed tnt instead
            if (event.getBlock().getType() == Material.TNT) {
                System.out.println("TNT placed");
                event.setCancelled(true);
                Location tntLoc = event.getBlock().getLocation();
                tntLoc.add(new Vector(0.5, 0.5, 0.5));
                TNTPrimed tnt = tntLoc.getWorld().spawn(tntLoc, TNTPrimed.class);
                tnt.setMetadata("placer", new FixedMetadataValue(main, event.getPlayer().getName()));
                System.out.println("TNT spawned");
                GameUtils.reduceInHandByOne(event.getPlayer());
            }
            if (event.getBlock().getType() == Material.STONE && event.getBlock().getData() == (byte) 5) {
                System.out.println("Silverfish placed");
                Block block = event.getBlock();
                block.setMetadata("player", new FixedMetadataValue(main, event.getPlayer().getUniqueId().toString()));
            }
            if (event.getBlock().getType() == Material.STONE_PLATE || event.getBlock().getType() == Material.WOOD_PLATE ||
                    event.getBlock().getType() == Material.IRON_PLATE) {
                System.out.println("Floor trap placed");
                Block block = event.getBlock();
                block.setMetadata("player", new FixedMetadataValue(main, event.getPlayer().getUniqueId().toString()));
            }
            if (event.getBlock().getType() == Material.CARPET) {
                GameUtils.makeMagicCarpet(event, main);
                GameUtils.reduceInHandByOne(event.getPlayer());
            }
        }

    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Arena arena = main.getArenaManager().getArena(player);

        if(event.getBlock().getType() == Material.BED_BLOCK || event.getBlock().getType() == Material.BED ) {
            // if it is a bed get both bed blocks & cancel the event
            Block bedBlock1 = event.getBlock();
            Block bedBlock2 = GameUtils.getOtherBedBlock(bedBlock1);
            event.setCancelled(true);
            // only remove the bed if it does not belong to the players team.
            Team team = arena.getTeam(player.getUniqueId());
            // team might be null while game is finishing
            if (team != null) {
                // make sure it's not the players own bed
                if (team.isTeamBedLocation(bedBlock1.getLocation(), bedBlock2.getLocation()) == false) {
                    System.out.println("Player destroyed another teams bed");
                    // replace detroyed bed with air
                    bedBlock1.setType(Material.AIR, false);
                    bedBlock2.setType(Material.AIR, false);
                    // make the noise
                    Location locsound = bedBlock1.getLocation();
                    String locworld = event.getBlock().getWorld().getName();
                    Bukkit.getWorld(locworld).playSound(locsound, Sound.ENDERDRAGON_GROWL, 100, (float) 1);
                    // call the bed broken arena function that will handle the game end check etc.
                    arena.teamBedBroken(bedBlock1.getLocation(), bedBlock2.getLocation());
                    // log broken bed for the player
                    main.getBb_api().getPlayerManager().increaseValueByOne(player.getUniqueId(), "bw_beds");
                }
            }
        }
        // if it's not a player block (function also removed the player block from the list if it is)
        if(arena.removePlayerBlock(event.getBlock()) == false){
            // cancel (leave the block in the game)
          event.setCancelled(true);
        } else {
            // Silverfish spawns if Andesite is mined by a player.
            if (event.getBlock().getType() == Material.STONE && event.getBlock().getData() == (byte) 5) {
                Block andesite = event.getBlock();
                if (andesite.hasMetadata("player")) {
                    Location andesiteLoc = andesite.getLocation();
                    andesiteLoc.add(new Vector(0.5, 0.5, 0.5));
                    event.setCancelled(true);
                    andesite.setType(Material.AIR);
                    Player placedBy = getBlockPlacedPlayer(andesite);
                    Team team = arena.getTeam(placedBy.getUniqueId());
                    if (team != null) {
                        team.spawnTeamSilverfish(andesiteLoc, placedBy.getName());
                    }
                   // Silverfish silverfish = andesiteLoc.getWorld().spawn(andesiteLoc, Silverfish.class);
                }

            }
            // TODO if it's a trap block make sure it dissapears and cant be picked up, use peters code.
            if(event.getBlock().getType() == Material.STONE_PLATE || event.getBlock().getType() == Material.IRON_PLATE || event.getBlock().getType() == Material.WOOD_PLATE ) {
                Block plate = event.getBlock();
                event.setCancelled(true);
                plate.setType(Material.AIR);
            }

        }
        //  ***NOTE - THE SILVERFISH will only have vanilla AI, in other words it will attack anybody, regardless of team.  This is fine.
        //  Note - All surviving entities (e.g. mobs, iron, gold, ems etc) should be removed at the end of the game.
    }

   // next few are self explanatory & you made them anyway :)
    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        for (Block block : new ArrayList<Block>(event.blockList())) {
            Arena arena = main.getArenaManager().getFirstArena();
          //  System.out.println("Entity exploded type = " + block.getType().toString());
            if (block.getType() == Material.BED_BLOCK) {
                event.blockList().remove(block);
                System.out.println("Block was bed, preventing its destuction");
            } else if (!arena.isPlayerBlock(block)) {
                event.blockList().remove(block);
            }
        }
    }

    // next few are self explanatory & you made them anyway :)
    @EventHandler
    public void sssss(PlayerDeathEvent e) {
        Player p = e.getEntity();
        System.out.println("death cause " + e.getEntity().getLastDamageCause().getCause().toString());

    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {

        LivingEntity entity = event.getEntity();

        // getType() is inherited from Entity
        if(entity.getType() == EntityType.IRON_GOLEM) {
            System.out.println("Golem was killed");
            Arena arena = main.getArenaManager().getFirstArena();
            arena.golemDied((CraftIronGolem) event.getEntity());
        }
        if(entity.getType() == EntityType.SILVERFISH) {
            System.out.println("Silverfish was killed");
            Arena arena = main.getArenaManager().getFirstArena();
            arena.silverfishDied((CraftSilverfish) event.getEntity());
        }

    }

    // next few are self explanatory & you made them anyway :)
    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        for (Block block : new ArrayList<Block>(event.blockList())) {
            Arena arena = main.getArenaManager().getFirstArena();
            if (block.getType() == Material.BED_BLOCK) {
                event.blockList().remove(block);
                System.out.println("XX Block was bed, preventing its destuction");
            } else if (!arena.isPlayerBlock(block)) {
                event.blockList().remove(block);
            }
        }
    }

    @EventHandler
    public void onIgnite(BlockIgniteEvent event) {
        Arena arena = main.getArenaManager().getFirstArena();
        if(!arena.isPlayerBlock(event.getBlock())){
            event.setCancelled(true);
            if(event.getBlock().getType() == Material.FIRE) {
                event.getBlock().setType(Material.AIR);
            }
        }
    }

    @EventHandler
    public void onBurn(BlockBurnEvent event) {
        Arena arena = main.getArenaManager().getFirstArena();
        if(!arena.isPlayerBlock(event.getBlock())){
            event.setCancelled(true);
            if(event.getBlock().getType() == Material.FIRE) {
                event.getBlock().setType(Material.AIR);
            }
        }
    }

    @EventHandler
    public void onSleepTry(PlayerBedEnterEvent sleepevent) { sleepevent.setCancelled(true); }

    @EventHandler
    public void onPickupItem(PlayerPickupItemEvent event) {
        if (event.getItem().getItemStack().equals(new ItemStack(Material.EMERALD)) ||
                event.getItem().getItemStack().equals(new ItemStack(Material.DIAMOND))) {
            main.getArenaManager().getFirstArena().ifNewPlayerNotSeenDiaEmHelpMessage(event.getPlayer());
        }
    }

    // when a player places a block on a bed
    @EventHandler
    public void onPlayerInteracts(PlayerInteractEvent clickevent) {
        Player player = clickevent.getPlayer();
        Action clic = clickevent.getAction();
        Arena arena = main.getArenaManager().getArena(player);
        if (arena.getState() == GameState.LIVE) {

            // for pressure plates
            if (clickevent.getAction() == Action.PHYSICAL) {
                Block block = clickevent.getClickedBlock();
                if (block != null) {
                    // get who placed the trap from its metadata
                    Player placedBy = getBlockPlacedPlayer(block);

                    // get the team of the player who placed the trap
                    Team placedTeam = arena.getTeam(placedBy.getUniqueId());
                    // if the team that placed the block is different from the team of the player who stepped on it
                    // apply the traps effect.
                    if (!placedTeam.equals(arena.getTeam(player.getUniqueId()))) {
                        System.out.println("trap placed by other team");
                        if (block.getType() == Material.STONE_PLATE) {
                            System.out.println("launched");
                            clickevent.setCancelled(true);
                            //  If you or your team mate (check array) stood on it, stop (return) the code here, otherwise...
                            player.setVelocity(new Vector(0.14, 1.2, 0.14));
                            player.getWorld().playSound(block.getLocation(), Sound.PISTON_RETRACT, (float) 1.4, (float) (float) (1.5));
                            return;
                        }
                        if (block.getType() == Material.WOOD_PLATE && !player.hasPotionEffect(PotionEffectType.POISON)) {
                            System.out.println("poisoned");
                            clickevent.setCancelled(true);
                            //  If you or your team mate (check array) stood on it, stop (return) the code here, otherwise...
                            player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 240, 1));
                            player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 170, 0));
                            player.getWorld().playSound(block.getLocation(), Sound.PISTON_RETRACT, (float) 1.4, (float) (float) (2));
                            return;
                        }
                        if (block.getType() == Material.IRON_PLATE && !player.hasPotionEffect(PotionEffectType.CONFUSION)) {
                            System.out.println("blindsided");
                            clickevent.setCancelled(true);
                            //  If you or your team mate (check array) stood on it, stop (return) the code here, otherwise...
                            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 300, 2));
                            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 300, 2));
                            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 300, 0));
                            player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 170, 0));
                            player.getWorld().playSound(block.getLocation(), Sound.PISTON_RETRACT, (float) 1.4, (float) (float) (0.5));
                            return;
                        }
                    } else {
                        System.out.println("trap placed by own team");
                    }
                }

                return;
            }

            //  Shoots fireball (note that explosion radius is dealt with in another event).
            if (((clic == Action.RIGHT_CLICK_BLOCK) || (clic == Action.RIGHT_CLICK_AIR)) && (player.getInventory().getItemInHand() != null)) {
                if ((player.getInventory().getItemInHand().getType() == Material.FIREBALL)) {
                    player.launchProjectile(Fireball.class, player.getLocation().getDirection());
                    if (player.getInventory().getItemInHand().getAmount() == 1) {
                        //  If the player only has one item in his hand set it to null
                        player.getInventory().setItemInHand(null);
                        System.out.println("last block placed setting to null");
                    } else {
                        //  If more than one item reduces your inventory amount by one.
                        System.out.println("not last block placed, reducing by one");
                        player.getInventory().getItemInHand().setAmount(player.getItemInHand().getAmount() - 1);
                    }
                    return;
                }

                if ((player.getInventory().getItemInHand().getType() == Material.EGG)) {
                    System.out.println("monster egg used");
                    if (makeGolem(player)) {
                      //  GameUtils.reduceInHandByOne(player);
                    }
                }

                int slot = player.getInventory().getHeldItemSlot();
                if (slot == 8) {
                    //open game menu
                    GameInventory gameInventory = new GameInventory(main);
                    player.openInventory(gameInventory.getGameInventory(player));
                    clickevent.setCancelled(true);
                }
            }

            if (clickevent.getClickedBlock() == null) {
                return;
            }

            Location blockloc = clickevent.getClickedBlock().getLocation();
            Location blockabove = blockloc.add(0, 1, 0);

            // i fthey just have air in their hand do nothing
            if (clickevent.getClickedBlock().getType() == Material.BED_BLOCK && player.getInventory().getItemInHand().getType() == Material.AIR && (clic == Action.RIGHT_CLICK_BLOCK)) {
                clickevent.setCancelled(true);
                return;
            }

            // if they have clciked on a bed block and have a valid material in their hand and the block above
            // where they clicked is air.
            if (clickevent.getClickedBlock().getType() == Material.BED_BLOCK && (clic == Action.RIGHT_CLICK_BLOCK) &&
                    (player.getInventory().getItemInHand().getType() == Material.WOOL ||
                            player.getInventory().getItemInHand().getType() == Material.WOOD ||
                            player.getInventory().getItemInHand().getType() == Material.STAINED_GLASS ||
                            player.getInventory().getItemInHand().getType() == Material.OBSIDIAN) &&
                    (blockabove.getBlock().getType().equals(Material.AIR))) {

               //Arena arena = main.getArenaManager().getFirstArena();
                // set the block above the bed to the same type as they had in their hand.
                blockabove.getBlock().setType(player.getItemInHand().getType());
                // if it's wool or stained glass need to get the color
                if (player.getInventory().getItemInHand().getType() == Material.WOOL || player.getInventory().getItemInHand().getType() == Material.STAINED_GLASS) {
                    // need to get color from team which is a few class jumps away
                    // first get the arena given the player, then get the team from the arena also given the player
                    // finally get the colour as an int form the team
                    int teamColorInt = main.getArenaManager().getArena(player).getTeam(player.getUniqueId()).getTeamColorInt();
                    // set blockabove to team color wool/glass
                    blockabove.getBlock().setData((byte) teamColorInt);
                }
                // add the placed block to the player block list so it can be removed
                arena.addPlayerBlock(blockabove.getBlock());

                if (player.getInventory().getItemInHand().getAmount() == 1) {
                    //  If the player only has one item in his hand set it to null
                    player.getInventory().setItemInHand(null);
                    System.out.println("last block placed setting to null");
                } else {
                    //  If more than one item reduces your inventory amount by one.
                    System.out.println("not last block placed, reducing by one");
                    player.getInventory().getItemInHand().setAmount(player.getItemInHand().getAmount() - 1);
                }

                System.out.println("Placed item on bed");
                clickevent.setCancelled(true);
                return;
            }

        }
    }

    // Spawns a Blaze at the passed location & returns the Blaze variable
    public boolean makeGolem(Player player) {
        Arena arena = main.getArenaManager().getArena(player);
        Block block = player.getTargetBlock((HashSet<Byte>) null, 2);

        if (block == null) {
            System.out.println("Block is null");
            return false;
        } else {
            System.out.println("block type " + block.getType().toString());
            Location locInFront = player.getTargetBlock((HashSet<Byte>) null, 2).getLocation();
            Location spawnLocation = getGolemSpawnLocation(locInFront.add(0.5,0,0.5));


            if (spawnLocation == null) {
                player.sendMessage(ChatColor.RED + "Invalid spawn location!");
                return false;
            }
            System.out.println(spawnLocation.getX() + " " + spawnLocation.getZ() + " " + spawnLocation.getY());
            Team team = arena.getTeam(player.getUniqueId());
            if (team != null) {
                team.spawnTeamGolem(spawnLocation, player.getName());
                return true;
            }
            return false;
        }
    }

    // checks the passed location is valid for spawning a golem, also looks a block up or down.
    private Location getGolemSpawnLocation(Location locInFront) {
        // one block down from front of player
        if (locInFront.clone().add(0, -2, 0).getBlock().getType() != Material.AIR &&
                locInFront.clone().add(0, -1, 0).getBlock().getType() == Material.AIR &&
                locInFront.clone().getBlock().getType() == Material.AIR &&
                locInFront.clone().add(0, 1, 0).getBlock().getType() == Material.AIR) {
           // System.out.println("one block down");
                return locInFront.clone().add(0, -1, 0);
        }
        // the block in front of the player
        if (locInFront.clone().add(0, -1, 0).getBlock().getType() != Material.AIR &&

                locInFront.clone().getBlock().getType() == Material.AIR &&
                locInFront.clone().add(0, 1, 0).getBlock().getType() == Material.AIR &&
                locInFront.clone().add(0, 2, 0).getBlock().getType() == Material.AIR) {
          //  System.out.println("in front");
            return locInFront.clone();
        }
        // one block up in front of player
        if (locInFront.clone().getBlock().getType() != Material.AIR &&

                locInFront.clone().add(0, 1, 0).getBlock().getType() == Material.AIR &&
                locInFront.clone().add(0, 2, 0).getBlock().getType() == Material.AIR &&
                locInFront.clone().add(0, 3, 0).getBlock().getType() == Material.AIR) {
         //   System.out.println("one block up");
            return locInFront.clone().add(0, 1, 0);
        }
        return null;
    }

    // handles inventory click, used mostly for shops
    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        // clicking outside inventory will cause this,
        if (e.getClickedInventory() == null) {
            System.out.println("Clicked inventory is null");
            return;
        }
        Player player = (Player) e.getWhoClicked();
        GameState state = main.getArenaManager().getArena(player).getState();
        if (e.getSlotType().equals(InventoryType.SlotType.ARMOR) || state == GameState.COUNTDOWN || state == GameState.RECRUITING) {
            // dot want folk moving armour or doing anything with inventory in lobby
            e.setCancelled(true);
            // now check for map vote
                // below here fires the item click event I've moved to the shopentites class for each of the different shop types
                if (ChatColor.translateAlternateColorCodes('&', e.getClickedInventory().getTitle())
                        .equals("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "VOTE FOR A MAP.") &&
                        e.getCurrentItem() != null) {
                    main.getArenaManager().getArena(player).mapVote(player, e.getRawSlot());
                    // the shop click function handles what to do depending on the slot that was clicked
                   // lobby.getGameManager().gameChosenFromInventory(player, e.getRawSlot());
                }
        }

        if (e.getClickedInventory().getType().equals(InventoryType.CRAFTING)) {
            // cancels the event, which makes it so when the player tries to take an item from crafting it, it won't let the player take it.
            e.setCancelled(true);
            player.sendMessage(ChatColor.RED + "Crafting is disabled on this server!");
        }

        GameState s = main.getArenaManager().getArena(player).getState();

        if (s== GameState.LIVE || s == GameState.FINISHING ) {
            // in the game
            // not clicked in a shop inventory, no need to do anything more in this function
            if (ChatColor.translateAlternateColorCodes('&', e.getClickedInventory().getTitle()).equals(null)) {
                System.out.println("Clicked inventory title is null");
                return;
            }

            // below here fires the item click event I've moved to the shopentites class for each of the different shop types
            if (ChatColor.translateAlternateColorCodes('&', e.getClickedInventory().getTitle())
                    .equals("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "GAME MENU.") &&
                    e.getCurrentItem() != null) {
                e.setCancelled(true);
            }

            if (e.getCurrentItem().getType().equals(Material.NETHER_STAR)) {
                // game menu netherstar
                System.out.println("NOOOOOOOOOO");
                e.setCancelled(true);
            }

            ShopEntities shop = new ShopEntities(main);

            if (ChatColor.translateAlternateColorCodes('&', e.getClickedInventory().getTitle())
                    .equals("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "GAME MENU.") &&
                    e.getCurrentItem() != null) {
                if (e.getRawSlot() == 22) {
                    // leave the game
                    player.sendTitle("", "");
                    ByteArrayDataOutput out = ByteStreams.newDataOutput();
                    out.writeUTF("Connect");
                    out.writeUTF("Lobby");
                    player.sendPluginMessage(main, "BungeeCord", out.toByteArray());
                }
            }

            // this is what you made but split up a bit into different classes
            // when the player clicks in the main item shop
            // the switch case will oen an inventory as defined in the shop entities class
            if (ChatColor.translateAlternateColorCodes('&', e.getClickedInventory().getTitle())
                    .equals("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "ITEM SHOP - MAIN MENU.") &&
                    e.getCurrentItem() != null) {
                e.setCancelled(true);


                switch (e.getRawSlot()) {
                    case 20:
                        //WEAPONS
                        player.openInventory(shop.getWeaponShop(player));
                        break;
                    case 21:
                        //ARMOUR
                        player.openInventory(shop.getArmourShop(player));
                        break;
                    case 22:
                        player.openInventory(shop.getToolShop(player));
                        break;
                    case 23:
                        // this inventory needs the team color to show the wool & glass properly
                        // need to get color from team which is a few class jumps away
                        // first get the arena given the player, then get the team from the arena also given the player
                        // finally get the colour as an int form the team
                        int teamColorInt = main.getArenaManager().getArena(player).getTeam(player.getUniqueId()).getTeamColorInt();
                        player.openInventory(shop.getBlockShop(player, teamColorInt));
                        break;
                    case 24:
                        //BLOCK TRAPS
                        player.openInventory(shop.getBlockTrapShop(player));
                        break;
                    case 28:
                        // STONE SWORD
                        shop.BuyStoneSword(player);
                        break;
                    case 37:
                        //IRON SWORD;
                        shop.BuyIronSword(player);
                        break;
                    case 38:
                        // WOOL BLOCKS
                        int teamColorInt2 = main.getArenaManager().getArena(player).getTeam(player.getUniqueId()).getTeamColorInt();
                        shop.BuyWoolBlock(player, teamColorInt2);
                        break;
                    case 39:
                        // STONE PICK
                        shop.BuyStonePick(player);
                        break;
                    case 40:
                        // BOW
                        shop.BuyBow(player);
                        break;
                    case 41:
                        // ARROWS
                        shop.BuyArrows(player);
                        break;
                    case 42:
                        // GAPPLE
                        shop.BuyGapple(player, "armoury.island-gapple");
                        break;
                    case 43:
                        // MILK
                        shop.BuyMilk(player);
                        break;
                    case 34:
                        // PEARL
                        shop.BuyPearl(player);
                        break;
                    default:
                        return;


                }
            }

            // below here fires the item click event I've moved to the shopentites class for each of the different shop types
            if (ChatColor.translateAlternateColorCodes('&', e.getClickedInventory().getTitle())
                    .equals("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "WEAPONS MENU.") &&
                    e.getCurrentItem() != null) {
                e.setCancelled(true);
              //  Player player = (Player) e.getWhoClicked();
                // the shop click function handles what to do depending on the slot that was clicked
                shop.WeaponShopItemClick(player, e.getRawSlot());
            }

            if (ChatColor.translateAlternateColorCodes('&', e.getClickedInventory().getTitle())
                    .equals("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "ARMOUR MENU.") &&
                    e.getCurrentItem() != null) {
                e.setCancelled(true);
              //  Player player = (Player) e.getWhoClicked();
                // the shop click function handles what to do depending on the slot that was clicked
                shop.ArmourShopItemClick(player, e.getRawSlot());
            }

            if (ChatColor.translateAlternateColorCodes('&', e.getClickedInventory().getTitle())
                    .equals("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "TOOL MENU.") &&
                    e.getCurrentItem() != null) {
                e.setCancelled(true);
              //  Player player = (Player) e.getWhoClicked();
                // the shop click function handles what to do depending on the slot that was clicked
                shop.ToolShopItemClick(player, e.getRawSlot());
            }

            if (ChatColor.translateAlternateColorCodes('&', e.getClickedInventory().getTitle())
                    .equals("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "BLOCK MENU.") &&
                    e.getCurrentItem() != null) {
                e.setCancelled(true);
              //  Player player = (Player) e.getWhoClicked();
                // get the color code for the team as used to dye item stacks
                int teamColorInt = main.getArenaManager().getArena(player).getTeam(player.getUniqueId()).getTeamColorInt();
                // the shop click function handles what to do depending on the slot that was clicked
                shop.BlockShopItemClick(player, e.getRawSlot(), teamColorInt);
            }

            if (ChatColor.translateAlternateColorCodes('&', e.getClickedInventory().getTitle())
                    .equals("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "BLOCK TRAPS MENU.") &&
                    e.getCurrentItem() != null) {
                e.setCancelled(true);
             //   Player player = (Player) e.getWhoClicked();
                // the shop click function handles what to do depending on the slot that was clicked
                shop.TrapShopItemClick(player, e.getRawSlot());
            }

            if (ChatColor.translateAlternateColorCodes('&', e.getClickedInventory().getTitle())
                    .equals("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "UPGRADES MENU.") &&
                    e.getCurrentItem() != null) {
                e.setCancelled(true);
              //  Player player = (Player) e.getWhoClicked();
                // i need the players team in the click event so I can upgrade the correct teams item (speed etc)
                Team team = main.getArenaManager().getArena(player).getTeam(player.getUniqueId());
                // the shop click function handles what to do depending on the slot that was clicked and the team it is for
                shop.UpgradeShopItemClick(player, e.getSlot(), team);
            }

            if (ChatColor.translateAlternateColorCodes('&', e.getClickedInventory().getTitle())
                    .equals("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Hold target item in hand.") &&
                    e.getCurrentItem() != null) {
                e.setCancelled(true);
              //  Player player = (Player) e.getWhoClicked();
                if ((player.getInventory().getItemInHand().getType() == null)) {
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.RED + "Hold the item that you want to enchant, in your hand.");
                    return;
                }
                // the shop click function handles what to do depending on the slot that was clicked
                shop.EnchantShopItemClick(player, e.getRawSlot());
            }

            if (ChatColor.translateAlternateColorCodes('&', e.getClickedInventory().getTitle())
                    .equals("" + ChatColor.DARK_GRAY + ChatColor.BOLD + "The ARMOURER menu.") &&
                    e.getCurrentItem() != null) {
                e.setCancelled(true);
             //   Player player = (Player) e.getWhoClicked();
                // the shop click function handles what to do depending on the slot that was clicked
                shop.ArmourerShopItemClick(player, e.getRawSlot());
            }
        }
    }

    // prevent player dropping things in the lobby with q button
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event){
        Player player = event.getPlayer();
        GameState s = main.getArenaManager().getFirstArena().getState();

        if (s== GameState.COUNTDOWN || s == GameState.RECRUITING ) {
            event.setCancelled(true);
        } else {
            if (event.getItemDrop().getItemStack().getType() == Material.NETHER_STAR)  {
            event.setCancelled(true);
            }
        }
    }

    // handles what happens when a player clicks on a shop keeper
    @EventHandler
    public void onPlayerInteractAtEntity (PlayerInteractAtEntityEvent eVill) {
        Player player = eVill.getPlayer();
        if (eVill.getRightClicked() == null) { return; }
        Entity entity = eVill.getRightClicked();

        ShopEntities shop = new ShopEntities(main);

        if (entity.getType() == EntityType.VILLAGER) {
            // open the shop inventory found in the ShopEntity's class
            int teamColorInt = main.getArenaManager().getArena(player).getTeam(player.getUniqueId()).getTeamColorInt();
            player.openInventory(shop.getVillagerShop(player, teamColorInt));
        }

        if (entity.getType() == EntityType.BLAZE) {
            Arena arena = main.getArenaManager().getArena(player);
            Team team = arena.getTeam(player.getUniqueId());
            // open the upgrades inventory found in the ShopEntity's class
            // it needs the team so it won't show upgrades they already have
            if (team != null) {
                player.openInventory(shop.getBlazeShop(player, team));
            }
        }

        if (entity.getType() == EntityType.WITCH) {
            // open the enchantress inventory found in the ShopEntity's class
            player.openInventory(shop.getWitchShop(player));
        }

        if (entity.getType() == EntityType.PIG_ZOMBIE) {
            // open the armourer inventory found in the ShopEntity's class
            player.openInventory(shop.getPigZombieShop(player));
        }
    }

    // prevent certain types if inventory interaction
    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent e) {
        System.out.println("Inventory opened type = " + e.getInventory().getType().toString());
        Player player = (Player) e.getPlayer();
        if(e.getInventory().getType() == InventoryType.MERCHANT){
            System.out.println("Inventory type merchant");
            e.setCancelled(true);
            // this is where i was having the first click on villager problem.
            // having this event open the sop inventory fixed it for me and didn't seem to have any
            // ill effects for you who didn't have the issue.
            int teamColorInt = main.getArenaManager().getArena(player).getTeam(player.getUniqueId()).getTeamColorInt();
            player.openInventory(ShopEntities.getVillagerShop(player, teamColorInt));
        }
        if(e.getInventory().getType() == InventoryType.ANVIL){
            e.setCancelled(true);
        }
        if(e.getInventory().getType() == InventoryType.BEACON){
            e.setCancelled(true);
        }
        if(e.getInventory().getType() == InventoryType.WORKBENCH){
            e.setCancelled(true);
        }
    }

    // this is copied from your code & fairly untested.
    @EventHandler
    public void onProjectileHit(ProjectileHitEvent projectileHitEvent) {
        Location landingSpot = projectileHitEvent.getEntity().getLocation();
        if (projectileHitEvent.getEntity().getShooter() instanceof Player) {
            Player player = (Player) projectileHitEvent.getEntity().getShooter();

            // SETS *ADDITIONAL* FIREBALL POWER (radius).
            if (projectileHitEvent.getEntity().getType() == EntityType.FIREBALL) {
                landingSpot.getWorld().createExplosion(landingSpot, 2);
            }

            // SPAWNS SILVERFISH WHERE SNOWBALLS LAND.
            if (projectileHitEvent.getEntity().getType() == EntityType.SNOWBALL) {
                Snowball sn = (Snowball)projectileHitEvent.getEntity();
                if(player != null) {
                    System.out.println("snowball fired by " + player.getDisplayName() + " landed & spawned silverfish");
                    Arena arena = main.getArenaManager().getArena(player);
                    Team team = arena.getTeam(player.getUniqueId());
                    if (team != null) {
                        team.spawnTeamSilverfish(landingSpot, player.getName());
                    }
                }
            }
        }
    }

    // prevent hunger depletion
    @EventHandler
    public void onHungerDeplete(FoodLevelChangeEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Player p = (Player) e.getEntity();
        e.setCancelled(true);
        p.setFoodLevel(20);
    }

    @EventHandler
    public void onLobbyCLick(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        GameState s = main.getArenaManager().getArena(player).getState();

        if (s== GameState.RECRUITING || s == GameState.COUNTDOWN ) {
            // in the lobby
            Action action = e.getAction();
            // only listening for right click
            if (action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR) {
                int slot = player.getInventory().getHeldItemSlot();

                if (slot == 0) {
                    //open map vote inventory
                    MapVoteInventory mapVoteInventory = new MapVoteInventory(main);
                    player.openInventory(mapVoteInventory.getMapVoteInventory(player));
                } else if (slot == 8) {
                    // player leave the game (compass)
                    player.sendTitle("", "");
                    ByteArrayDataOutput out = ByteStreams.newDataOutput();
                    out.writeUTF("Connect");
                    out.writeUTF("Lobby");
                    player.sendPluginMessage(main, "BungeeCord", out.toByteArray());
                } else if (slot == 7) { // op start the game
                    if (player.isOp()) {
                        Arena arena = main.getArenaManager().getArena(player);
                        arena.start();
                    }
                } else if (slot == 6) { // op simulate player join for queue test
                    if (player.isOp()) {
                        Arena arena = main.getArenaManager().getArena(player);
                        arena.simulatePlayerJoinForQueue(player);
                    }
                } //else {
                   // main.getArenaManager().getArena(player).mapVote(player, slot);
                    // Event setCancelled is already used in the blockplace event.
                    // e.setCancelled(true);
                //}
            }
        }
    }

    @EventHandler
    public void onCollect(PlayerPickupItemEvent e) {
        Player player = e.getPlayer();
        Arena arena = main.getArenaManager().getFirstArena();
        if (arena.IsPlayerOutOfGame(player.getUniqueId())) {
            e.setCancelled(true);
        }
    }

    // i dont think this is ever fired,  golem & silverfish targets are set manulally.
    @EventHandler
    public void onTarget(EntityTargetEvent e) {
        System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX entity target event fired");
        if (e.getEntity() instanceof IronGolem) {
            if (e.getTarget() instanceof Player) {
                Player player = (Player) e.getTarget();
                Arena arena = main.getArenaManager().getArena(player);
                if (arena.IsPlayerOutOfGame(player.getUniqueId())) {
                    e.setCancelled(true);
                } else {
                    Team team = arena.getTeam(player.getUniqueId());
                    if (arena.golemConfirmTarget((CraftIronGolem) e.getEntity(), team)) {
                        //  System.out.println("player on other team, target confirmed");
                    } else {
                        // System.out.println("player on same team as golem, target cancelled");
                        e.setCancelled(true);
                    }
                }
            } else {
                System.out.println("Golem targeted non player");
                e.setCancelled(true);
            }
        }
        if (e.getEntity() instanceof Silverfish) {
            if (e.getTarget() instanceof Player) {
                Player player = (Player) e.getTarget();
                Arena arena = main.getArenaManager().getArena(player);
                if (arena.IsPlayerOutOfGame(player.getUniqueId())) {
                    e.setCancelled(true);
                } else {
                    Team team = arena.getTeam(player.getUniqueId());
                    if (team != null) {
                        if (arena.silverfishConfirmTarget((CraftSilverfish) e.getEntity(), team)) {
                            //  System.out.println("player on other team, target confirmed");
                        } else {
                            // System.out.println("player on same team as golem, target cancelled");
                            e.setCancelled(true);
                        }
                    }
                }
            }
            else {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority= EventPriority.HIGHEST)
    public void onWeatherChange(WeatherChangeEvent event) {

        boolean rain = event.toWeatherState();
        if(rain)
            event.setCancelled(true);
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onThunderChange(ThunderChangeEvent event) {

        boolean storm = event.toThunderState();
        if(storm)
            event.setCancelled(true);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        Arena arena = main.getArenaManager().getFirstArena();
        if (arena.IsPlayerOutOfGame(player.getUniqueId())) {
            if (e.getTo().getY() < (arena.GetFloorY() + 20)) {
                Location location = player.getLocation();
                location.add(0, 1, 0);
                player.teleport(location);
                player.setFlying(true);
            }
        }
    }



}
