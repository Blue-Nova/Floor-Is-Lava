package com.bluenova.floorislava.command.lobbycommands;

import com.bluenova.floorislava.command.subcommand.SubCommand;
import com.bluenova.floorislava.game.object.Lobby;
import com.bluenova.floorislava.game.object.gamelobby.GameLobbyManager;
import com.bluenova.floorislava.game.object.invitelobby.InviteLobby;
import com.bluenova.floorislava.game.object.invitelobby.InviteLobbyManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LobbyListCmd implements SubCommand {

    private final InviteLobbyManager lobbyManager;
    private final GameLobbyManager gameManager;

    public LobbyListCmd(InviteLobbyManager lobbyManager, GameLobbyManager gameManager) {
        this.lobbyManager = lobbyManager;
        this.gameManager = gameManager;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be run by a player.");
            return true; // Indicate command was handled (by showing error)
        }
        Player player = (Player) sender;

        // 1. Check if player is already in a game
        if (gameManager.isPlayerIngame(player)) {
            player.sendMessage("You are already in a game.");
            return true;
        }

        // 2. Check if player is not in a lobby (owner or member)
        if (!lobbyManager.isPlayerInLobby(player)) {
            player.sendMessage("You are not in any lobby.");
            return true;
        }

        // 3. get the lobby
        InviteLobby lobby = lobbyManager.getLobbyFromPlayer(player);
        if (lobby == null) {
            player.sendMessage("Error getting lobby information.");
            return true;
        }

        // 4. List the players in the lobby
        ArrayList<Player> players = lobby.players;
        ArrayList<Player> invited = lobby.sentList;

        player.sendMessage("Players in your lobby:");
        for (Player p : players) {
            player.sendMessage(p.getName());
        }
        player.sendMessage("Invited players:");
        for (Player p : invited) {
            player.sendMessage(p.getName());
        }

        return false;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }

    @Override
    public String getUsage() {
        return "";
    }

    @Override
    public String getPermission() {
        return "";
    }
}
