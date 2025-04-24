package com.bluenova.floorislava.game.object;

import com.bluenova.floorislava.FloorIsLava;
import com.bluenova.floorislava.game.object.gamelobby.GameLobby;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Random;

public class ChaosEventManager {
    private static final ChaosEvents[] chaosEvents = ChaosEvents.values();

    private static ArrayList<Player> cursedPlayers = new ArrayList<>();
    private static ArrayList<Player> bountyPlayers = new ArrayList<>();

    public static void eventate(GameLobby GL){
        /*
        ChaosEvents pickedEvent = chaosEvents[new Random().nextInt(0,chaosEvents.length-1)];
        switch (pickedEvent){
            case CURSE:
                cursePlayer(GL);
                return;
            case BLAZES:
                spawnBlazes(GL);
                return;
            case BOUNTY:
                bountyPlayer(GL);
                return;
            case GHASTS:
                spawnGhasts(GL);
                return;
            case KIT_BLOCKS:
                giveBlocksKit(GL);
                return;
            case KIT_PVP:
                givePvPKit(GL);
                return;
            case FIRE_RES:
                giveFireRes(GL);
                return;
            case TNT_SHOWER:
                spawnTNTShower(GL);
                return;
            case CREEPER_SHOWER:
                spawnCreeperShower(GL);
                return;
            case KIT_THROWABLES:
                giveThrowKit(GL);
                return;
            default:
        }*/
    }

    private static void giveThrowKit(GameLobby GL) {
        for (Player p:GL.players) {
            ItemStack pearls = new ItemStack(Material.ENDER_PEARL);
            ItemStack bow = new ItemStack(Material.BOW);ItemStack arrows = new ItemStack(Material.ARROW);
            pearls.setAmount(6);arrows.setAmount(16);

            p.getInventory().addItem(pearls);p.getInventory().addItem(arrows);
            p.getInventory().addItem(bow);p.getInventory().addItem(pearls);
        }
        GL.announce("Throw Kit!");
    }

    private static void spawnCreeperShower(GameLobby GL) {
        double y = getAboveHighestPlayer(GL);
        Location StartLoc = GL.gameStartLoc;
        Location EndLoc = GL.gameEndLoc;
        Bukkit.getScheduler().runTask(FloorIsLava.getInstance(),(BukkitTask ->{
            for(int i=0;i<25;i++){
                double spawnX = new Random().nextDouble(StartLoc.getX(),EndLoc.getX());
                double spawnZ = new Random().nextDouble(StartLoc.getZ(),EndLoc.getZ());
                GL.gameStartLoc.getWorld().spawn(
                        new Location(GL.gameStartLoc.getWorld(),spawnX,y,spawnZ), Creeper.class,(creeper)->{
                            creeper.setCustomName("ALIEN CREEPER");
                            creeper.setInvulnerable(true);
                            creeper.setExplosionRadius(5);
                            tickTock(creeper,50);
                        });}}));
        GL.announce("creeper shower!");
    }

    private static void tickTock(Damageable entity,int seconds) {
        Bukkit.getScheduler().runTaskLater(FloorIsLava.getInstance(),(task)->{
            entity.setHealth(0);
        },20*seconds);
    }


    private static void spawnTNTShower(GameLobby GL) {
        double y = getAboveHighestPlayer(GL);
        Location StartLoc = GL.gameStartLoc;
        Location EndLoc = GL.gameEndLoc;
        Bukkit.getScheduler().runTask(FloorIsLava.getInstance(),(BukkitTask ->{
            for(int i=0;i<25;i++){
                double spawnX = new Random().nextDouble(StartLoc.getX(),EndLoc.getX());
                double spawnZ = new Random().nextDouble(StartLoc.getZ(),EndLoc.getZ());
                GL.gameStartLoc.getWorld().spawn(
                        new Location(GL.gameStartLoc.getWorld(),spawnX,y,spawnZ), TNTPrimed.class,(tntPrimed) ->{
                            tntPrimed.setFuseTicks(50);
                        });}}));
        GL.announce("tnt shower!");
    }

