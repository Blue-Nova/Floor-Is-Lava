package com.bluenova.floorislava.command.gamecommands;

import com.bluenova.floorislava.command.subcommand.SubCommand;
import com.bluenova.floorislava.game.object.gamelobby.GameLobbyManager;
import com.bluenova.floorislava.game.object.invitelobby.InviteLobby;
import com.bluenova.floorislava.game.object.invitelobby.InviteLobbyManager;
import com.sk89q.worldedit.WorldEditException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class GameStartCmd implements SubCommand {

    private final GameLobbyManager gameManager;
    private final InviteLobbyManager lobbyManager;

    public GameStartCmd(GameLobbyManager gameManager, InviteLobbyManager lobbyManager) {
        this.gameManager = gameManager;
        this.lobbyManager = lobbyManager;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        // 1. Check if sender is a Player
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be run by a player.");
            return true; // Indicate command was handled (by showing error)
        }
        Player player = (Player) sender;

        // 2. Check if player is already in a game
        if (gameManager.isPlayerIngame(player)) {
            player.sendMessage("You are already in a game.");
            return true;
        }
        // 3. Check if player is owner of a lobby
        if (!lobbyManager.isLobbyOwner(player)) {
            player.sendMessage("You are not the owner of a lobby.");
            return true;
        }
        InviteLobby lobby = lobbyManager.getLobbyFromOwner(player);
        try{
            lobby.startGame();
            return true;
        }catch (WorldEditException ex) {
            player.sendMessage("An error occurred while starting the game.");
            ex.printStackTrace();
            return true;
        }
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
        return null;
    }
}
