package GameObjects;


import Utils.*;
import floorIsLava.FloorIsLava;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
public class GameLobby {

    public static ArrayList<GameLobby>GAME_LOBBY_LIST = new ArrayList<>();

    public final HashMap<Player, Location> previousLocationList;

    public HashMap<Player, ItemStack[]> previousInventoryList = new HashMap<>();
    public HashMap<Player, Double> previousHealthList = new HashMap<>();
    public HashMap<Player, Integer> previousHungerList = new HashMap<>();
    public HashMap<Player, Float> previousXPList = new HashMap<>();

    public ArrayList<Player> playerList;
    public ArrayList<Player> specList = new ArrayList<>();

    public GamePlot gamePlot;

    final Player owner;
    WorldBorder gameBorder;
    Location gameStartLoc;
    Location gameEndLoc;

    boolean gameON = true;
    int lavaHeight = -63;

    public int countDown = 3;

    public GameLobby(ArrayList<Player> playerList, Player owner, GamePlot plot) {

        this.gamePlot = plot;

        this.playerList = playerList;
        this.owner = owner;

        HashMap<Player,Location> prevLocTempMap = new HashMap<>();
        for (Player player:playerList) {
            prevLocTempMap.put(player,player.getLocation());
        }
        previousLocationList = prevLocTempMap;

        GameLobby.GAME_LOBBY_LIST.add(this);
        gameStartLoc = gamePlot.plotStart;
        gameEndLoc = gamePlot.plotEnd;

        announce(ChatColor.AQUA + "Generating Terrain For Game...");
        FloorIsLava.WLR.addWorkload(new FindAllowedLocation(this));

    }

    public void generatePlot(int x, int z){
        if(!gamePlot.hasBordeers) {
            FloorIsLava.WLR.addWorkload(new MakeBarrierWall((int) gamePlot.plotStart.getX() - 1, (int) gamePlot.plotStart.getZ() - 1, (int) gamePlot.plotStart.getX() + FloorIsLava.GPD.plotSize + 1, (int) gamePlot.plotStart.getZ() - 1));
            FloorIsLava.WLR.addWorkload(new MakeBarrierWall((int) gamePlot.plotStart.getX() - 1, (int) gamePlot.plotStart.getZ() - 1, (int) gamePlot.plotStart.getX() - 1, (int) gamePlot.plotStart.getZ() + FloorIsLava.GPD.plotSize + 1));
            FloorIsLava.WLR.addWorkload(new MakeBarrierWall((int) gamePlot.plotStart.getX() + FloorIsLava.GPD.plotSize, (int) gamePlot.plotStart.getZ() + FloorIsLava.GPD.plotSize, (int) gamePlot.plotStart.getX() + FloorIsLava.GPD.plotSize, (int) gamePlot.plotStart.getZ() - 1));
            FloorIsLava.WLR.addWorkload(new MakeBarrierWall((int) gamePlot.plotStart.getX() + FloorIsLava.GPD.plotSize, (int) gamePlot.plotStart.getZ() + FloorIsLava.GPD.plotSize, (int) gamePlot.plotStart.getX() - 1, (int) gamePlot.plotStart.getZ() + FloorIsLava.GPD.plotSize));
        }
        gamePlot.hasBordeers = true;
        int x_copy = x;
        int z_copy = z;
        int x_paste = (int)gamePlot.plotStart.getX();
        int z_paste = (int)gamePlot.plotStart.getZ();
        GameLobby gp = null;

        for(int x_index = 0; x_index < FloorIsLava.GPD.plotSize; x_index++){
            for(int z_index = 0; z_index < FloorIsLava.GPD.plotSize; z_index++){
                if(z_index == FloorIsLava.GPD.plotSize-1 && x_index == FloorIsLava.GPD.plotSize-1) gp = this;
                FloorIsLava.WLR.addWorkload(new GenerateGameTerrain(gp,x_copy+x_index,z_copy+z_index,x_paste+x_index,z_paste+z_index));
            }
        }
    }

    public void startGame(){
        Bukkit.getScheduler().runTaskTimer(FloorIsLava.plugin, (task) ->{
            if(countDown <= 0){
                announce( ChatColor.AQUA + "Teleporting...");

                gameBorder = Bukkit.createWorldBorder();
                gameBorder.setCenter(new Location(FloorIsLava.VOID_WORLD,(gamePlot.plotStart.getX()+(FloorIsLava.GPD.plotSize/2)),120,gamePlot.plotStart.getZ()+(FloorIsLava.GPD.plotSize/2)));
                gameBorder.setSize(FloorIsLava.GPD.plotSize);

                for (Player player: playerList) {
                    player.teleport(Tools.getSafeLocation(FloorIsLava.VOID_WORLD,gamePlot));
                    player.setWorldBorder(gameBorder);
                    savePlayerInfo(player);
                }
                runBackMusic();
                InviteLobby.INVITE_LOBBY_LIST.remove(Tools.getLobbyFromOwner(owner));
                beginLavaTimer();
                task.cancel();
            }
            announce(ChatColor.RED + "Game starts" + ChatColor.RESET+ " in " + ChatColor.AQUA + countDown);
            countDown--;
        },0,21L);
    }

