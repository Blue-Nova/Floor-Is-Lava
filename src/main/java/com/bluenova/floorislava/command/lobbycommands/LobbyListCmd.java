package com.bluenova.floorislava.command.lobbycommands;

import com.bluenova.floorislava.command.subcommand.SubCommand;
import com.bluenova.floorislava.game.object.gamelobby.GameLobbyManager;
import com.bluenova.floorislava.game.object.invitelobby.InviteLobby;
import com.bluenova.floorislava.game.object.invitelobby.InviteLobbyManager;
// Import MiniMessages and related classes
import com.bluenova.floorislava.util.messages.MiniMessages;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
// Other imports
import org.bukkit.ChatColor; // Keep for fallback/error
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
            MiniMessages.send(sender, "general.not_a_player"); // Use MiniMessages
            return true;
        }
        Player player = (Player) sender;

        if (gameManager.isPlayerIngame(player)) {
            MiniMessages.send(player, "general.already_in_game_error"); // Use MiniMessages
            return true;
        }

        if (!lobbyManager.isPlayerInLobby(player)) {
            MiniMessages.send(player, "lobby.not_in_lobby"); // Use MiniMessages
            return true;
        }

        InviteLobby lobby = lobbyManager.getLobbyFromPlayer(player);
        if (lobby == null) {
            // Use MiniMessages (assuming key lobby.error_generic added)
            MiniMessages.send(player, "lobby.error_generic");
            // player.sendMessage(ChatColor.RED+"Error getting lobby information."); // Fallback
            return true;
        }

        // --- Refactored Player Listing using MiniMessages ---
        ArrayList<Player> joinedPlayers = lobby.players; // Assumes 'players' is the joined list
        ArrayList<Player> invitedPlayers = lobby.sentList;

        MiniMessages.send(player, "lobby.list_header");
        MiniMessages.send(player, "lobby.list_joined_header");

        if (joinedPlayers == null || joinedPlayers.isEmpty()) { // Check if null just in case
            MiniMessages.send(player, "lobby.list_empty_joined");
        } else {
            for (Player p : joinedPlayers) {
                if (p == null) continue; // Safety check
                TagResolver playerPlaceholder = TagResolver.resolver(
                        Placeholder.unparsed("player_name", p.getName())
                );
                if (p.equals(lobby.getOwner())) {
                    MiniMessages.send(player, "lobby.list_entry_owner", playerPlaceholder);
                } else {
                    MiniMessages.send(player, "lobby.list_entry", playerPlaceholder);
                }
            }
        }

        MiniMessages.send(player, "lobby.list_invited_header");

        if (invitedPlayers == null || invitedPlayers.isEmpty()) {
            MiniMessages.send(player, "lobby.list_empty_invited");
        } else {
            for (Player p : invitedPlayers) {
                if (p == null) continue; // Safety check
                TagResolver playerPlaceholder = TagResolver.resolver(
                        Placeholder.unparsed("player_name", p.getName())
                );
                MiniMessages.send(player, "lobby.list_entry", playerPlaceholder);
            }
        }

        MiniMessages.send(player, "lobby.list_footer");
        // --- End Refactored Listing ---

        // Return true because command was handled
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        // Correct, no args
        return Collections.emptyList();
    }

    @Override
    public String getUsage() {
        // Updated usage
        return "/fil lobby list - Lists players in your current lobby.";
    }

    @Override
    public String getPermission() {
        // Updated permission (null = anyone in lobby can use)
        // return ""; // Problematic
        return null;
        // OR return "floorislava.lobby.list";
    }
}