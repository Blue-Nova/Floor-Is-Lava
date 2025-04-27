package com.bluenova.floorislava.command.lobbycommands;

import com.bluenova.floorislava.command.subcommand.SubCommand;
import com.bluenova.floorislava.game.object.gamelobby.GameLobbyManager;
import com.bluenova.floorislava.game.object.invitelobby.InviteLobbyManager;
import com.bluenova.floorislava.util.messages.MiniMessages;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class LobbyCreateCmd implements SubCommand {

    // Dependencies injected via constructor
    private final InviteLobbyManager lobbyManager;
    private final GameLobbyManager gameManager;

    public LobbyCreateCmd(InviteLobbyManager lobbyManager, GameLobbyManager gameManager) {
        this.lobbyManager = lobbyManager;
        this.gameManager = gameManager;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            // Correctly uses MiniMessages and correct key
            MiniMessages.send(sender, "general.not_a_player");
            return true;
        }
        Player player = (Player) sender;
        // ... UUID lookup ideal later ...

        if (gameManager.isPlayerIngame(player)) {
            // MiniMessages.send(player, "game.already_in_game");
            MiniMessages.send(player, "general.already_in_game_error"); // CORRECTED KEY
            return true;
        }

        if (lobbyManager.isPlayerInLobby(player)) {
            // MiniMessages.send(player, "AlreadyInLobby");
            MiniMessages.send(player, "lobby.already_in_lobby"); // CORRECTED KEY
            return true;
        }

        lobbyManager.createLobby(player);
        // MiniMessages.send(player, "LobbyCreated");
        MiniMessages.send(player, "lobby.created"); // CORRECTED KEY

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        // No tab completions for this command
        return Collections.emptyList();
    }

    @Override
    public String getUsage() {
        // Correct
        return "/fil lobby create - Creates a new game lobby.";
    }

    @Override
    public String getPermission() {
        // Correct (uses a valid node format)
        return "floorislava.lobby.create";
    }
}