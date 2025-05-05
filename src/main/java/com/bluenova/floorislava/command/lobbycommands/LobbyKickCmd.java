package com.bluenova.floorislava.command.lobbycommands;

import com.bluenova.floorislava.command.subcommand.SubCommand;
import com.bluenova.floorislava.game.object.gamelobby.GameLobbyManager;
import com.bluenova.floorislava.game.object.invitelobby.InviteLobby;
import com.bluenova.floorislava.game.object.invitelobby.InviteLobbyManager;
import com.bluenova.floorislava.util.messages.MiniMessages;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LobbyKickCmd implements SubCommand {

    private final InviteLobbyManager lobbyManager;
    private final GameLobbyManager gameManager;

    public LobbyKickCmd(InviteLobbyManager lobbyManager, GameLobbyManager gameManager) {
        this.lobbyManager = lobbyManager;
        this.gameManager = gameManager;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        // Check if the sender is a player
        if (!(sender instanceof Player)) {
            MiniMessages.send(sender, "general.not_a_player"); // Use MiniMessages
            return true;
        }
        Player player = (Player) sender;

        // check if args length at least 1
        if (args.length < 1) {
            MiniMessages.send(player, "command_usage.lobby_kick"); // Use MiniMessages
            return true;
        }

        // Check if the player is the lobby owner
        if (!lobbyManager.isLobbyOwner(player)) {
            MiniMessages.send(player, "lobby.not_lobby_owner"); // Use MiniMessages
            return true;
        }

        // get lobby from owner
        InviteLobby lobby = lobbyManager.getLobbyFromOwner(player);
        if (lobby == null) {
            MiniMessages.send(player, "lobby.error_generic"); // Use MiniMessages
            return true;
        }

        // loop through args and check if each player is online and in the owner lobby
        ArrayList<Player> playersToKick = new ArrayList<>();
        ArrayList<String> playersNotFound = new ArrayList<>();
        for (String arg : args) {
            Player target = player.getServer().getPlayer(arg);
            if (target == null) {
                playersNotFound.add(arg);
                continue;
            }

            if (!lobby.players.contains(target)) {
                playersNotFound.add(arg);
                continue;
            }

            if (target == player) {
                MiniMessages.send(player, "lobby.kick_self_error"); // Use MiniMessages
                continue;
            }

            playersToKick.add(target);
            // kick the player from the lobby
            lobby.kickPlayers(playersToKick);
        }

        return false;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        ArrayList<String> completions = new ArrayList<>();
        InviteLobby lobby = lobbyManager.getLobbyFromOwner((Player) sender);
        if (lobby == null) {
            return Collections.emptyList(); // No lobby, no completions
        }
        for (Player p : lobby.players){
            if (p.equals(sender)) continue; // Don't suggest self
            completions.add(p.getName());
        }
        return completions;
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
