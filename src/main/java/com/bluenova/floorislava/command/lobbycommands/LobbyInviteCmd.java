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
import org.bukkit.Bukkit; // Import needed
import org.bukkit.ChatColor; // Keep for fallback/error
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil; // Import for tab complete

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors; // For tab complete stream

public class LobbyInviteCmd implements SubCommand {

    private final InviteLobbyManager lobbyManager;
    private final GameLobbyManager gameManager;

    public LobbyInviteCmd(InviteLobbyManager lobbyManager, GameLobbyManager gameManager) {
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

        if (!lobbyManager.isLobbyOwner(player)) {
            MiniMessages.send(player, "lobby.not_lobby_owner"); // Use MiniMessages
            return true;
        }

        InviteLobby lobby = lobbyManager.getLobbyFromOwner(player);
        if (lobby == null) {
            player.sendMessage(ChatColor.RED + "Error: Could not find your lobby."); // Fallback
            return true;
        }

        if (gameManager.isPlayerIngame(player)) {
            MiniMessages.send(player, "general.already_in_game_error"); // Use MiniMessages
            return true;
        }

        if (args.length == 0) {
            MiniMessages.send(player, "command_usage.lobby_invite"); // Use MiniMessages
            return true;
        }

        ArrayList<Player> inviteList = new ArrayList<>();
        ArrayList<String> failedArgs = new ArrayList<>(); // Track args that failed, simpler than Player objects

        for (String arg : args) {
            Player targetPlayer = Bukkit.getPlayer(arg); // Consider getPlayerExact
            TagResolver targetPlaceholder = TagResolver.resolver(Placeholder.unparsed("player", arg)); // Placeholder with name as typed

            if (targetPlayer == null) {
                MiniMessages.send(player, "general.player_not_found", targetPlaceholder); // Use MiniMessages
                failedArgs.add(arg);
                continue;
            }
            if (targetPlayer.equals(player)) { // Prevent inviting self
                MiniMessages.send(player, "lobby.cannot_invite_self"); // Add this key to YAML: "<red>You cannot invite yourself!</red>"
                failedArgs.add(arg);
                continue;
            }
            if (lobbyManager.isPlayerInLobby(targetPlayer)){ // Prevent inviting players already in ANY lobby
                MiniMessages.send(player, "lobby.invited_player_in_lobby_error", Placeholder.unparsed("player", targetPlayer.getName()));
                failedArgs.add(arg);
                continue;
            }

            // Use the existing check in InviteLobby
            if (lobby.checkPlayerAlreadyInvited(targetPlayer)) {
                // Use MiniMessages with placeholder
                MiniMessages.send(player, "lobby.already_invited_error", Placeholder.unparsed("player", targetPlayer.getName()));
                failedArgs.add(arg);
                continue;
            }
            inviteList.add(targetPlayer);
        }

        if (inviteList.isEmpty()) {
            // Add this key to YAML: "<red>No valid players found to invite.</red>"
            MiniMessages.send(player, "lobby.invite_no_valid_targets");
            return true;
        }

        // Delegate inviting to the lobby object
        // Ensure InviteLobby.invitePlayers uses MiniMessages internally for its feedback
        // Pass failedArgs if invitePlayers needs it, otherwise remove failedArgs tracking
        lobby.invitePlayers(inviteList); // Assuming this method handles its own messaging now

        // Return true as command was handled
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        // Suggest online players for any argument slot after "invite"
        if (args.length >= 1 && sender instanceof Player) {
            String currentArg = args[args.length - 1].toLowerCase();
            List<String> possibilities = new ArrayList<>();
            InviteLobby lobby = lobbyManager.getLobbyFromOwner((Player) sender);

            for(Player onlinePlayer : Bukkit.getOnlinePlayers()){
                // Filter self
                if(onlinePlayer.equals(sender)) continue;

                // Filter players already in *any* lobby (stronger filter)
                if(lobbyManager.isPlayerInLobby(onlinePlayer)) continue;

                // Filter players already invited by *this* lobby (if sender is owner)
                if(lobby != null && lobby.checkPlayerAlreadyInvited(onlinePlayer)) {
                    continue;
                }

                possibilities.add(onlinePlayer.getName());
            }
            // Filter based on current input
            StringUtil.copyPartialMatches(currentArg, possibilities, completions);
            Collections.sort(completions);
        }
        return completions;
    }

    @Override
    public String getUsage() {
        // Could load this from config too: command_usage.lobby_invite_description
        return "/fil lobby invite <player1> <player2> ...";
    }

    @Override
    public String getPermission() {
        return null;
    }
}