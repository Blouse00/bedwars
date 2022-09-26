package com.stewart.bedwars.utils;

import com.stewart.bedwars.Bedwars;
import com.stewart.bedwars.instance.Arena;
import com.stewart.bedwars.instance.PlayerItemInventory;
import com.stewart.bedwars.team.Team;
import net.minecraft.server.v1_8_R3.EntityLiving;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

// This is a fairly large class, it contains most of the stuff I chopped out of your original listener code.
// as you made it I'll have fewer comments here.
public class ShopEntities {

    private Bedwars main;

    YamlConfiguration pricesConfig;

    public ShopEntities(Bedwars main) {
        this.main = main;
        File file = new File(main.getDataFolder(), "prices.yml");
        pricesConfig =  YamlConfiguration.loadConfiguration(file);
    }

    // Spawns a villager at the passed location & returns the villager variable
    public  static Villager makeVillager(Location location) {
        Villager villager = location.getWorld().spawn(location, Villager.class);
        setAI(villager, false);
        net.minecraft.server.v1_8_R3.Entity znms = ((CraftEntity) villager).getHandle();
        NBTTagCompound ztag = new NBTTagCompound();
        znms.c(ztag);
        ztag.setBoolean("Silent", true);
        znms.f(ztag);
        return villager;
    }

    // Spawns a Blaze at the passed location & returns the Blaze variable
    public static Blaze makeBlaze(Location location) {
        Blaze blaze = location.getWorld().spawn(location, Blaze.class);
        setAI(blaze, false);
        net.minecraft.server.v1_8_R3.Entity znms = ((CraftEntity) blaze).getHandle();
        NBTTagCompound ztag = new NBTTagCompound();
        znms.c(ztag);
        ztag.setBoolean("Silent", true);
        znms.f(ztag);
        return  blaze;
    }

    // Spawns a Witch at the passed location & returns the Witch variable
    public  static Witch makeWitch(Location location) {
        Witch witch = location.getWorld().spawn(location, Witch.class);
        setAI(witch, false);
        net.minecraft.server.v1_8_R3.Entity znms = ((CraftEntity) witch).getHandle();
        NBTTagCompound ztag = new NBTTagCompound();
        znms.c(ztag);
        ztag.setBoolean("Silent", true);
        znms.f(ztag);
        return witch;
    }

    // Spawns a PigZombie at the passed location & returns the PigZombie variable
    public static PigZombie makePigZombie(Location location) {
        PigZombie pigZombie = location.getWorld().spawn(location, PigZombie.class);
        if(pigZombie.isBaby()) pigZombie.setBaby(false);
        setAI(pigZombie, false);
        net.minecraft.server.v1_8_R3.Entity znms = ((CraftEntity) pigZombie).getHandle();
        NBTTagCompound ztag = new NBTTagCompound();
        znms.c(ztag);
        ztag.setBoolean("Silent", true);
        znms.f(ztag);
        return pigZombie;
    }

    // turn off the passed entities AI
    private static void setAI(LivingEntity entity, boolean hasAi) {
        EntityLiving handle = ((CraftLivingEntity) entity).getHandle();
        handle.getDataWatcher().watch(15, (byte) (hasAi ? 0 : 1));
    }


    //-------------------SHOP INVENTORIES---------------

    // the inventory that is returned to the listener class when it asks for the villager shop
    public static Inventory getVillagerShop(Player player) {
        Inventory inv = Bukkit.createInventory(player, 54, "" + ChatColor.DARK_GRAY + ChatColor.BOLD + "ITEM SHOP - MAIN MENU.");

        //GOLD SWORD - WEAPONS
        ItemStack weapons = new ItemStack(Material.GOLD_SWORD);
        ItemMeta weaponsMeta = weapons.getItemMeta();
        weaponsMeta.setDisplayName(ChatColor.BLUE + "Weapon menu");
        weaponsMeta.setLore(Arrays.asList(ChatColor.GRAY + "Click here to view weapons for sale."));
        weapons.setItemMeta(weaponsMeta);
        inv.setItem(20, weapons);

        //GOLD CHAIN - ARMOUR
        ItemStack armour = new ItemStack(Material.GOLD_CHESTPLATE);
        ItemMeta armourMeta = armour.getItemMeta();
        armourMeta.setDisplayName(ChatColor.BLUE + "Armour menu");
        armourMeta.setLore(Arrays.asList(ChatColor.GRAY + "Click here to view armour for sale."));
        armour.setItemMeta(armourMeta);
        inv.setItem(21, armour);

        //GOLD PICK - TOOLS
        ItemStack tools = new ItemStack(Material.GOLD_PICKAXE);
        ItemMeta toolsMeta = tools.getItemMeta();
        toolsMeta.setDisplayName(ChatColor.BLUE + "Tool menu");
        toolsMeta.setLore(Arrays.asList(ChatColor.GRAY + "Click here to view tools for sale."));
        tools.setItemMeta(toolsMeta);
        inv.setItem(22, tools);

        //WOOD BLOCK - BLOCKS
        ItemStack blocks = new ItemStack(Material.WOOD);
        ItemMeta blocksMeta = blocks.getItemMeta();
        blocksMeta.setDisplayName(ChatColor.BLUE + "Block menu");
        blocksMeta.setLore(Arrays.asList(ChatColor.GRAY + "Click here to view blocks for sale."));
        blocks.setItemMeta(blocksMeta);
        inv.setItem(23, blocks);

        //WOODEN PRESSURE PLATE - FLOOR TRAPS
        ItemStack traps = new ItemStack(Material.WOOD_PLATE);
        ItemMeta trapsMeta = traps.getItemMeta();
        trapsMeta.setDisplayName(ChatColor.BLUE + "Block-trap / floor-trap menu");
        trapsMeta.setLore(Arrays.asList(ChatColor.GRAY + "Click here to view placeable traps."));
        traps.setItemMeta(trapsMeta);
        inv.setItem(24, traps);

        //FRAME
        ItemStack frame = new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.BLACK.getData());
        ItemMeta frameMeta = frame.getItemMeta();
        frameMeta.setDisplayName("");
        for (int i : new int[]{0,1,2,3,4,5,6,7,8,9,17,18,26,27,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53})
            inv.setItem(i, frame);

