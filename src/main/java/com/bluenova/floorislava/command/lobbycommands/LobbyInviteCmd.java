package com.bluenova.floorislava.command.lobbycommands;

import com.bluenova.floorislava.command.subcommand.SubCommand;
import com.bluenova.floorislava.game.object.gamelobby.GameLobbyManager;
import com.bluenova.floorislava.game.object.invitelobby.InviteLobby;
import com.bluenova.floorislava.game.object.invitelobby.InviteLobbyManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
            sender.sendMessage("This command can only be run by a player.");
            return true; // Indicate command was handled (by showing error)
        }

        Player player = (Player) sender;
        // Player can invite only if they are owner of a lobby.

        if (!lobbyManager.isLobbyOwner(player)) {
            player.sendMessage("You are not the owner of any lobby.");
            return true;
        }

        InviteLobby lobby = lobbyManager.getLobbyFromOwner(player);

        // Check if player is already in a game
        if (gameManager.isPlayerIngame(player)) {
            player.sendMessage("You can only invite outside of a game.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("Usage: /fil lobby invite <player1> <player2> ...");
            return true;
        }

        ArrayList<Player> inviteList = new ArrayList<>();
        for (String arg : args) {
            Player targetPlayer = player.getServer().getPlayer(arg);
            if (targetPlayer == null) {
                player.sendMessage("Player " + arg + " cannot be found. (Maybe Offline?)");
                continue;
            }
            if (lobby.checkPlayerAlreadyInvited(targetPlayer)) {
                player.sendMessage("You have already invited " + targetPlayer.getName() + ".");
                continue;
            }
            inviteList.add(targetPlayer);
        }
        if (inviteList.isEmpty()) {
            player.sendMessage("No valid players to invite.");
            return true;
        }
        lobby.invitePlayers(inviteList);

        return false;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        for (Player onlinePlayer : sender.getServer().getOnlinePlayers()) {
            if (!onlinePlayer.getName().equalsIgnoreCase(sender.getName())) {
                completions.add(onlinePlayer.getName());
            }
        }
        return completions;
    }

    @Override
    public String getUsage() {
        return "/fil lobby invite <player1> <player2> ...";
    }

    @Override
    public String getPermission() {
        return "";
    }
}
