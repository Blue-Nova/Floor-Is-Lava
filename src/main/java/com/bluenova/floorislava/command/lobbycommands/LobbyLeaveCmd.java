package com.bluenova.floorislava.command.lobbycommands;

import com.bluenova.floorislava.command.subcommand.SubCommand;
import com.bluenova.floorislava.game.object.gamelobby.GameLobby;
import com.bluenova.floorislava.game.object.gamelobby.GameLobbyManager;
import com.bluenova.floorislava.game.object.invitelobby.InviteLobby;
import com.bluenova.floorislava.game.object.invitelobby.InviteLobbyManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class LobbyLeaveCmd implements SubCommand {

    private final InviteLobbyManager lobbyManager;
    private final GameLobbyManager gameManager;

    public LobbyLeaveCmd(InviteLobbyManager lobbyManager, GameLobbyManager gameManager) {
        this.lobbyManager = lobbyManager;
        this.gameManager = gameManager;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        // 1. Check if sender is a Player
        if (!(sender instanceof org.bukkit.entity.Player)) {
            sender.sendMessage("This command can only be run by a player.");
            return true; // Indicate command was handled (by showing error)
        }

        Player player = (Player) sender;

        // 2. Check if player is in a lobby
        if (lobbyManager.isPlayerInLobby(player)) {
            InviteLobby lobby = lobbyManager.getLobbyFromPlayer(player);
            lobby.removePlayer(player);
            return true;
        }

        // 3. Check if player is in a game
        if (gameManager.isPlayerIngame(player)) {
            GameLobby lobby = gameManager.getGameFromPlayer(player);
            lobby.remove(player, false);
            return true;
        }

        return false;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }

    @Override
    public String getUsage() {
        return "/fil lobby leave";
    }

    @Override
    public String getPermission() {
        return "";
    }
}