    private static void giveFireRes(GameLobby GL) {
        for (Player p:GL.players) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE,2000,1));
        }
        GL.announce("Fire res!");
    }

    private static void givePvPKit(GameLobby GL) {
        for (Player p:GL.players) {
            ItemStack chestPlate = new ItemStack(Material.LEATHER_CHESTPLATE);
            chestPlate.addEnchantment(Enchantment.PROTECTION,1);
            ItemStack boots = new ItemStack(Material.IRON_BOOTS);ItemStack sword = new ItemStack(Material.IRON_SWORD);
            ItemStack beef = new ItemStack(Material.BEEF); beef.setAmount(12);

            p.getInventory().addItem(chestPlate);p.getInventory().addItem(sword);
            p.getInventory().addItem(boots);p.getInventory().addItem(beef);
        }
        GL.announce("pvp Kit!");
    }

    private static void giveBlocksKit(GameLobby GL) {
        for (Player p:GL.players) {
            ItemStack dirtBlocks = new ItemStack(Material.DIRT);ItemStack stoneBlocks = new ItemStack(Material.DIRT);
            ItemStack logsBlocks = new ItemStack(Material.OAK_LOG);
            dirtBlocks.setAmount(64);stoneBlocks.setAmount(64);logsBlocks.setAmount(32);
            p.getInventory().addItem(dirtBlocks);p.getInventory().addItem(dirtBlocks);
            p.getInventory().addItem(stoneBlocks);p.getInventory().addItem(logsBlocks);
        }
        GL.announce("blocks Kit!");
    }

    private static void spawnGhasts(GameLobby GL) {
        double y = getAboveHighestPlayer(GL);
        Location StartLoc = GL.gameStartLoc;
        Location EndLoc = GL.gameEndLoc;
        GL.announce("GHASTS ARE SPAWNING!");
        Bukkit.getScheduler().runTask(FloorIsLava.getInstance(),(BukkitTask ->{
            for(int i=0;i<5;i++){
                double spawnX = new Random().nextDouble(StartLoc.getX(),EndLoc.getX());
                double spawnZ = new Random().nextDouble(StartLoc.getZ(),EndLoc.getZ());
                GL.gameStartLoc.getWorld().spawn(
                        new Location(GL.gameStartLoc.getWorld(),spawnX,y,spawnZ), Ghast.class,(ghast)->{
                            tickTock(ghast,120);
                        });}}));
    }

    private static void spawnBlazes(GameLobby GL) {
        double y = getAboveHighestPlayer(GL);
        Location StartLoc = GL.gameStartLoc;
        Location EndLoc = GL.gameEndLoc;
        GL.announce("BLAZES ARE SPAWNING!");
        Bukkit.getScheduler().runTask(FloorIsLava.getInstance(),(BukkitTask ->{
            for(int i=0;i<10;i++){
                double spawnX = new Random().nextDouble(StartLoc.getX(),EndLoc.getX());
                double spawnZ = new Random().nextDouble(StartLoc.getZ(),EndLoc.getZ());
                GL.gameStartLoc.getWorld().spawn(
                new Location(GL.gameStartLoc.getWorld(),spawnX,y,spawnZ), Blaze.class,(blaze)->{
                            tickTock(blaze,120);
                        });}}));
    }

    private static void bountyPlayer(GameLobby GL) {
        Player bountyPlayer = GL.players.get(new Random().nextInt(0,GL.players.size()));
        bountyPlayers.add(bountyPlayer);
        GL.announce(bountyPlayer.getName()+" has a BOUNTY on them for 2 mins!");
        Bukkit.getScheduler().runTaskLater(FloorIsLava.getInstance(),(bukkitTask ->{
            cursedPlayers.remove(bountyPlayer);
            GL.announce(bountyPlayer.getName()+" no longer has a BOUNTY!");
        }),20*120);
    }

    private static void cursePlayer(GameLobby GL) {
        Player cursedPlayer = GL.players.get(new Random().nextInt(0,GL.players.size()));
        cursedPlayers.add(cursedPlayer);
        GL.announce(cursedPlayer.getName()+" has a CURSE on them for 2 mins!");
        Bukkit.getScheduler().runTaskLater(FloorIsLava.getInstance(),(bukkitTask ->{
            cursedPlayers.remove(cursedPlayer);
            GL.announce(cursedPlayer.getName()+" no longer has a CURSE!");
        }),20*120);
    }
    private static double getAboveHighestPlayer(GameLobby gl) {
        double y=-64;
        for (Player p:gl.players) {
            if(p.getLocation().getY()>y)y=p.getLocation().getY();
        }
        return y+20;
    }
}