        return inv;
    }

    // the inventory that is returned to the listener class when it asks for the upgrade shop
    public  Inventory getBlazeShop(Player player, Team team) {
        Inventory inv = Bukkit.createInventory(player, 54, "" + ChatColor.DARK_GRAY + ChatColor.BOLD + "UPGRADES MENU.");

        //GOLD SUMMONER 1
        // only show this option if the team does not already have it
        if (team.isGoldSummonerActive() == false) {
            ItemStack goldsummoner1 = new ItemStack(Material.GOLD_INGOT);
            ItemMeta goldsummoner1Meta = goldsummoner1.getItemMeta();
            String cost = getPrice("upgrades.gold-summoner", true);
            goldsummoner1Meta.setDisplayName(ChatColor.GOLD + "Gold summoner - cost " +  cost);
            goldsummoner1Meta.setLore(Arrays.asList(ChatColor.GRAY + "Click here to for team summoner " + ChatColor.GOLD + " GOLD upgrade."));
            goldsummoner1.setItemMeta(goldsummoner1Meta);
            inv.setItem(20, goldsummoner1);
        }

        //DIAMOND SUMMONER 1
        // only show this option if the team does not already have it
        if (team.isDiamondSummonerActive() == false) {
            ItemStack diamondsummoner1 = new ItemStack(Material.DIAMOND);
            ItemMeta diamondsummoner1Meta = diamondsummoner1.getItemMeta();
            String cost = getPrice( "upgrades.diamond-summoner", true);
            diamondsummoner1Meta.setDisplayName(ChatColor.GOLD + "Diamond summoner - cost " +  cost);
            diamondsummoner1Meta.setLore(Arrays.asList(ChatColor.GRAY + "Click here to for team summoner " + ChatColor.BLUE + " DIAMOND upgrade."));
            diamondsummoner1.setItemMeta(diamondsummoner1Meta);
            inv.setItem(21, diamondsummoner1);
        }

        //More Summoner SPEED
        // only show this option if the team has both DIAMOND & GOLD SUMMONER
        if (team.isDiamondSummonerActive() && team.isGoldSummonerActive() && team.isFinalSummonerActive() == false) {
            ItemStack finalSummoner = new ItemStack(Material.WATCH);
            ItemMeta finalSummonerMeta = finalSummoner.getItemMeta();
            String cost = getPrice( "upgrades.final-summoner", true);
            finalSummonerMeta.setDisplayName(ChatColor.GOLD + "Team summoner speed - cost " +  cost);
            finalSummonerMeta.setLore(Arrays.asList(ChatColor.GRAY + "Click here to for team summoner " + ChatColor.RED + " SPEED upgrade."));
            finalSummoner.setItemMeta(finalSummonerMeta);
            inv.setItem(21, finalSummoner);
        }

        //TEAM SPEED 1
        // only show this option if the teams speed is un-upgraded
        if (team.getTeamSpeed() == 0) {
            ItemStack teamspeed1 = new ItemStack(Material.BLAZE_ROD);
            ItemMeta teamspeed1Meta = teamspeed1.getItemMeta();
            String cost = getPrice( "upgrades.speed-1", true);
            teamspeed1Meta.setDisplayName(ChatColor.GOLD + "Team SPEED 1 - cost " +  cost);
            teamspeed1Meta.setLore(Arrays.asList(ChatColor.GRAY + "Click here to for team " + ChatColor.BLUE + " SPEED 1 upgrade."));
            teamspeed1.setItemMeta(teamspeed1Meta);
            inv.setItem(22, teamspeed1);
        }

        //TEAM SPEED 2
        // only show this option if the teams speed had the first upgrade
        if (team.getTeamSpeed() == 1) {
            ItemStack teamspeed2 = new ItemStack(Material.ACTIVATOR_RAIL);
            ItemMeta teamspeed2Meta = teamspeed2.getItemMeta();
            String cost = getPrice( "upgrades.speed-2", true);
            teamspeed2Meta.setDisplayName(ChatColor.GOLD + "Team SPEED 2 - cost " +  cost);
            teamspeed2Meta.setLore(Arrays.asList(ChatColor.GRAY + "Click here to for team " + ChatColor.RED + " SPEED 2 upgrade."));
            teamspeed2.setItemMeta(teamspeed2Meta);
            inv.setItem(23, teamspeed2);
        }

        //FRAME
        ItemStack frame = new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.BLACK.getData());
        ItemMeta frameMeta = frame.getItemMeta();
        frameMeta.setDisplayName("");
        for (int i : new int[]{0,1,2,3,4,5,6,7,8,9,17,18,26,27,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53})
            inv.setItem(i, frame);

        return inv;
    }

    // the inventory that is returned to the listener class when it asks for the enchantress shop
    public Inventory getWitchShop(Player player) {
        Inventory inv = Bukkit.createInventory(player, 54, "" + ChatColor.DARK_GRAY + ChatColor.BOLD + "Hold target item in hand.");

        //SHARPNESS - DIAMOND SWORD ONLY
        ItemStack sharpness = new ItemStack(Material.IRON_SWORD);
        ItemMeta sharpnessMeta = sharpness.getItemMeta();
        String cost = getPrice("enchantress.sharpness-1-sword", true);
        sharpnessMeta.setDisplayName(ChatColor.GOLD + "Sharpness - cost " +  cost);
        sharpnessMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "1. Hold a sword,",
                "2. Click here,",
                "3. Enchantress gives it " + ChatColor.BLUE + "SHARPNESS."));
        sharpness.setItemMeta(sharpnessMeta);
        inv.setItem(20, sharpness);

        //KNOCKBACK - DIAMOND SWORD ONLY
        ItemStack knockback = new ItemStack(Material.STICK);
        ItemMeta knockbackMeta = knockback.getItemMeta();
        cost = getPrice("enchantress.knockback-2-sword", true);
        knockbackMeta.setDisplayName(ChatColor.GOLD + "Knockback - cost " +  cost);
        knockbackMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "1. Hold a sword,",
                "2. Click here,",
                "3. Enchantress gives it " + ChatColor.BLUE + "KNOCKBACK."));
        knockback.setItemMeta(knockbackMeta);
        inv.setItem(21, knockback);

        //FIRE ASPECT - DIAMOND SWORD OR BOW ONLY
        ItemStack fireaspectsword = new ItemStack(Material.BLAZE_POWDER);
        ItemMeta fireaspectswordMeta = fireaspectsword.getItemMeta();
        cost = getPrice("enchantress.fire-5-sword", true);
        fireaspectswordMeta.setDisplayName(ChatColor.GOLD + "Fire Aspect - cost " +  cost);
        fireaspectswordMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "1. Hold a sword or bow,",
                "2. Click here,",
                "3. Enchantress gives it " + ChatColor.GOLD + "FIRE ASPECT."));
        fireaspectsword.setItemMeta(fireaspectswordMeta);
        inv.setItem(22, fireaspectsword);

        //POWER PUNCH - BOW ONLY
        ItemStack powerpunchbow = new ItemStack(Material.BOW);
        powerpunchbow.addEnchantment(Enchantment.ARROW_KNOCKBACK, 1);
        powerpunchbow.addEnchantment(Enchantment.ARROW_DAMAGE, 1);
        ItemMeta powerpunchbowMeta = powerpunchbow.getItemMeta();
        cost = getPrice("enchantress.power-punch-4-bow", true);
        powerpunchbowMeta.setDisplayName(ChatColor.GOLD + "Power Punch - cost " +  cost);
        powerpunchbowMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "1. Hold a bow,",
                "2. Click here,",
                "3. Enchantress gives it " + ChatColor.BLUE + "POWER PUNCH."));
        powerpunchbow.setItemMeta(powerpunchbowMeta);
        inv.setItem(23, powerpunchbow);

        //EFFICIENCY - PICK ONLY
        ItemStack efficiency = new ItemStack(Material.DIAMOND_PICKAXE);
        ItemMeta efficiencyMeta = efficiency.getItemMeta();
        cost = getPrice("enchantress.efficiency-4-mining", true);
        efficiencyMeta.setDisplayName(ChatColor.GOLD + "Efficiency - cost " +  cost);
        efficiencyMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "1. Hold a pickaxe,",
                "2. Click here,",
                "3. Enchantress gives it " + ChatColor.BLUE + "EFFICIENCY."));
        efficiency.setItemMeta(efficiencyMeta);
        inv.setItem(24, efficiency);

        //PROTECTION 1 - DIAMOND ARMOUR ONLY
        ItemStack protection1 = new ItemStack(Material.DIAMOND_CHESTPLATE);
        ItemMeta protection1Meta = protection1.getItemMeta();
        cost = getPrice("enchantress.protection-2-armour", true);
        protection1Meta.setDisplayName(ChatColor.GOLD + "Protection - cost " +  cost);
        protection1Meta.setLore(Arrays.asList(
                ChatColor.GRAY + "1. Wear armour,",
                "2. Click here,",
                "3. Enchantress gives it " + ChatColor.GOLD + "PROTECTION."));
        protection1.setItemMeta(protection1Meta);
        inv.setItem(31, protection1);

        //FRAME
        ItemStack frame = new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.BLACK.getData());
        ItemMeta frameMeta = frame.getItemMeta();
        frameMeta.setDisplayName("");
        for (int i : new int[]{0,1,2,3,4,5,6,7,8,9,17,18,26,27,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53})
            inv.setItem(i, frame);

        return inv;
    }

    // the inventory that is returned to the listener class when it asks for the armourer shop
    public Inventory getPigZombieShop(Player player) {
        Inventory inv = Bukkit.createInventory(player, 54, "" + ChatColor.DARK_GRAY + ChatColor.BOLD + "The ARMOURER menu.");

        //TNT
        ItemStack TNT = new ItemStack(Material.TNT);
        ItemMeta TNTMeta = TNT.getItemMeta();
        String cost = getPrice("armoury.tnt", true);
        TNTMeta.setDisplayName(ChatColor.GOLD + "TNT - cost " + cost);
        TNTMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "When placed, will explode a few seconds later."));
        TNT.setItemMeta(TNTMeta);
        inv.setItem(20, TNT);

        //FIREBALL
        ItemStack fireball = new ItemStack(Material.FIREBALL);
        ItemMeta fireballMeta = fireball.getItemMeta();
        cost = getPrice("armoury.fireball", true);
        fireballMeta.setDisplayName(ChatColor.GOLD + "Fireball - cost " + cost);
        fireballMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Handle with care!"));
        fireball.setItemMeta(fireballMeta);
        inv.setItem(21, fireball);

        //TEAM GOLEM
        ItemStack golem = new ItemStack(Material.EGG);
        ItemMeta golemMeta = golem.getItemMeta();
        cost = getPrice("armoury.golem", true);
        golemMeta.setDisplayName(ChatColor.GOLD + "Golem - cost " + cost);
        golemMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Spawn for bed defence / attack."));
        golem.setItemMeta(golemMeta);
        inv.setItem(22, golem);

        //GAPPLE
        ItemStack gapple = new ItemStack(Material.GOLDEN_APPLE);
        ItemMeta gappleMeta = gapple.getItemMeta();
        cost = getPrice("armoury.gapple", true);
        gappleMeta.setDisplayName(ChatColor.GOLD + "Golden Apple - cost " + cost);
        gappleMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Eat when you want to recover health."));
        gapple.setItemMeta(gappleMeta);
        inv.setItem(23, gapple);

        //WATERBUCKET
        ItemStack bucket = new ItemStack(Material.WATER_BUCKET);
        ItemMeta bucketMeta = bucket.getItemMeta();
        cost = getPrice("armoury.water-bucket", true);
        bucketMeta.setDisplayName(ChatColor.GOLD + "Water Bucket - cost " + cost);
        bucketMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Useful for bed defence / attack, etc."));
        bucket.setItemMeta(bucketMeta);
        inv.setItem(24, bucket);

        //SILVERFISH
        ItemStack snowball = new ItemStack(Material.SNOW_BALL);
        ItemMeta snowballMeta = snowball.getItemMeta();
        cost = getPrice("armoury.silverfish", true);
        snowballMeta.setDisplayName(ChatColor.GOLD + "Silverfish - cost " + cost);
        snowballMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Silverfish will spawn where it lands, & attack enemies."));
        snowball.setItemMeta(snowballMeta);
        inv.setItem(30, snowball);

        //SPEED POTION
        ItemStack potion = new ItemStack(Material.POTION);
        ItemMeta potionMeta = potion.getItemMeta();
        cost = getPrice("armoury.speed-potion", true);
        potionMeta.setDisplayName(ChatColor.GOLD + "Speed Potion - cost " + cost);
        potionMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "When you drink, it lasts 1 min 30 seconds."));
        potion.setItemMeta(potionMeta);
        inv.setItem(31, potion);

        //HOUND

        //FRAME
        ItemStack frame = new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.BLACK.getData());
        ItemMeta frameMeta = frame.getItemMeta();
        frameMeta.setDisplayName("");
        for (int i : new int[]{0,1,2,3,4,5,6,7,8,9,17,18,26,27,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53})
            inv.setItem(i, frame);

        return inv;
    }


    //--------------SHOP SUB INVENTORIES------------------

    // the inventory that is returned to the listener class when it asks for the weapon shop
    public Inventory getWeaponShop(Player player) {
        Inventory weaponshop = Bukkit.createInventory(player, 54, "" + ChatColor.DARK_GRAY + ChatColor.BOLD + "WEAPONS MENU.");

        //STONE SWORD
        ItemStack stonesword = new ItemStack(Material.STONE_SWORD);
        ItemMeta stoneswordMeta = stonesword.getItemMeta();
        String cost = getPrice("weapons.stone-sword", true);
        stoneswordMeta.setDisplayName(ChatColor.GOLD + "Stone sword - cost " + cost);
        stoneswordMeta.setLore(Arrays.asList(ChatColor.GRAY + "Stone sword."));
        stonesword.setItemMeta(stoneswordMeta);
        weaponshop.setItem(20, stonesword);

        //IRON SWORD
        ItemStack ironsword = new ItemStack(Material.IRON_SWORD);
        ItemMeta ironswordMeta = ironsword.getItemMeta();
        cost = getPrice("weapons.iron-sword", true);
        ironswordMeta.setDisplayName(ChatColor.GOLD + "Iron sword - cost " + cost);
        ironswordMeta.setLore(Arrays.asList(ChatColor.GRAY + "Iron sword."));
        ironsword.setItemMeta(ironswordMeta);
        weaponshop.setItem(21, ironsword);

        //DIAMOND SWORD
        ItemStack diamondsword = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta diamondswordMeta = diamondsword.getItemMeta();
        cost = getPrice("weapons.diamond-sword", true);
        diamondswordMeta.setDisplayName(ChatColor.GOLD + "Diamond sword - cost " + cost);
        diamondswordMeta.setLore(Arrays.asList(ChatColor.BLUE + "Diamond sword."));
        diamondsword.setItemMeta(diamondswordMeta);
        weaponshop.setItem(22, diamondsword);

        //BOW
        ItemStack bow = new ItemStack(Material.BOW);
        ItemMeta bowMeta = bow.getItemMeta();
        cost = getPrice("weapons.bow", true);
        bowMeta.setDisplayName(ChatColor.GOLD + "Bow - cost " + cost);
        bowMeta.setLore(Arrays.asList(ChatColor.GRAY + "Bow."));
        bow.setItemMeta(bowMeta);
        weaponshop.setItem(23, bow);

        //ARROWS
        ItemStack arrows = new ItemStack(Material.ARROW);
        ItemMeta arrowsMeta = arrows.getItemMeta();
        cost = getPrice("weapons.arrows", true);
        arrowsMeta.setDisplayName(ChatColor.GOLD + "Arrows - cost " + cost);
        arrowsMeta.setLore(Arrays.asList(ChatColor.GRAY + pricesConfig.getString("weapons.arrows.amount") + " arrows."));
        arrows.setItemMeta(arrowsMeta);
        weaponshop.setItem(24, arrows);

        //FRAME
        ItemStack weaponframe = new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.BLACK.getData());
        ItemMeta frameMeta = weaponframe.getItemMeta();
        frameMeta.setDisplayName("");
        for (int i : new int[]{0,1,2,3,4,5,6,7,8,9,17,18,26,27,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53})
            weaponshop.setItem(i, weaponframe);

        return weaponshop;
    }

    // the inventory that is returned to the listener class when it asks for the armour shop
    public Inventory getArmourShop(Player player) {

        Inventory armourshop = Bukkit.createInventory(player, 54, "" + ChatColor.DARK_GRAY + ChatColor.BOLD + "ARMOUR MENU.");

        //CHAINMAIL
        ItemStack chainmail = new ItemStack(Material.CHAINMAIL_CHESTPLATE);
        ItemMeta chainmailMeta = chainmail.getItemMeta();
        String cost = getPrice("armour.chainmail", true);
        chainmailMeta.setDisplayName(ChatColor.GOLD + "Chainmail - cost " + cost);
        chainmailMeta.setLore(Arrays.asList(ChatColor.GRAY + "Chainmail armour."));
        chainmail.setItemMeta(chainmailMeta);
        armourshop.setItem(21, chainmail);

        //IRON ARMOUR
        ItemStack ironarmour = new ItemStack(Material.IRON_CHESTPLATE);
        ItemMeta ironarmourMeta = ironarmour.getItemMeta();
        cost = getPrice("armour.iron-armour", true);
        ironarmourMeta.setDisplayName(ChatColor.GOLD + "Iron armour - cost " + cost);
        ironarmourMeta.setLore(Arrays.asList(ChatColor.GRAY + "Iron armour."));
        ironarmour.setItemMeta(ironarmourMeta);
        armourshop.setItem(22, ironarmour);

        //DIAMOND ARMOUR
        ItemStack diamondarmour = new ItemStack(Material.DIAMOND_CHESTPLATE);
        ItemMeta diamondarmourMeta = diamondarmour.getItemMeta();
        cost = getPrice("armour.diamond-armour", true);
        diamondarmourMeta.setDisplayName(ChatColor.GOLD + "Diamond armour - cost " + cost);
        diamondarmourMeta.setLore(Arrays.asList(ChatColor.BLUE + "Diamond armour."));
        diamondarmour.setItemMeta(diamondarmourMeta);
        armourshop.setItem(23, diamondarmour);

        //FRAME
        ItemStack armourframe = new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.BLACK.getData());
        ItemMeta armourframeMeta = armourframe.getItemMeta();
        armourframeMeta.setDisplayName("");
        for (int i : new int[]{0,1,2,3,4,5,6,7,8,9,17,18,26,27,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53})
            armourshop.setItem(i, armourframe);
        return armourshop;

    }

    // the inventory that is returned to the listener class when it asks for the tool shop
    public Inventory getToolShop(Player player) {
        //TOOLS
        Inventory toolshop = Bukkit.createInventory(player, 54, "" + ChatColor.DARK_GRAY + ChatColor.BOLD + "TOOL MENU.");

        //STONE PICK
        ItemStack stonepick = new ItemStack(Material.STONE_PICKAXE);
        ItemMeta stonepickMeta = stonepick.getItemMeta();
        String cost = getPrice("tools.stone-pick", true);
        stonepickMeta.setDisplayName(ChatColor.GOLD + "Stone pickaxe - cost " + cost);
        stonepickMeta.setLore(Arrays.asList(ChatColor.GRAY + "Stone pickaxe."));
        stonepick.setItemMeta(stonepickMeta);
        toolshop.setItem(12, stonepick);

        //IRON PICK
        ItemStack ironpick = new ItemStack(Material.IRON_PICKAXE);
        ItemMeta ironpickMeta = ironpick.getItemMeta();
        cost = getPrice("tools.iron-pick", true);
        ironpickMeta.setDisplayName(ChatColor.GOLD + "Iron pickaxe - cost " + cost);
        ironpickMeta.setLore(Arrays.asList(ChatColor.GRAY + "Iron pickaxe."));
        ironpick.setItemMeta(ironpickMeta);
        toolshop.setItem(13, ironpick);

        //DIAMOND PICK
        ItemStack diamondpick = new ItemStack(Material.DIAMOND_PICKAXE);
        ItemMeta diamondpickMeta = diamondpick.getItemMeta();
        cost = getPrice("tools.diamond-pick", true);
        diamondpickMeta.setDisplayName(ChatColor.GOLD + "Diamond pickaxe - cost " + cost);
        diamondpickMeta.setLore(Arrays.asList(ChatColor.BLUE + "Diamond pickaxe."));
        diamondpick.setItemMeta(diamondpickMeta);
        toolshop.setItem(14, diamondpick);

        //STONE AXE
        ItemStack stoneaxe = new ItemStack(Material.STONE_AXE);
        ItemMeta stoneaxeMeta = stoneaxe.getItemMeta();
        cost = getPrice("tools.stone-axe", true);
        stoneaxeMeta.setDisplayName(ChatColor.GOLD + "Stone axe - cost " + cost);
        stoneaxeMeta.setLore(Arrays.asList(ChatColor.GRAY + "Stone axe."));
        stoneaxe.setItemMeta(stoneaxeMeta);
        toolshop.setItem(21, stoneaxe);

        //IRON AXE
        ItemStack ironaxe = new ItemStack(Material.IRON_AXE);
        ItemMeta ironaxeMeta = ironaxe.getItemMeta();
        cost = getPrice("tools.iron-axe", true);
        ironaxeMeta.setDisplayName(ChatColor.GOLD + "Iron axe - cost " + cost);
        ironaxeMeta.setLore(Arrays.asList(ChatColor.GRAY + "Iron axe."));
        ironaxe.setItemMeta(ironaxeMeta);
        toolshop.setItem(22, ironaxe);

        //DIAMOND AXE
        ItemStack diamondaxe = new ItemStack(Material.DIAMOND_AXE);
        ItemMeta diamondaxeMeta = diamondaxe.getItemMeta();
        cost = getPrice("tools.diamond-axe", true);
        diamondaxeMeta.setDisplayName(ChatColor.GOLD + "Diamond axe - cost " + cost);
        diamondaxeMeta.setLore(Arrays.asList(ChatColor.BLUE + "Diamond axe."));
        diamondaxe.setItemMeta(diamondaxeMeta);
        toolshop.setItem(23, diamondaxe);

        //SHEARS
        ItemStack shears = new ItemStack(Material.SHEARS);
        ItemMeta shearsMeta = shears.getItemMeta();
        cost = getPrice("tools.shears", true);
        shearsMeta.setDisplayName(ChatColor.GOLD + "Shears - cost " + cost);
        shearsMeta.setLore(Arrays.asList(ChatColor.GRAY + "Shears."));
        shears.setItemMeta(shearsMeta);
        toolshop.setItem(30, shears);

        //COMPASS
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta compassMeta = compass.getItemMeta();
        cost = getPrice("tools.compass", true);
        compassMeta.setDisplayName(ChatColor.GOLD + "Compass - cost " + cost);
        compassMeta.setLore(Arrays.asList(ChatColor.GRAY + "Compass."));
        compass.setItemMeta(compassMeta);
        toolshop.setItem(31, compass);

        //FRAME
        ItemStack toolframe = new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.BLACK.getData());
        ItemMeta toolframeMeta = toolframe.getItemMeta();
        toolframeMeta.setDisplayName("");
        for (int i : new int[]{0,1,2,3,4,5,6,7,8,9,17,18,26,27,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53})
            toolshop.setItem(i, toolframe);

        return toolshop;

    }

    // the inventory that is returned to the listener class when it asks for the block shop
    public  Inventory getBlockShop(Player player, int teamColorInt) {
        //BLOCKS
        Inventory blockshop = Bukkit.createInventory(player, 54, "" + ChatColor.DARK_GRAY + ChatColor.BOLD + "BLOCK MENU.");

        //WOOL
        // wool and glass get set to the passed team color int
        ItemStack wool = new ItemStack(new ItemStack(Material.WOOL, 1, (short) teamColorInt));
        ItemMeta woolMeta = wool.getItemMeta();
        String cost = getPrice("blocks.wool", true);
        woolMeta.setDisplayName(ChatColor.GOLD + "Wool - cost " + cost);
        woolMeta.setLore(Arrays.asList(ChatColor.GRAY +  pricesConfig.getString("blocks.wool.amount") + " blocks."));
        wool.setItemMeta(woolMeta);
        blockshop.setItem(20, wool);

        //GLASS
        ItemStack glass = new ItemStack(Material.STAINED_GLASS, 1, (short) teamColorInt);
        ItemMeta glassMeta = glass.getItemMeta();
        cost = getPrice("blocks.glass", true);
        glassMeta.setDisplayName(ChatColor.GOLD + "Stained glass - cost " + cost);
        glassMeta.setLore(Arrays.asList(ChatColor.GRAY + pricesConfig.getString("blocks.glass.amount") + " blocks."));
        glass.setItemMeta(glassMeta);
        blockshop.setItem(21, glass);

        //WOOD
        ItemStack wood = new ItemStack(Material.WOOD);
        ItemMeta woodMeta = wood.getItemMeta();
        cost = getPrice("blocks.wood", true);
        woodMeta.setDisplayName(ChatColor.GOLD + "Wood - cost " + cost);
        woodMeta.setLore(Arrays.asList(ChatColor.GRAY + pricesConfig.getString("blocks.wood.amount") + " blocks."));
        wood.setItemMeta(woodMeta);
        blockshop.setItem(22, wood);

        //SANDSTONE
        ItemStack sandstone = new ItemStack(Material.SANDSTONE);
        ItemMeta sandstoneMeta = sandstone.getItemMeta();
        cost = getPrice("blocks.sandstone", true);
        sandstoneMeta.setDisplayName(ChatColor.GOLD + "Sandstone - cost " + cost);
        sandstoneMeta.setLore(Arrays.asList(ChatColor.GRAY  + pricesConfig.getString("blocks.sandstone.amount") + " blocks."));
        sandstone.setItemMeta(sandstoneMeta);
        blockshop.setItem(23, sandstone);

        //STONE
        ItemStack stone = new ItemStack(Material.STONE);
        ItemMeta stoneMeta = stone.getItemMeta();
        cost = getPrice("blocks.stone", true);
        stoneMeta.setDisplayName(ChatColor.GOLD + "Stone - cost " + cost);
        stoneMeta.setLore(Arrays.asList(ChatColor.GRAY + pricesConfig.getString("blocks.stone.amount") + " blocks."));
        stone.setItemMeta(stoneMeta);
        blockshop.setItem(24, stone);

        //ENDSTONE
        ItemStack endstone = new ItemStack(Material.ENDER_STONE);
        ItemMeta endstoneMeta = endstone.getItemMeta();
        cost = getPrice("blocks.enderstone", true);
        endstoneMeta.setDisplayName(ChatColor.GOLD + "Endstone - cost " + cost);
        endstoneMeta.setLore(Arrays.asList(ChatColor.GRAY + pricesConfig.getString("blocks.enderstone.amount") + " blocks."));
        endstone.setItemMeta(endstoneMeta);
        blockshop.setItem(30, endstone);

        //OBSIDIAN
        ItemStack obsidian = new ItemStack(Material.OBSIDIAN);
        ItemMeta obsidianMeta = obsidian.getItemMeta();
        cost = getPrice("blocks.obsidian", true);
        obsidianMeta.setDisplayName(ChatColor.GOLD + "Obsidian - cost " + cost);
        obsidianMeta.setLore(Arrays.asList(ChatColor.GRAY + pricesConfig.getString("blocks.obsidian.amount") + " block."));
        obsidian.setItemMeta(obsidianMeta);
        blockshop.setItem(31, obsidian);

        //MAGIC CARPET
        ItemStack carpet = new ItemStack(new ItemStack(Material.CARPET, 1, (short) teamColorInt));
        ItemMeta carpetMeta = carpet.getItemMeta();
        cost = getPrice("blocks.carpet", true);
        carpetMeta.setDisplayName(ChatColor.GOLD + "Magic carpet - cost " + cost);
        carpetMeta.setLore(Arrays.asList(ChatColor.GRAY +  pricesConfig.getString("blocks.carpet.amount") + " block."));
        carpet.setItemMeta(carpetMeta);
        blockshop.setItem(32, carpet);

        //FRAME
        ItemStack blockframe = new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.BLACK.getData());
        ItemMeta blockframeMeta = blockframe.getItemMeta();
        blockframeMeta.setDisplayName("");
        for (int i : new int[]{0,1,2,3,4,5,6,7,8,9,17,18,26,27,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53})
            blockshop.setItem(i, blockframe);

        return blockshop;
    }

    // the inventory that is returned to the listener class when it asks for the trap shop
    public Inventory getBlockTrapShop(Player player) {
        Inventory blocktrapshop = Bukkit.createInventory(player, 54, "" + ChatColor.DARK_GRAY + ChatColor.BOLD + "BLOCK TRAPS MENU.");

        //STONEFISH
        ItemStack andesite = new ItemStack(Material.STONE);
        // The line below turns the Stone into Andesite (sets its data value).
        andesite.setDurability((short) 5 );
        ItemMeta andesiteMeta = andesite.getItemMeta();
        String cost = getPrice("traps.stonefish", true);
        andesiteMeta.setDisplayName(ChatColor.GOLD + "Stonefish - cost " + cost);
        andesiteMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Silverfish block trap - 1 block.",
                "When block is broken, a silverfish will hatch",
                "in its place, and attack the nearest player."));
        andesite.setItemMeta(andesiteMeta);
        blocktrapshop.setItem(20, andesite);

        //PRESSURE PLATE LAUNCHER
        ItemStack launcher = new ItemStack(Material.STONE_PLATE);
        ItemMeta launcherMeta = launcher.getItemMeta();
        String launcherCost = getPrice("traps.launcher", true);
        launcherMeta.setDisplayName(ChatColor.GOLD + "Launcher - cost " + launcherCost);
        launcherMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Launcher floor trap - 1 block.",
                "When stepped on will launch the player into the air."));
        launcher.setItemMeta(launcherMeta);
        blocktrapshop.setItem(21, launcher);

        //PRESSURE PLATE POISON
        ItemStack poisoner = new ItemStack(Material.WOOD_PLATE);
        ItemMeta poisonerMeta = poisoner.getItemMeta();
        String poisonerCost = getPrice("traps.poison", true);
        poisonerMeta.setDisplayName(ChatColor.GOLD + "Poisoner - cost " + poisonerCost);
        poisonerMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Poisoner floor trap - 1 block.",
                "When stepped on will poison the player."));
        poisoner.setItemMeta(poisonerMeta);
        blocktrapshop.setItem(22, poisoner);

        //PRESSURE PLATE BLINDSIDE
        ItemStack blindside = new ItemStack(Material.IRON_PLATE);
        ItemMeta blindsideMeta = blindside.getItemMeta();
        String blindsideCost = getPrice("traps.blindside", true);
        blindsideMeta.setDisplayName(ChatColor.GOLD + "Blindside - cost " + blindsideCost);
        blindsideMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Blindside floor trap - " + pricesConfig.getString("traps.blindside.amount") + " blocks.",
                "When stepped on will blind and slow the player."));
        blindside.setItemMeta(blindsideMeta);
        blocktrapshop.setItem(23, blindside);

        //FRAME
        ItemStack blocktrapframe = new ItemStack(Material.STAINED_GLASS_PANE, 1, DyeColor.BLACK.getData());
        ItemMeta blocktrapframeMeta = blocktrapframe.getItemMeta();
        blocktrapframeMeta.setDisplayName("");
        for (int i : new int[]{0,1,2,3,4,5,6,7,8,9,17,18,26,27,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53})
            blocktrapshop.setItem(i, blocktrapframe);

        return blocktrapshop;
    }

    //-----------------SHOP ITEM CLICK (Purchase item)------------------------

    // notice that in my item click functions I get an instance of the playeriteminventry class I made.
    // I use this to more easily check if the player has enough to pay for an item and to remove the items from their
    // inventory

    // Called when clicking to buy something from the weapon shop, it is passed the player and the slot number that was
    // clicked on
    public  void WeaponShopItemClick(Player player, int slot) {
        // my player inventory class
        PlayerItemInventory playerItemInventory = new PlayerItemInventory(player);
        switch (slot) {
            case 20:
                // Buy stone sword
                if (playerItemInventory.hasAmount(pricesConfig, "weapons.stone-sword", true)) {
                   // playerItemInventory.removeIron(5);
                    player.getInventory().addItem(new ItemStack(Material.STONE_SWORD, 1));
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.GREEN + "You bought a " + ChatColor.BLUE + "stone sword" + ChatColor.GREEN + " for " + getPrice("weapons.stone-sword", true));
                } else {
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.RED + "You need at least " + getPrice("weapons.stone-sword", false) + " to buy this item.");
                }
                break;
            case 21:
                // Buy iron sword
                if (playerItemInventory.hasAmount(pricesConfig, "weapons.iron-sword", true)) {
                    //playerItemInventory.removeGold(10);
                    player.getInventory().addItem(new ItemStack(Material.IRON_SWORD, 1));
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.GREEN + "You bought an " + ChatColor.BLUE + "iron sword" + ChatColor.GREEN + " for " + getPrice("weapons.iron-sword", true));
                } else {
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.RED + "You need at least " + getPrice("weapons.iron-sword", false) +" to buy this item.");
                }
                break;
            case 22:
                // Buy diamond sword
                if (playerItemInventory.hasAmount(pricesConfig, "weapons.diamond-sword", true)) {
                    //playerItemInventory.removeEmerald(5);
                    player.getInventory().addItem(new ItemStack(Material.DIAMOND_SWORD, 1));
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.GREEN + "You bought a " + ChatColor.BLUE + "diamond sword" + ChatColor.GREEN + " for " + getPrice("weapons.diamond-sword", true));
                } else {
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.RED + "You need at least " + getPrice("weapons.diamond-sword", false) + " to buy this item.");
                }
                break;
            case 23:
                // Buy bow
                if (playerItemInventory.hasAmount(pricesConfig, "weapons.bow", true)) {
                    //playerItemInventory.removeDiamond(5);
                    player.getInventory().addItem(new ItemStack(Material.BOW, 1));
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.GREEN + "You bought a " + ChatColor.BLUE + "bow" + ChatColor.GREEN + " for " + getPrice("weapons.bow", true));
                } else {
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.RED + "You need at least " + getPrice("weapons.bow", false) + " to buy this item.");
                }
                break;
            case 24:
                // Buy arrows
                if (playerItemInventory.hasAmount(pricesConfig, "weapons.arrows", true)) {
                    playerItemInventory.removeGold(5);
                    player.getInventory().addItem(new ItemStack(Material.ARROW, 8));
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.GREEN + "You bought " + pricesConfig.getString("weapons.arrows.amount") + ChatColor.BLUE + " arrows" + ChatColor.GREEN + " for " + getPrice("weapons.arrows", true));
                } else {
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.RED + "You need at least " + getPrice("weapons.arrows", false) + " to buy this item.");
                }
                break;
            default:
                return;
        }
    }

    // same as above but for all the different shops
    public  void ArmourShopItemClick(Player player, int slot) {
        PlayerItemInventory playerItemInventory = new PlayerItemInventory(player);

        switch (slot) {
            case 21:
                if (player.getInventory().getChestplate() != null && player.getInventory().getChestplate().getType().equals(Material.LEATHER_CHESTPLATE) == false) {
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.RED + "Item not bought - you're already wearing this or better!");
                    return;
                }
                if (playerItemInventory.hasAmount(pricesConfig, "armour.chainmail", true) == false) {
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.RED + "You need at least " + getPrice("armour.chainmail", false) + " to buy this item.");
                    return;
                }
               // playerItemInventory.removeIron(20);
                GameUtils.removeArmour(player);
                player.getInventory().setHelmet(new ItemStack(Material.CHAINMAIL_HELMET));
                player.getInventory().setChestplate(new ItemStack(Material.CHAINMAIL_CHESTPLATE));
                player.getInventory().setLeggings(new ItemStack(Material.CHAINMAIL_LEGGINGS));
                player.getInventory().setBoots(new ItemStack(Material.CHAINMAIL_BOOTS));
                player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.GREEN + "You bought " + ChatColor.BLUE + "chainmail armour" + ChatColor.GREEN + " for " + getPrice("armour.chainmail", true));
                break;
            case 22:
                // Buy iron armour
                if ((player.getInventory().getChestplate() != null) &&
                        (player.getInventory().getChestplate().getType().equals(Material.LEATHER_CHESTPLATE) == false &&
                                (player.getInventory().getChestplate().getType().equals(Material.CHAINMAIL_CHESTPLATE) == false))) {
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.RED + "Item not bought - you're already wearing this or better!");
                    return;
                }
                if (playerItemInventory.hasAmount(pricesConfig, "armour.iron-armour", true) == false) {
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.RED + "You need at least " + getPrice("armour.iron-armour", false) + " to buy this item.");
                    return;
                }

             //   player.getInventory().removeItem(new ItemStack(266, 30));
                GameUtils.removeArmour(player);
                player.getInventory().setHelmet(new ItemStack(Material.IRON_HELMET));
                player.getInventory().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
                player.getInventory().setLeggings(new ItemStack(Material.IRON_LEGGINGS));
                player.getInventory().setBoots(new ItemStack(Material.IRON_BOOTS));
                player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.GREEN + "You bought " + ChatColor.BLUE + "iron armour" + ChatColor.GREEN + " for " + getPrice("armour.iron-armour", true));
                break;
            case 23:
                // Buy diamond armour
                if (player.getInventory().getChestplate().getType().equals(Material.DIAMOND_CHESTPLATE))  {
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.RED + "Item not bought - you're already wearing this or better!");
                    return;
                }
                if (playerItemInventory.hasAmount(pricesConfig, "armour.diamond-armour", true) == false) {
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.RED + "You need at least " + getPrice("armour.diamond-armour", false) + " to buy this item.");
                    return;
                }

              //  playerItemInventory.removeEmerald(20);
                GameUtils.removeArmour(player);
                player.getInventory().setHelmet(new ItemStack(Material.DIAMOND_HELMET));
                player.getInventory().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
                player.getInventory().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
                player.getInventory().setBoots(new ItemStack(Material.DIAMOND_BOOTS));
                player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.GREEN + "You bought " + ChatColor.BLUE + "diamond armour" + ChatColor.GREEN + " for " + getPrice("armour.diamond-armour", true));
                break;

            default:
                return;
        }

       GameUtils.makePlayerArmourUnbreakable(player);
    }

    public void ToolShopItemClick(Player player, int slot) {
        PlayerItemInventory playerItemInventory = new PlayerItemInventory(player);
        String configItem = "";
        switch (slot) {
            case 12:
                // Buy stone pickaxe
                configItem = "tools.stone-pick";
                if (playerItemInventory.hasAmount(pricesConfig, configItem, true)) {
                   // playerItemInventory.removeIron(15);
                    player.getInventory().addItem(new ItemStack(Material.STONE_PICKAXE, 1));
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.GREEN + "You bought a " + ChatColor.BLUE + "stone pickaxe" + ChatColor.GREEN + " for " + getPrice(configItem, true));
                } else {
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.RED + "You need at least " + getPrice(configItem, false) + " to buy this item.");
                }
                break;
            case 13:
                // Buy iron pickaxe
                configItem = "tools.iron-pick";
                if (playerItemInventory.hasAmount(pricesConfig, configItem, true)) {
                  //  playerItemInventory.removeGold(15);
                    player.getInventory().addItem(new ItemStack(Material.IRON_PICKAXE, 1));
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.GREEN + "You bought an " + ChatColor.BLUE + "iron pickaxe" + ChatColor.GREEN + " for " + getPrice(configItem, true));
                } else {
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.RED + "You need at least " + getPrice(configItem, false) + " to buy this item.");
                }
                break;
            case 14:
                // Buy diamond pickaxe
                configItem = "tools.diamond-pick";
                if (playerItemInventory.hasAmount(pricesConfig, configItem, true)) {
                   // playerItemInventory.removeEmerald(3);
                    player.getInventory().addItem(new ItemStack(Material.DIAMOND_PICKAXE, 1));
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.GREEN + "You bought a " + ChatColor.BLUE + "diamond pickaaxe" + ChatColor.GREEN + " for " + getPrice(configItem, true));
                } else {
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.RED + "You need at least " + getPrice(configItem, false) + " to buy this item.");
                }
                break;
            case 21:
                // Buy stone axe
                configItem = "tools.stone-axe";
                if (playerItemInventory.hasAmount(pricesConfig, configItem, true)) {
                   // playerItemInventory.removeIron(15);
                    player.getInventory().addItem(new ItemStack(Material.STONE_AXE, 1));
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.GREEN + "You bought a " + ChatColor.BLUE + "stone axe" + ChatColor.GREEN + " for " + getPrice(configItem, true));
                } else {
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.RED + "You need at least " + getPrice(configItem, false) + " to buy this item.");
                }
                break;
            case 22:
                // Buy iron axe
                configItem = "tools.iron-axe";
                if (playerItemInventory.hasAmount(pricesConfig, configItem, true)) {
                  //  playerItemInventory.removeGold(15);
                    player.getInventory().addItem(new ItemStack(Material.IRON_AXE, 1));
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.GREEN + "You bought an " + ChatColor.BLUE + "iron axe" + ChatColor.GREEN + " for " + getPrice(configItem, true));
                } else {
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.RED + "You need at least " + getPrice(configItem, false) + " to buy this item.");
                }
                break;
            case 23:
                // Buy diamond axe
                configItem = "tools.diamond-axe";
                if (playerItemInventory.hasAmount(pricesConfig, configItem, true)) {
                   // playerItemInventory.removeDiamond(15);
                    player.getInventory().addItem(new ItemStack(Material.DIAMOND_AXE, 1));
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.GREEN + "You bought a " + ChatColor.BLUE + "diamond axe" + ChatColor.GREEN + " for " + getPrice(configItem, true));
                } else {
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.RED + "You need at least " + getPrice(configItem, false) + " to buy this item.");
                }
                break;
            case 30:
                // Buy shears
                configItem = "tools.shears";
                if (playerItemInventory.hasAmount(pricesConfig, configItem, true)) {
                   // playerItemInventory.removeIron(15);
                    player.getInventory().addItem(new ItemStack(Material.SHEARS, 1));
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.GREEN + "You bought " + ChatColor.BLUE + "shears" + ChatColor.GREEN + " for " + getPrice(configItem, true));
                } else {
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.RED + "You need at least " + getPrice(configItem, true) + " to buy this item.");
                }
                break;

            case 31:
                // Buy compass
                configItem = "tools.compass";
                if (playerItemInventory.hasAmount(pricesConfig, configItem, true)) {
                    // playerItemInventory.removeIron(15);
                    ItemStack compass = new ItemStack(Material.COMPASS);
                    ItemMeta ism = compass.getItemMeta();
                    ism.setDisplayName(ChatColor.GOLD + "Targets nearest enemy player.");
                    compass.setItemMeta(ism);
                    player.getInventory().addItem(compass);
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.GREEN + "You bought " + ChatColor.BLUE + "a compass" + ChatColor.GREEN + " for " + getPrice(configItem, true));
                } else {
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.RED + "You need at least " + getPrice(configItem, true) + " to buy this item.");
                }
                break;
            default:
                return;
        }
    }

    public  void BlockShopItemClick(Player player, int slot, int teamColorInt) {
        PlayerItemInventory playerItemInventory = new PlayerItemInventory(player);

        String configItem = "";
        switch (slot) {
            case 20:
                // Buy wool
                configItem = "blocks.wool";
                if (playerItemInventory.hasAmount(pricesConfig, configItem, true)) {
                  //  playerItemInventory.removeIron(5);
                    int amount = pricesConfig.getInt(configItem + ".amount");
                    player.getInventory().addItem(new ItemStack(Material.WOOL, amount, (short) teamColorInt));
                    //
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.GREEN + "You bought " + amount  + ChatColor.BLUE + " blocks of wool" + ChatColor.GREEN + " for " + getPrice(configItem, true));
                } else {
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.RED + "You need at least " + getPrice(configItem, false) + " to buy this item.");
                }
                break;
            case 21:
                // Buy glass
                configItem = "blocks.glass";
                if (playerItemInventory.hasAmount(pricesConfig, configItem, true)) {
                   // playerItemInventory.removeIron(5);
                    int amount = pricesConfig.getInt(configItem + ".amount");
                    player.getInventory().addItem(new ItemStack(Material.STAINED_GLASS, 16, (short) teamColorInt));
                    //
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.GREEN + "You bought " + amount  + ChatColor.BLUE + " blocks of glass" + ChatColor.GREEN + " for " + getPrice(configItem, true));
                } else {
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.RED + "You need at least " + getPrice(configItem, false) + " to buy this item.");
                }
                break;
            case 22:
                // Buy wood
                configItem = "blocks.wood";
                if (playerItemInventory.hasAmount(pricesConfig, configItem, true)) {
                   // playerItemInventory.removeGold(3);
                    int amount = pricesConfig.getInt(configItem + ".amount");
                    player.getInventory().addItem(new ItemStack(5, 16));
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.GREEN + "You bought " + amount  + ChatColor.BLUE + " blocks of wood" + ChatColor.GREEN + " for " + getPrice(configItem, true));
                } else {
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.RED + "You need at least " + getPrice(configItem, false) + " to buy this item.");
                }
                break;
            case 23:
                // Buy sandstone
                configItem = "blocks.sandstone";
                if (playerItemInventory.hasAmount(pricesConfig, configItem, true)) {
                  //  playerItemInventory.removeGold(4);
                    int amount = pricesConfig.getInt(configItem + ".amount");
                    player.getInventory().addItem(new ItemStack(Material.SANDSTONE, 16));
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.GREEN + "You bought " + amount  + ChatColor.BLUE + " blocks of sandstone" + ChatColor.GREEN + " for " + getPrice(configItem, true));
                } else {
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.RED + "You need at least " + getPrice(configItem, false) + " to buy this item.");
                }
                break;
            case 24:
                // Buy stone
                configItem = "blocks.stone";
                if (playerItemInventory.hasAmount(pricesConfig, configItem, true)) {
                  //  playerItemInventory.removeGold(6);
                    int amount = pricesConfig.getInt(configItem + ".amount");
                    player.getInventory().addItem(new ItemStack(Material.STONE, 16));
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.GREEN + "You bought " + amount  + ChatColor.BLUE + " blocks of stone" + ChatColor.GREEN + " for " + getPrice(configItem, true));
                } else {
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.RED + "You need at least " + getPrice(configItem, false) + " to buy this item.");
                }
                break;
            case 30:
                // Buy endstone
                configItem = "blocks.enderstone";
                if (playerItemInventory.hasAmount(pricesConfig, configItem, true)) {
                  //  playerItemInventory.removeGold(8);
                    int amount = pricesConfig.getInt(configItem + ".amount");
                    player.getInventory().addItem(new ItemStack(Material.ENDER_STONE, 16));
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.GREEN + "You bought " + amount  + ChatColor.BLUE + " blocks of endstone" + ChatColor.GREEN + " for " + getPrice(configItem, true));
                } else {
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.RED + "You need at least " + getPrice(configItem, false) + " to buy this item.");
                }
                break;
            case 31:
                // Buy obsidian
                configItem = "blocks.obsidian";
                if (playerItemInventory.hasAmount(pricesConfig, configItem, true)) {
                   // playerItemInventory.removeEmerald(1);
                    int amount = pricesConfig.getInt(configItem + ".amount");
                    player.getInventory().addItem(new ItemStack(Material.OBSIDIAN, 1));
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.GREEN + "You bought " + amount  + ChatColor.BLUE + " block of obsidian" + ChatColor.GREEN + " for " + getPrice(configItem, true));
                } else {
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.RED + "You need at least " + getPrice(configItem, false) + " to buy this item.");
                }
                break;
            case 32:
                // Buy wool
                configItem = "blocks.carpet";
                if (playerItemInventory.hasAmount(pricesConfig, configItem, true)) {
                    //  playerItemInventory.removeIron(5);
                    int amount = pricesConfig.getInt(configItem + ".amount");
                    ItemStack carpetStack = new ItemStack(Material.CARPET, amount, (short) teamColorInt);
                    ItemMeta carpetMeta = carpetStack.getItemMeta();
                    carpetMeta.setDisplayName(ChatColor.RESET + "Magic carpet");
                    carpetStack.setItemMeta(carpetMeta);
                    player.getInventory().addItem(carpetStack);
                    //
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.GREEN + "You bought " + amount  + ChatColor.BLUE + " blocks of wool" + ChatColor.GREEN + " for " + getPrice(configItem, true));
                } else {
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.RED + "You need at least " + getPrice(configItem, false) + " to buy this item.");
                }
                break;

            default:
                return;

        }
    }

    public void TrapShopItemClick(Player player, int slot) {
        PlayerItemInventory playerItemInventory = new PlayerItemInventory(player);
        String configItem = "";
        switch (slot) {
            case 20:
                // Buy Stonefish
                configItem = "traps.stonefish";
                if (playerItemInventory.hasAmount(pricesConfig, configItem, true)) {
                    int amount = pricesConfig.getInt(configItem + ".amount");
                   // playerItemInventory.removeIron(60);
                    ItemStack andesite = new ItemStack(Material.STONE);
                    andesite.setDurability((short) 5);
                    player.getInventory().addItem(andesite);
                    player.getInventory().addItem(andesite);
                    player.getInventory().addItem(andesite);
                    player.getInventory().addItem(andesite);
                    player.getInventory().addItem(andesite);
                    player.getInventory().addItem(andesite);
                    player.getInventory().addItem(andesite);
                    player.getInventory().addItem(andesite);
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.GREEN + "You bought " + amount  + ChatColor.BLUE + " stonefish blocks" + ChatColor.GREEN + " for " + getPrice(configItem, true));
                } else {
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.RED + "You need at least " + getPrice(configItem, false) + " to buy this item.");
                }
                break;
            case 21:
                // Buy launcher plate - stone
                configItem = "traps.launcher";
                if (playerItemInventory.hasAmount(pricesConfig, configItem, true)) {
                    ItemStack stack = new ItemStack(Material.STONE_PLATE, 1);
                    ItemMeta stackMeta  = stack.getItemMeta();
                    stackMeta.setDisplayName("Launcher floor trap");
                    stack.setItemMeta(stackMeta);
                    player.getInventory().addItem(stack);
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.GREEN + "You bought a " + ChatColor.BLUE + " pressure plate launcher " + ChatColor.GREEN + " for " + getPrice(configItem, true));
                } else {
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.RED + "You need at least " + getPrice(configItem, false) + " to buy this item.");
                }
                break;
            case 22:
                // Buy poison plate - wood
                configItem = "traps.poison";
                if (playerItemInventory.hasAmount(pricesConfig, configItem, true)) {
                    ItemStack stack = new ItemStack(Material.WOOD_PLATE, 1);
                    ItemMeta stackMeta  = stack.getItemMeta();
                    stackMeta.setDisplayName("Poison floor trap");
                    stack.setItemMeta(stackMeta);
                    player.getInventory().addItem(stack);
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.GREEN + "You bought a " + ChatColor.BLUE + " pressure plate poisoner " + ChatColor.GREEN + " for " + getPrice(configItem, true));
                } else {
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.RED + "You need at least " + getPrice(configItem, false) + " to buy this item.");
                }
                break;
            case 23:
                // Buy blindside plate - iron
                configItem = "traps.blindside";
                if (playerItemInventory.hasAmount(pricesConfig, configItem, true)) {
                    ItemStack stack = new ItemStack(Material.IRON_PLATE, 1);
                    ItemMeta stackMeta  = stack.getItemMeta();
                    stackMeta.setDisplayName("Blindside floor trap");
                    stack.setItemMeta(stackMeta);
                    player.getInventory().addItem(stack);
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.GREEN + "You bought a " + ChatColor.BLUE + " blindside pressure plate " + ChatColor.GREEN + " for " + getPrice(configItem, true));
                } else {
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.RED + "You need at least " + getPrice(configItem, false) + " to buy this item.");
                }
                break;
            default:
                return;
        }

    }

    public  void UpgradeShopItemClick(Player player, int slot, Team team) {
        PlayerItemInventory playerItemInventory = new PlayerItemInventory(player);
        String configItem = "";
        switch (slot) {
            case 20:
                // Gold-1 summoner upgrade.
                // only fire if they dont already have it
                if (team.isGoldSummonerActive() == false) {
                    configItem = "upgrades.gold-summoner";
                    if (playerItemInventory.hasAmount(pricesConfig, configItem, true)) {
                        // playerItemInventory.removeDiamond(1);
                        //
                        team.startGoldSummoner();
                        player.closeInventory();
                        player.openInventory(getBlazeShop(player, team));
                        //
                        player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.GREEN + "You have upgraded to the " + ChatColor.GOLD + "GOLD-1" + ChatColor.GREEN + " summoner.");
                    } else {
                        player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.RED + "You need at least " + getPrice(configItem, false) + " to buy this upgrade.");
                    }
                }
                break;
            case 21:
                // Diamond-1 summoner upgrade.
                // only fire if they dont already have it
                if (team.isDiamondSummonerActive() == false) {
                    configItem = "upgrades.diamond-summoner";
                    if (playerItemInventory.hasAmount(pricesConfig, configItem, true)) {
                        //playerItemInventory.removeEmerald(1);
                        team.startDiamondSummoner();
                        player.closeInventory();
                        player.openInventory(getBlazeShop(player, team));
                        //
                        player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.GREEN + "You have upgraded to the " + ChatColor.BLUE + "DIAMOND-1" + ChatColor.GREEN + " summoner.");
                    } else {
                        player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.RED + "You need at least " + getPrice(configItem, false) + " to buy this upgrade.");
                    }
                } else {
                    // if both diamond & gold active but final not, this should be the option
                    if (team.isGoldSummonerActive() && team.isDiamondSummonerActive() && team.isFinalSummonerActive() == false) {
                        configItem = "upgrades.final-summoner";
                        if (playerItemInventory.hasAmount(pricesConfig, configItem, true)) {
                            //playerItemInventory.removeEmerald(1);
                            team.startFinalSummoner();
                            player.closeInventory();
                            player.openInventory(getBlazeShop(player, team));
                            //
                            player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.GREEN + "You have upgraded to the " + ChatColor.RED + "SPEED" + ChatColor.GREEN + " summoner.");
                        } else {
                            player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.RED + "You need at least " + getPrice(configItem, false) + " to buy this upgrade.");
                        }
                    }
                }
                break;
            case 22:
                // Speed-1 upgrade.
                configItem = "upgrades.speed-1";
                if (playerItemInventory.hasAmount(pricesConfig, configItem, true)) {
                   team.applySpeed1();
                    //  *** NOTE ***   For the sake of testing, then to switch off the potion effects, I've added the line below to the "spawn" command.
                    //  *** NOTE ***   We need a separate method which runs when players leave the game (also at game end), which removes any residual potion effects.
                    //                 The code for speed removal would be:    if (player.hasPotionEffect(PotionEffectType.SPEED) == true) { player.removePotionEffect(PotionEffectType.SPEED); }
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.GREEN + "You have enabled team " + ChatColor.BLUE + "SPEED 1" + ChatColor.GREEN + " upgrade!");
                    player.closeInventory();
                    player.openInventory(getBlazeShop(player, team));
                } else {
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.RED + "You need at least " + getPrice(configItem, false) + " to buy this upgrade.");
                }
                break;
            case 23:
                // Speed-2 upgrade.
                configItem = "upgrades.speed-2";
                if (playerItemInventory.hasAmount(pricesConfig, configItem, false) == false) {
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.RED + "You need at least " + getPrice(configItem, false) + " to buy this upgrade.");
                    return;
                }
                if (player.hasPotionEffect(PotionEffectType.SPEED) == true) {
                    playerItemInventory.hasAmount(pricesConfig, configItem, true);
                    //  *** NOTE ***   The line below applies to the player only, but you would obviously need to loop through and apply it to all the player's team-mates.
                    team.applySpeed2();
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.GREEN + "You have bought team " + ChatColor.BLUE + "SPEED 2" + ChatColor.GREEN + " upgrade!");
                    player.closeInventory();
                    player.openInventory(getBlazeShop(player, team));
                } else { player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.RED + "You need to buy speed level 1 first, before you can buy this upgrade."); }
                break;


            default:
                return;
        }
    }

    public void EnchantShopItemClick(Player player, int slot) {
        PlayerItemInventory playerItemInventory = new PlayerItemInventory(player);
        String configItem = "";
        switch (slot) {
            case 20:
                // Sharpness
                configItem = "enchantress.sharpness-1-sword";
                ItemStack inHand = player.getInventory().getItemInHand();
                if (playerItemInventory.hasAmount(pricesConfig, configItem, false) == false) {
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.RED + "You need at least " + getPrice(configItem, false) + " to buy this enchantment.");
                    return;
                }
                if (inHand.getType() == Material.DIAMOND_SWORD || inHand.getType() == Material.IRON_SWORD || inHand.getType() == Material.STONE_SWORD) {
                    if (inHand.containsEnchantment(Enchantment.DAMAGE_ALL)) {
                        player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.RED + "Your sword already has this enchantment!");
                        return;
                    }
                    inHand.addEnchantment(Enchantment.DAMAGE_ALL, 1);
                    playerItemInventory.hasAmount(pricesConfig, configItem, true);
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.GREEN + "You bought " + ChatColor.BLUE + "sharpness" + ChatColor.GREEN + " for " + getPrice(configItem, true));
                } else { player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.RED + "Sorry, you can only enchant a SWORD with sharpness.");
                    return;
                }
                break;
            case 21:
                // Knockback
                configItem = "enchantress.knockback-2-sword";
                if (playerItemInventory.hasAmount(pricesConfig, configItem, false) == false) {
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.RED + "You need at least " + getPrice(configItem, false) + " to buy this enchantment.");
                    return;
                }
                inHand = player.getInventory().getItemInHand();
                if (inHand.getType() == Material.DIAMOND_SWORD || inHand.getType() == Material.IRON_SWORD || inHand.getType() == Material.STONE_SWORD) {
                    if (inHand.containsEnchantment(Enchantment.KNOCKBACK)) {
                        player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.RED + "Your sword already has this enchantment!");
                        return;
                    }
                    inHand.addEnchantment(Enchantment.KNOCKBACK, 1);
                    playerItemInventory.hasAmount(pricesConfig, configItem, true);
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.GREEN + "You bought " + ChatColor.BLUE + "knockback" + ChatColor.GREEN + " for " + getPrice(configItem, true));
                } else {
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.RED + "Sorry, you can only enchant a SWORD with knockback.");
                    return;
                }
                break;
            case 22:
                // Flame sword or bow
                configItem = "enchantress.fire-5-sword";
                if (playerItemInventory.hasAmount(pricesConfig, configItem, false) == false) {
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.RED + "You need at least " + getPrice(configItem, false) + " to buy this enchantment.");
                    return;
                }
                inHand = player.getInventory().getItemInHand();
             //   if (inHand.getType() == Material.DIAMOND_SWORD || inHand.getType() == Material.IRON_SWORD || inHand.getType() == Material.STONE_SWORD || inHand.getType() == Material.BOW) {
                if (inHand.getType() == Material.DIAMOND_SWORD || inHand.getType() == Material.IRON_SWORD || inHand.getType() == Material.STONE_SWORD) {
                    if (inHand.containsEnchantment(Enchantment.FIRE_ASPECT)) {
                        player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.RED + "Your sword already has this enchantment!");
                        return;
                    }
                    inHand.addEnchantment(Enchantment.FIRE_ASPECT, 2);
                    playerItemInventory.hasAmount(pricesConfig, configItem, true);
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.GREEN + "You bought " + ChatColor.BLUE + "fire aspect" + ChatColor.GREEN + " for " + getPrice(configItem, true));
                } else {
                    if (inHand.getType() == Material.BOW) {
                        if (inHand.containsEnchantment(Enchantment.ARROW_FIRE)) {
                            player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.RED + "Your bow already has this enchantment!");
                            return;
                        }
                        inHand.addEnchantment(Enchantment.ARROW_FIRE, 1);
                        playerItemInventory.hasAmount(pricesConfig, configItem, true);
                        player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.GREEN + "You bought " + ChatColor.BLUE + "fire aspect" + ChatColor.GREEN + " for " + getPrice(configItem, true));
                    } else {
                        // player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.RED + "Sorry, this enchantment is for SWORD or BOW only.");
                        player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.RED + "Sorry, this enchantment is for SWORD or BOW.");
                        return;
                    }
                }
                break;
            case 23:
                // Power Punch bow
                configItem = "enchantress.power-punch-4-bow";
                if (playerItemInventory.hasAmount(pricesConfig, configItem, false) == false) {
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.RED + "You need at least " + getPrice(configItem, false) + " to buy this enchantment.");
                    return;
                }
                inHand = player.getInventory().getItemInHand();
                if (inHand.getType() == Material.BOW) {
                    if (inHand.containsEnchantment(Enchantment.ARROW_KNOCKBACK)) {
                        player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.RED + "Your bow already has this enchantment!");
                        return;
                    }
                    inHand.addEnchantment(Enchantment.ARROW_KNOCKBACK, 1);
                    inHand.addEnchantment(Enchantment.ARROW_DAMAGE, 1);
                    playerItemInventory.hasAmount(pricesConfig, configItem, true);
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.GREEN + "You bought " + ChatColor.BLUE + "power punch" + ChatColor.GREEN + " for " + getPrice(configItem, true));
                } else {
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.RED + "Sorry, this enchantment is for BOW only.");
                    return;
                }
                break;
            case 24:
                // Efficiency pick
                configItem = "enchantress.efficiency-4-mining";
                if (playerItemInventory.hasAmount(pricesConfig, configItem, false) == false) {
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.RED + "You need at least " + getPrice(configItem, false) + " to buy this enchantment.");
                    return;
                }
                inHand = player.getInventory().getItemInHand();
                if (inHand.getType() == Material.STONE_PICKAXE || inHand.getType() == Material.IRON_PICKAXE || inHand.getType() == Material.DIAMOND_PICKAXE) {
                    if (inHand.containsEnchantment(Enchantment.DIG_SPEED)) {
                        player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.RED + "Your pick already has this enchantment!");
                        return;
                    }
                    inHand.addEnchantment(Enchantment.DIG_SPEED, 2);
                    playerItemInventory.hasAmount(pricesConfig, configItem, true);
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.GREEN + "You bought " + ChatColor.BLUE + "efficiency" + ChatColor.GREEN + " for " + getPrice(configItem, true));
                } else { player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.RED + "Sorry, this enchantment is for a PICKAXE only.");
                    return;
                }
                break;
            case 31:
                // Protection
                configItem = "enchantress.protection-2-armour";
                if (playerItemInventory.hasAmount(pricesConfig, configItem, false) == false) {
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.RED + "You need at least " + getPrice(configItem, false) + " to buy this enchantment.");
                    return;
                }
                if (player.getInventory().getHelmet() != null) {
                    if (player.getInventory().getHelmet().containsEnchantment(Enchantment.PROTECTION_FIRE)) {
                        player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.RED + "Your armour already has this enchantment!");
                        return;
                    }
                    player.getInventory().getHelmet().addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
                    player.getInventory().getHelmet().addEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 1);
                    player.getInventory().getHelmet().addEnchantment(Enchantment.PROTECTION_FIRE, 1);
                    player.getInventory().getHelmet().addEnchantment(Enchantment.PROTECTION_PROJECTILE, 1);
                }
                if (player.getInventory().getChestplate() != null) {
                    player.getInventory().getChestplate().addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
                    player.getInventory().getChestplate().addEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 1);
                    player.getInventory().getChestplate().addEnchantment(Enchantment.PROTECTION_FIRE, 1);
                    player.getInventory().getChestplate().addEnchantment(Enchantment.PROTECTION_PROJECTILE, 1);
                }
                if (player.getInventory().getLeggings() != null) {
                    player.getInventory().getLeggings().addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
                    player.getInventory().getLeggings().addEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 1);
                    player.getInventory().getLeggings().addEnchantment(Enchantment.PROTECTION_FIRE, 1);
                    player.getInventory().getLeggings().addEnchantment(Enchantment.PROTECTION_PROJECTILE, 1);
                }
                if (player.getInventory().getBoots() != null) {
                    player.getInventory().getBoots().addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
                    player.getInventory().getBoots().addEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 1);
                    player.getInventory().getBoots().addEnchantment(Enchantment.PROTECTION_FALL, 1);
                    player.getInventory().getBoots().addEnchantment(Enchantment.PROTECTION_FIRE, 1);
                    player.getInventory().getBoots().addEnchantment(Enchantment.PROTECTION_PROJECTILE, 1);
                }
                playerItemInventory.hasAmount(pricesConfig, configItem, true);
                player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.GREEN + "You bought " + ChatColor.BLUE + "armour protection" + ChatColor.GREEN + " for " + getPrice(configItem, true));
                break;

            default:
                return;
        }
    }

    public  void ArmourerShopItemClick(Player player, int slot) {
        PlayerItemInventory playerItemInventory = new PlayerItemInventory(player);
        String configItem = "";
        switch (slot) {
            case 20:
                // Buy TNT
                configItem = "armoury.tnt";
                if (playerItemInventory.hasAmount(pricesConfig, configItem, true)) {
                   // playerItemInventory.removeEmerald(1);
                    player.getInventory().addItem(new ItemStack(Material.TNT, 1));
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.GREEN + "You bought " + ChatColor.BLUE + "TNT" + ChatColor.GREEN + " for " + getPrice(configItem, true));
                } else {
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.RED + "You need at least " + getPrice(configItem, false) + " to buy this item.");
                }
                break;
            case 21:
                // Buy fireball
                configItem = "armoury.fireball";
                if (playerItemInventory.hasAmount(pricesConfig, configItem, true)) {
                   // playerItemInventory.removeIron(120);
                    player.getInventory().addItem(new ItemStack(Material.FIREBALL, 1));
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.GREEN + "You bought a " + ChatColor.BLUE + "fireball" + ChatColor.GREEN + " for " + getPrice(configItem, true));
                } else {
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.RED + "You need at least " + getPrice(configItem, false) + " to buy this item.");
                }
                break;
            case 22:
                // Buy golem
                configItem = "armoury.golem";
                if (playerItemInventory.hasAmount(pricesConfig, configItem, true)) {
                  //  playerItemInventory.removeIron(200);
                  //  player.getInventory().addItem(new ItemStack(383, 1));
                    ItemStack eggs = new ItemStack(Material.EGG, 1);
                    ItemMeta eggsMeta  = eggs.getItemMeta();
                    eggsMeta.setDisplayName("Golem spawn egg");
                    eggs.setItemMeta(eggsMeta);
                    player.getInventory().addItem(eggs);
                   // player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.GREEN + "You bought a " + ChatColor.BLUE + "team golem" + ChatColor.GREEN + " for " + getPrice(configItem, true));
                } else {
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.RED + "You need at least " + getPrice(configItem, false) + " to buy this item.");
                }
                break;
            case 23:
                // Buy gapple
                configItem = "armoury.gapple";
                if (playerItemInventory.hasAmount(pricesConfig, configItem, true)) {
                   // playerItemInventory.removeEmerald(1);
                    player.getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE, 1));
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.GREEN + "You bought a " + ChatColor.BLUE + "golden apple" + ChatColor.GREEN + " for " + getPrice(configItem, true));
                } else {
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.RED + "You need at least " + getPrice(configItem, false) + " to buy this item.");
                }
                break;
            case 24:
                // Buy water bucket
                configItem = "armoury.water-bucket";
                if (playerItemInventory.hasAmount(pricesConfig, configItem, true)) {
                  //  playerItemInventory.removeEmerald(2);
                    player.getInventory().addItem(new ItemStack(Material.WATER_BUCKET, 1));
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.GREEN + "You bought a " + ChatColor.BLUE + "water bucket" + ChatColor.GREEN + " for " + getPrice(configItem, true));
                } else {
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.RED + "You need at least " + getPrice(configItem, false) + " to buy this item.");
                }
                break;
            case 30:
                // Buy silverfish
                configItem = "armoury.silverfish";
                if (playerItemInventory.hasAmount(pricesConfig, configItem, true)) {
                    // playerItemInventory.removeIron(50);
                    player.getInventory().addItem(new ItemStack(332, 1));
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.GREEN + "You bought a " + ChatColor.BLUE + "team silverfish" + ChatColor.GREEN + " for " + getPrice(configItem, true));
                } else {
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.RED + "You need at least " + getPrice(configItem, false) + " to buy this item.");
                }
                break;
            case 31:
                // Buy speed potion
                configItem = "armoury.speed-potion";
                if (playerItemInventory.hasAmount(pricesConfig, configItem, true)) {
                    Potion potion = new Potion(PotionType.SPEED, 2);
                    // playerItemInventory.removeEmerald(1);
                    ItemStack potionIs = potion.toItemStack(1);
                    player.getInventory().addItem(potionIs);
                    //  Glass bottle is 373
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.GREEN + "You bought a " + ChatColor.BLUE + "swiftness potion" + ChatColor.GREEN + " for " + getPrice(configItem, true));
                } else {
                    player.sendMessage(ChatUtils.arenaChatPrefix + ChatColor.RED + "You need at least " + getPrice(configItem, false) + " to buy this item.");
                }
                break;

            default:
                return;
        }
    }

    private String getPrice(String item, boolean withColorAndFullStop) {
        int cost = pricesConfig.getInt(item + ".price");
        String currency = pricesConfig.getString(item + ".currency");
        String ret = "";
        if (withColorAndFullStop) {
            if (currency.equals("iron")) {
                ret = ChatColor.WHITE + "";
            }
            if (currency.equals("emerald")) {
                ret = ChatColor.DARK_GREEN + "";
            }
            if (currency.equals("gold")) {
                ret = ChatColor.GOLD + "";
            }
            if (currency.equals("diamond")) {
                ret = ChatColor.BLUE + "";
            }
        }
        if (cost > 1 && (currency.equals("diamond") || currency.equals("emerald"))) {
            // make currency plural
            currency = currency + "s";
        }
        ret = ret + cost + " " + currency;

        if (withColorAndFullStop) {
            ret = ret + ".";
        }

        return  ret;

    }

}
