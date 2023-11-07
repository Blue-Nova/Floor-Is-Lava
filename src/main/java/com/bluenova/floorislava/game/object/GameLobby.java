package com.bluenova.floorislava.game.object;

import com.bluenova.floorislava.FloorIsLava;
import com.bluenova.floorislava.util.*;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class GameLobby {

    public static ArrayList<GameLobby> GAME_LOBBY_LIST = new ArrayList<>();
    ArrayList<Integer> LAVA_ANNOUNCE_HEIGHTS = new ArrayList<>();

    final int LAVA_INCREMENT = 3;
    public final HashMap<Player, Location> previousLocationList;
    final Player owner;
    public HashMap<Player, ItemStack[]> previousInventoryList = new HashMap<>();
    public HashMap<Player, Double> previousHealthList = new HashMap<>();
    public HashMap<Player, Integer> previousHungerList = new HashMap<>();
    public HashMap<Player, Float> previousXPList = new HashMap<>();
    public ArrayList<Player> playerList;
    HashMap<Player,Location> playerSpawnLocation = new HashMap<>();
    public ArrayList<Player> specList = new ArrayList<>();
    public GamePlot gamePlot;
    public int countDown = 3;
    WorldBorder gameBorder;
    Location gameStartLoc;
    Location gameEndLoc;
    boolean gameON = true;
    public int lavaHeight = -63 + LAVA_INCREMENT;

    public GameLobby(ArrayList<Player> playerList, Player owner, GamePlot plot) {

        LAVA_ANNOUNCE_HEIGHTS.add(-32);LAVA_ANNOUNCE_HEIGHTS.add(0);LAVA_ANNOUNCE_HEIGHTS.add(20);LAVA_ANNOUNCE_HEIGHTS.add(40);

        this.gamePlot = plot;

        this.playerList = playerList;
        this.owner = owner;

        HashMap<Player, Location> prevLocTempMap = new HashMap<>();
        for (Player player : playerList) {
            prevLocTempMap.put(player, player.getLocation());
        }
        previousLocationList = prevLocTempMap;

        GameLobby.GAME_LOBBY_LIST.add(this);
        gameStartLoc = gamePlot.plotStart;
        gameEndLoc = gamePlot.plotEnd;

        announce(ChatColor.AQUA + "Generating Terrain For Game...");
        FloorIsLava.getInstance().getWorkloadRunnable().addWorkload(new FindAllowedLocation(this));

    }

    public void generatePlot(int x, int z) {
        WorkloadRunnable WLR = FloorIsLava.getInstance().getWorkloadRunnable();
        int border_x = (int) gamePlot.plotStart.getX();
        int border_z = (int) gamePlot.plotStart.getZ();
        int plotSize = FloorIsLava.getInstance().getGamePlotDivider().plotSize;
        if (!gamePlot.hasBorders) {
            for(int y_level = -64; y_level < 320; y_level++) {
                WLR.addWorkload(new MakeBarrierWall(border_x - 1, border_z - 1,
                        border_x + plotSize, border_z - 1, y_level));
                WLR.addWorkload(new MakeBarrierWall(border_x - 1, border_z - 1,
                        border_x - 1, border_z + plotSize, y_level));
                WLR.addWorkload(new MakeBarrierWall(border_x + plotSize,
                        border_z + plotSize, border_x + plotSize, border_z - 1, y_level));
                WLR.addWorkload(new MakeBarrierWall(border_x + plotSize,
                        border_z + plotSize, border_x - 1, border_z + plotSize, y_level));
            }
        }
        gamePlot.hasBorders = true;
        int x_copy = x;
        int z_copy = z;
        int x_paste = (int) gamePlot.plotStart.getX();
        int z_paste = (int) gamePlot.plotStart.getZ();
        GameLobby gp = null;

        for (int x_index = 0; x_index < FloorIsLava.getInstance().getGamePlotDivider().plotSize; x_index++) {
            for (int z_index = 0; z_index < FloorIsLava.getInstance().getGamePlotDivider().plotSize; z_index++) {
                if (z_index == FloorIsLava.getInstance().getGamePlotDivider().plotSize - 1 && x_index == FloorIsLava.getInstance().getGamePlotDivider().plotSize - 1)
                    gp = this;
                FloorIsLava.getInstance().getWorkloadRunnable().addWorkload(new GenerateGameTerrain(gp, x_copy + x_index, z_copy + z_index, x_paste + x_index, z_paste + z_index));
            }
        }
    }

    public void startGame() {
        Bukkit.getScheduler().runTaskTimer(FloorIsLava.getInstance(), (task) -> {
            if (countDown <= 0) {
                announce(ChatColor.AQUA + "Teleporting...");

                gameBorder = Bukkit.createWorldBorder();
                gameBorder.setCenter(new Location(FloorIsLava.getInstance().getVoidWorld(), (gamePlot.plotStart.getX() + (FloorIsLava.getInstance().getGamePlotDivider().plotSize / 2)), 120, gamePlot.plotStart.getZ() + (FloorIsLava.getInstance().getGamePlotDivider().plotSize / 2)));
                gameBorder.setSize(FloorIsLava.getInstance().getGamePlotDivider().plotSize);

                for (Player player : playerList) {
                    Location game_loc = Tools.getSafeLocation(FloorIsLava.getInstance().getVoidWorld(),gamePlot);
                    player.teleport(game_loc);
                    playerSpawnLocation.put(player,game_loc);
                    player.setWorldBorder(gameBorder);
                    savePlayerInfo(player);
                }
                runBackMusic();
                InviteLobby.inviteLobbyList.remove(Tools.getLobbyFromOwner(owner));
                beginLavaTimer();
                beginEventTimer();
                task.cancel();
            }
            announce(ChatColor.RED + "Game starts" + ChatColor.RESET + " in " + ChatColor.AQUA + countDown);
            countDown--;
        }, 0, 21L);
    }

    public void savePlayerInfo(Player player) {
        previousInventoryList.put(player, player.getInventory().getContents());
        previousHealthList.put(player, player.getHealth());
        previousHungerList.put(player, player.getFoodLevel());
        previousXPList.put(player, player.getExp());

        player.getInventory().clear();
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setExp(0);
    }

    public void returnPlayerInfo(Player player) {
        player.getInventory().setContents(previousInventoryList.get(player));
        player.setHealth(previousHealthList.get(player));
        player.setFoodLevel(previousHungerList.get(player));
        player.setExp(previousXPList.get(player));
    }

    public void runBackMusic() {
        Bukkit.getScheduler().runTaskTimer(FloorIsLava.getInstance(), playBackSongTask -> {
            if (gameON)
                playGameSound(Sound.MUSIC_DISC_PIGSTEP);
            else playBackSongTask.cancel();
        }, 0L, 2820L);
    }

    public void announce(String msg) {
        for (Player player : playerList) {
            player.sendMessage(msg);
        }
        for (Player player : specList) {
            player.sendMessage(msg);
        }
    }

    public void playGameSound(Sound sound) {
        for (Player player : playerList) {
            player.playSound(gameBorder.getCenter(), sound, 40, 1);
        }
        for (Player player : specList) {
            player.playSound(gameBorder.getCenter(), sound, 40, 1);
        }
    }

    public void beginEventTimer(){
        AtomicInteger EventSpawnChance = new AtomicInteger();
        Random rand = new Random();
        Bukkit.getScheduler().runTaskTimer(FloorIsLava.getInstance(),(task) -> {
            if(EventSpawnChance.get() >= rand.nextInt(10,100)){
                ChaosEventManager.eventate(this);
                EventSpawnChance.set(0);
            }else{
                EventSpawnChance.addAndGet(100);
            }
        },0,100);
    }

    public void beginLavaTimer() {
        Bukkit.getScheduler().runTaskTimer(FloorIsLava.getInstance(), (task) -> {
            if (gameON) {
                placeLava();
                if (!(lavaHeight >= 319))
                    lavaHeight += LAVA_INCREMENT;

            } else task.cancel();
        }, 0, 200);
    }

    public void placeLava() {
        if(owner.getLocation().getY() < lavaHeight){
            for (int y_lava = (int) (owner.getLocation().getY()-1); y_lava < (int) (owner.getLocation().getY()+3); y_lava++) {
                FloorIsLava.getInstance().getWorkloadRunnable().addWorkload(new ElevateLava(gamePlot, y_lava));
            }
        }
        for (Player player: playerList) {
            if(player.getLocation().getY() < lavaHeight){
                for (int y_lava = (int) (player.getLocation().getY()); y_lava < (int) (player.getLocation().getY()+3); y_lava++) {
                    FloorIsLava.getInstance().getWorkloadRunnable().addWorkload(new ElevateLava(gamePlot, y_lava));
                }
            }
        }
        for (int y_lava = lavaHeight-LAVA_INCREMENT; y_lava < lavaHeight+LAVA_INCREMENT; y_lava++) {
            FloorIsLava.getInstance().getWorkloadRunnable().addWorkload(new ElevateLava(gamePlot, y_lava));
        }
        for (Integer y_height:LAVA_ANNOUNCE_HEIGHTS) {
            if(y_height <= lavaHeight){
                announce(ChatColor.RED + "LAVA" + ChatColor.RESET + " has reached y level: " + ChatColor.GOLD + lavaHeight);
                playGameSound(Sound.BLOCK_LAVA_AMBIENT);
                LAVA_ANNOUNCE_HEIGHTS.remove(y_height);
                break;
            }
        }

    }

    public void remove(Player leavingPlayer, boolean died) {
        if (!died) {

            announce(ChatColor.RED + leavingPlayer.getName() + " has left" + ChatColor.RESET + " the game.");
            leavingPlayer.sendMessage("You have " + ChatColor.RED + "left the game.");
            leavingPlayer.teleport(previousLocationList.get(leavingPlayer));

            returnPlayerInfo(leavingPlayer);

            if (this.specList.contains(leavingPlayer)) {
                this.specList.remove(leavingPlayer);
                leavingPlayer.setGameMode(GameMode.SURVIVAL);
            }

            this.playerList.remove(leavingPlayer);

        } else {
            this.playerList.remove(leavingPlayer);
            this.specList.add(leavingPlayer);

            announce(ChatColor.RED + leavingPlayer.getName() + " has died!" + ChatColor.RESET + " They are now spectating.");
            leavingPlayer.playSound(leavingPlayer.getLocation(), Sound.ENTITY_PLAYER_BURP, 1, 1);
            leavingPlayer.setGameMode(GameMode.SPECTATOR);
        }

        if (playerList.size() == 1) {
            winPlayer(playerList.get(0));
        }
    }

    public void playerDiedNoLava(Player player) {
        ItemStack[] inv = player.getInventory().getContents();
        player.getInventory().clear();
        for (ItemStack is : inv) {
            if(is != null)
                FloorIsLava.getInstance().getVoidWorld().dropItem(player.getLocation(),is);
        }
        player.teleport(playerSpawnLocation.get(player));
    }

    public void winPlayer(Player player) {
        playGameSound(Sound.UI_TOAST_CHALLENGE_COMPLETE);
        announce(ChatColor.RED + player.getName() + ChatColor.GOLD + " has won the game!");
        player.sendTitle(ChatColor.GREEN + "YOU WON", "You are the " + ChatColor.RED + "LAVA " + ChatColor.GOLD + "MASTER", 10, 100, 40);
        endGame();
    }

    public void endGame() {
        gameON = false;
        Bukkit.getScheduler().runTaskLater(FloorIsLava.getInstance(), (task) ->
                        announce(ChatColor.RED + "Game ended!" + ChatColor.YELLOW + " Players will be teleported back shortly...")
                , 140L);

        Bukkit.getScheduler().runTaskLater(FloorIsLava.getInstance(), (task) -> {
            while (playerList.size() > 0) {
                remove(playerList.get(0), false);
            }
            while (specList.size() > 0) {
                remove(specList.get(0), false);
            }

            gamePlot.inUse = false;
        }, 200L);
    }
}
