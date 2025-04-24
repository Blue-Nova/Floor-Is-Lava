package com.bluenova.floorislava.command.gamecommands;

import com.bluenova.floorislava.command.subcommand.SubCommand;
import com.bluenova.floorislava.game.object.gamelobby.GameLobbyManager;
import com.bluenova.floorislava.game.object.invitelobby.InviteLobby;
import com.bluenova.floorislava.game.object.invitelobby.InviteLobbyManager;
// Import MiniMessages service
import com.bluenova.floorislava.util.messages.MiniMessages;
// Import ChatColor for potential fallback/error messages if needed immediately
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList; // Import needed
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
            // Use new message system
            MiniMessages.send(sender, "general.not_a_player");
            return true;
        }
        Player player = (Player) sender;

        // 2. Check if player is already in a game
        if (gameManager.isPlayerIngame(player)) {
            // Use new message system
            MiniMessages.send(player, "general.already_in_game_error");
            return true;
        }

        // 3. Check if player is owner of a lobby
        if (!lobbyManager.isLobbyOwner(player)) {
            // Use new message system
            MiniMessages.send(player, "lobby.not_lobby_owner");
            return true;
        }

        // 4. Get the lobby
        InviteLobby lobby = lobbyManager.getLobbyFromOwner(player);
        if (lobby == null) {
            // Should not happen if isLobbyOwner passed, but good practice
            player.sendMessage(ChatColor.RED + "Error: Could not find your lobby."); // Fallback message
            return true;
        }

        // 5. Check lobby size (Moved check here from old MainCommand)
        // Assuming lobby.players holds joined list including owner
        if (lobby.players.size() < 2) {
            MiniMessages.send(player, "lobby.start_lobby_too_small");
            return true;
        }

        // 6. Tell GameManager to create the game
        // This now handles getting a plot and creating the GameLobby instance.
        // The "No free plots" message is handled inside gameManager.createLobby
        // based on the implementation we saw earlier.
        gameManager.createLobby(new ArrayList<>(lobby.players), player);

        // Optional: Send a "Starting..." message? The GameLobby countdown handles the main alerts.
        // MiniMessages.send(player, "lobby.starting_game"); // If you add this key to config

        // NOTE: The removal of the InviteLobby from InviteLobbyManager needs to be handled.
        // This could happen within gameManager.createLobby on success,
        // or the GameLobby could notify InviteLobbyManager when it fully starts.

        return true; // Command was handled
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        // Correct, no arguments after "start"
        return Collections.emptyList();
    }

    @Override
    public String getUsage() {
        // Assuming this is for /fil lobby start based on logic
        return "/fil lobby start - Starts the game from your lobby.";
    }

    @Override
    public String getPermission() {
        return "floorislava.lobby.start";
    }
}