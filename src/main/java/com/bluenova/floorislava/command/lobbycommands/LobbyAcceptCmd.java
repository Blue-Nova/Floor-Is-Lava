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
import org.bukkit.Bukkit; // Import needed for getPlayerExact
import org.bukkit.ChatColor; // Keep for fallback messages if needed
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil; // Import for tab complete

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LobbyAcceptCmd implements SubCommand {

    private final InviteLobbyManager lobbyManager;
    private final GameLobbyManager gameManager;

    public LobbyAcceptCmd(InviteLobbyManager lobbyManager, GameLobbyManager gameManager) {
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

        // --- Add args length check ---
        if (args.length != 1) {
            MiniMessages.send(player, "command_usage.lobby_accept"); // Use key for usage
            return true;
        }
        // ---

        if (gameManager.isPlayerIngame(player)) {
            MiniMessages.send(player, "general.already_in_game_error"); // Use MiniMessages
            return true;
        }

        if (lobbyManager.isPlayerInLobby(player)) {
            MiniMessages.send(player, "lobby.already_in_lobby"); // Use MiniMessages
            return true;
        }

        String inviterName = args[0];
        // Consider getPlayerExact if case sensitivity matters
        Player inviter = Bukkit.getPlayer(inviterName);

        // Create placeholder resolver for the inviter's name
        TagResolver inviterPlaceholder = TagResolver.resolver(
                Placeholder.unparsed("player", inviterName) // Use unparsed for name as typed
        );

        if (inviter == null) {
            // Use MiniMessages with placeholder
            MiniMessages.send(player, "general.player_not_found", inviterPlaceholder);
            return true;
        }

        if (!lobbyManager.isLobbyOwner(inviter)) {
            // Use MiniMessages with placeholder (Add lobby.target_not_owner key to config)
            // Example YAML: lobby.target_not_owner: "<red>Player <aqua><player></aqua> does not own a lobby!</red>"
            MiniMessages.send(player, "lobby.target_not_owner", inviterPlaceholder);
            return true;
        }

        if (!lobbyManager.checkPlayerInvitedBy(inviter, player)) {
            // Use MiniMessages with placeholder (Add lobby.invite_not_pending key to config)
            // Example YAML: lobby.invite_not_pending: "<red>You do not have a pending invite from <aqua><player></aqua>.</red>"
            MiniMessages.send(player, "lobby.invite_not_pending", inviterPlaceholder);
            return true;
        }

        // Get the lobby using owner is slightly clearer
        InviteLobby lobby = lobbyManager.getLobbyFromOwner(inviter);
        if (lobby == null) {
            // Use MiniMessages (Add lobby.error_generic key to config if desired)
            // Example YAML: lobby.error_generic: "<red>An internal error occurred finding the lobby.</red>"
            MiniMessages.send(player, "lobby.error_generic");
            // Or send a generic Bukkit message for unexpected errors
            // player.sendMessage(ChatColor.RED + "Error getting lobby information.");
            return true;
        }

        // Delegate acceptance logic (this method should handle sending
        // lobby.invite_accepted_to_owner and lobby.invite_accepted_to_player using MiniMessages)
        lobby.inviteAccept(player);

        // Return true as command was handled
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        // Suggest online players for the first argument (inviter name)
        if(args.length == 1 && sender instanceof Player){
            String currentArg = args[0].toLowerCase();
            List<String> possibilities = new ArrayList<>();

            // Get all online players and filter out the sender
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (!onlinePlayer.equals(sender)) { // Check Player objects directly
                    possibilities.add(onlinePlayer.getName());
                }
            }
            // Filter based on current input
            StringUtil.copyPartialMatches(currentArg, possibilities, completions);
            Collections.sort(completions);
        }
        return completions; // Return filtered list or empty list
    }

    @Override
    public String getUsage() {
        // This message key could be loaded from config too, but fine hardcoded
        return "/fil lobby accept <Inviter (player name)>";
    }

    @Override
    public String getPermission() {
        return null;
    }
}