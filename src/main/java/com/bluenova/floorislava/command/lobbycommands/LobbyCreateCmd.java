package com.bluenova.floorislava.command.lobbycommands;

import com.bluenova.floorislava.command.subcommand.SubCommand;
import com.bluenova.floorislava.config.MessageConfig;
import com.bluenova.floorislava.game.object.gamelobby.GameLobbyManager;
import com.bluenova.floorislava.game.object.invitelobby.InviteLobbyManager;
import com.bluenova.floorislava.util.messages.MessageUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

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
        // 1. Check if sender is a Player
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be run by a player.");
            return true; // Indicate command was handled (by showing error)
        }
        Player player = (Player) sender;
        UUID playerUUID = player.getUniqueId(); // Use UUID for lookups

        // 2. Check if player is already in a game
        // Uses the injected gameManager instance
        if (gameManager.isPlayerIngame(player)) { // Assuming manager method takes Player for now
            // Ideally: gameManager.isPlayerInGame(playerUUID)
            MessageUtils.sendFILMessage(player, MessageConfig.getInstance().getAlreadyInGame());
            return true;
        }

        // 3. Check if player is already in a lobby (owner or member)
        // Uses the injected lobbyManager instance
        if (lobbyManager.isPlayerInLobby(player)) { // Assuming manager method takes Player for now
            // Ideally: lobbyManager.isPlayerInLobby(playerUUID)
            MessageUtils.sendFILMessage(player, MessageConfig.getInstance().getAlreadyInLobby());
            return true;
        }

        // 4. All checks passed, create the lobby
        // Uses the injected lobbyManager instance
        lobbyManager.createLobby(player);
        MessageUtils.sendFILMessage(player, MessageConfig.getInstance().getLobbyCreated());

        return true; // Command successfully handled
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        // No tab completions for this command
        return Collections.emptyList();
    }

    @Override
    public String getUsage() {
        return "/fil lobby create - Creates a new game lobby.";
    }

    @Override
    public String getPermission() {
        return "floorislava.lobby.create"; // Example permission node
    }
}