    public void savePlayerInfo(Player player){
        previousInventoryList.put(player, player.getInventory().getContents());
        previousHealthList.put(player, player.getHealth());
        previousHungerList.put(player, player.getFoodLevel());
        previousXPList.put(player, player.getExp());

        player.getInventory().clear();
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setExp(0);
    }

    public void returnPlayerInfo(Player player){
        player.getInventory().setContents(previousInventoryList.get(player));
        player.setHealth(previousHealthList.get(player));
        player.setFoodLevel(previousHungerList.get(player));
        player.setExp(previousXPList.get(player));
    }

    public void runBackMusic(){
        Bukkit.getScheduler().runTaskTimer(FloorIsLava.plugin,playBackSongTask -> {
            if(gameON)
                playGameSound(Sound.MUSIC_DISC_PIGSTEP);
            else playBackSongTask.cancel();
        },0L,2820L);
    }

    public void announce(String msg){
        for (Player player:playerList) {
            player.sendMessage(msg);
        }
        for (Player player:specList) {
            player.sendMessage(msg);
        }
    }
    public void playGameSound(Sound sound){
        for (Player player:playerList) {
            player.playSound(gameBorder.getCenter(),sound,40,1);
        }
        for (Player player:specList) {
            player.playSound(gameBorder.getCenter(),sound,40,1);
        }
    }

    public void beginLavaTimer(){
        Bukkit.getScheduler().runTaskTimer(FloorIsLava.plugin, (task) ->{
            if(gameON){
                placeLava();
                if(!(lavaHeight >= 319))
                    lavaHeight += 3;
            }else task.cancel();
        }, 0, 200);
    }

    public void placeLava() {
        for(int y_lava = -63; y_lava < lavaHeight;y_lava++){
            FloorIsLava.WLR.addWorkload(new ElevateLava(gamePlot,y_lava));
        }
    }

    public void remove(Player leavingPlayer, boolean died){
        if(!died){

            announce(ChatColor.RED + leavingPlayer.getName() + " has left"+ ChatColor.RESET + " the game.");
            leavingPlayer.sendMessage("You have " + ChatColor.RED + "left the game.");
            leavingPlayer.teleport(previousLocationList.get(leavingPlayer));

            returnPlayerInfo(leavingPlayer);

            if(this.specList.contains(leavingPlayer)) {
                this.specList.remove(leavingPlayer);
                leavingPlayer.setGameMode(GameMode.SURVIVAL);
            }

            if(this.playerList.contains(leavingPlayer))
                this.playerList.remove(leavingPlayer);

        }else{
            this.playerList.remove(leavingPlayer);
            this.specList.add(leavingPlayer);

            announce(ChatColor.RED + leavingPlayer.getName() + " has died!"+ ChatColor.RESET + " They are now spectating.");
            leavingPlayer.playSound(leavingPlayer.getLocation(),Sound.ENTITY_PLAYER_BURP,1,1);
            leavingPlayer.setGameMode(GameMode.SPECTATOR);}

        if(playerList.size() == 1){
            winPlayer(playerList.get(0));
        }
    }

    public void winPlayer(Player player){
        playGameSound(Sound.UI_TOAST_CHALLENGE_COMPLETE);
        announce(ChatColor.RED + player.getName() + ChatColor.GOLD + " has won the game!");
        player.sendTitle(ChatColor.GREEN + "YOU WON","You are the "+ChatColor.RED + "LAVA " + ChatColor.GOLD +"MASTER",10,100,40);
        endGame();
    }

    public void endGame(){
        gameON = false;
        Bukkit.getScheduler().runTaskLater(FloorIsLava.plugin, (task) ->
                announce(ChatColor.RED + "Game ended!" + ChatColor.YELLOW + " Players will be teleported back shortly...")
                ,140L);

        Bukkit.getScheduler().runTaskLater(FloorIsLava.plugin, (task) ->{
            while (playerList.size() > 0){
                remove(playerList.get(0),false);
            }
            while (specList.size() > 0) {
                remove(specList.get(0), false);
            }

            gamePlot.inUse = false;
        },200L);
    }

}
