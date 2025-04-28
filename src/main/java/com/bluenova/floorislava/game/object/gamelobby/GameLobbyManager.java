package com.bluenova.floorislava.game.object.gamelobby;

import com.bluenova.floorislava.FloorIsLava;
import com.bluenova.floorislava.config.PlayerDataManager;
import com.bluenova.floorislava.game.object.GamePlot;
import com.bluenova.floorislava.util.messages.PluginLogger;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;

public class GameLobbyManager {

    private final List<GameLobby> gameLobbyList = new ArrayList<>();
    private final PluginLogger pluginLogger;
    private final PlayerDataManager playerDataManager;

    public GameLobbyManager(PluginLogger pluginLogger, PlayerDataManager playerDataManager) {
        this.pluginLogger = pluginLogger;
        this.playerDataManager = playerDataManager;
    }

    // adds a lobby to the manager
    public void createLobby(ArrayList<Player> players,Player owner) {

        GamePlot gp = FloorIsLava.getGamePlotDivider().prepareFirstEmptyPlot();
        if (gp == null){
            owner.sendMessage(ChatColor.RED + "No free plots" + ChatColor.RESET + " available. Please wait a moment for a game to end" +
                    "or message a server admin to increase max amount of plots allowed");
            return;
        }
        GameLobby lobby = new GameLobby(FloorIsLava.getInstance(),pluginLogger, players, owner,
                FloorIsLava.getInviteLobbyManager(),this,FloorIsLava.getWorkloadRunnable(),
                FloorIsLava.getGamePlotDivider(),FloorIsLava.getVoidWorld(),FloorIsLava.getFILRegionManager(),gp);
        gameLobbyList.add(lobby);
    }

    public boolean isPlayerIngame(Player player) {
        for (GameLobby game : gameLobbyList) {
            if (game.players.contains(player)) return true;
        }
        for (GameLobby game : gameLobbyList) {
            if (game.specList.contains(player)) return true;
        }
        return false;
    }

    public GameLobby getGameFromPlayer(Player player) {
        for (GameLobby game : gameLobbyList) {
            if (game.players.contains(player)) return game;
        }
        for (GameLobby game : gameLobbyList) {
            if (game.specList.contains(player)) return game;
        }
        return null;
    }

    public boolean savePlayerData(Player player) {
        return playerDataManager.savePlayerData(player);
    }

    public boolean restorePlayerData(Player player) {
        return playerDataManager.restoreStateIfNecessary(player);
    }

    public void shutdownAllGames() {
        for (GameLobby game : gameLobbyList) {
            game.shutdown();
        }
        // WIP
    }
}
