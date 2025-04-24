package com.bluenova.floorislava.command.lobbycommands;

import com.bluenova.floorislava.command.subcommand.SubCommand;
import com.bluenova.floorislava.game.object.gamelobby.GameLobby;
import com.bluenova.floorislava.game.object.gamelobby.GameLobbyManager;
import com.bluenova.floorislava.game.object.invitelobby.InviteLobby;
import com.bluenova.floorislava.game.object.invitelobby.InviteLobbyManager;
// Import MiniMessages
import com.bluenova.floorislava.util.messages.MiniMessages;
// Other imports
import org.bukkit.ChatColor; // Keep for fallback/error
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

        if (!(sender instanceof Player)) {
            MiniMessages.send(sender, "general.not_a_player"); // Use MiniMessages
            return true;
        }
        Player player = (Player) sender;

        // Check Lobby
        if (lobbyManager.isPlayerInLobby(player)) {
            InviteLobby lobby = lobbyManager.getLobbyFromPlayer(player);
            if (lobby != null) {
                // Delegate removal. InviteLobby.removePlayer should handle all messaging
                // (e.g., sending "lobby.leaving_lobby_feedback" to the player,
                // "lobby.player_left_notification" to others, handling disband messages)
                // AND notify lobbyManager to update its maps.
                lobby.removePlayer(player);
            } else {
                player.sendMessage(ChatColor.RED + "Error: Found you in lobby map, but couldn't retrieve lobby object.");
                // MiniMessages.send(player, "lobby.error_generic"); // Use if you added this key
            }
            return true; // Handled
        }

        // Check Game
        if (gameManager.isPlayerIngame(player)) {
            GameLobby game = gameManager.getGameFromPlayer(player);
            if (game != null) {
                // Delegate removal. GameLobby.remove should handle all messaging
                // (e.g., sending "game.player_left_game" broadcast)
                // AND notify gameManager/PlotManager when game ends.
                game.remove(player, false); // false = not a death
            } else {
                player.sendMessage(ChatColor.RED + "Error: Found you in game map, but couldn't retrieve game object.");
                // MiniMessages.send(player, "game.error_generic"); // If you add a generic game error key
            }
            return true; // Handled
        }

        // Player is not in a lobby or game
        // --- Incorrect return ---
        // return false;
        // Use MiniMessages - "lobby.not_in_lobby" is the closest existing key
        MiniMessages.send(player, "lobby.not_in_lobby");
        return true; // Command was handled
        // ---
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        // Correct, no args
        return Collections.emptyList();
    }

    @Override
    public String getUsage() {
        // Correct
        return "/fil lobby leave";
    }

    @Override
    public String getPermission() {
        // Corrected to null or specific node
        // return ""; // Problematic
        return null; // Example: Allow anyone to leave
        // OR
        // return "floorislava.lobby.leave";
    }
}