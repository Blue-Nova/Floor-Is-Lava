package com.bluenova.floorislava.command.lobbycommands;

import com.bluenova.floorislava.command.subcommand.SubCommand;
import com.bluenova.floorislava.game.object.gamelobby.GameLobbyManager;
import com.bluenova.floorislava.game.object.invitelobby.InviteLobby;
import com.bluenova.floorislava.game.object.invitelobby.InviteLobbyManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
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
            sender.sendMessage("This command can only be run by a player.");
            return true; // Indicate command was handled (by showing error)
        }

        Player player = (Player) sender;

        // 1. Check if player is already in a game
        if (gameManager.isPlayerIngame(player)) {
            player.sendMessage("You cannot accept invites during a game.");
            return true;
        }

        // 2. Check if player is not in a lobby (owner or member)
        if (lobbyManager.isPlayerInLobby(player)) {
            player.sendMessage("You need to leave your lobby first.");
            return true;
        }

        // 4. find inviter from name
        String inviterName = args[0];
        Player inviter = player.getServer().getPlayer(inviterName);
        if (inviter == null) {
            player.sendMessage("Player " + inviterName + " cannot be found. (Maybe Offline?)");
            return true;
        }
        if (!lobbyManager.isLobbyOwner(inviter)) {
            player.sendMessage("Player " + inviterName + " is not the owner of any lobby.");
            return true;
        }
        if (!lobbyManager.checkPlayerInvitedBy(inviter, player)) {
            player.sendMessage("You have not been invited by " + inviterName + ".");
            return true;
        }

        // 4. Get the lobby
        InviteLobby lobby = lobbyManager.getLobbyFromPlayer(inviter);
        if (lobby == null) {
            player.sendMessage("Error getting lobby information.");
            return true;
        }
        lobby.inviteAccept(player);
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
        return "/fil lobby accept <Inviter (player name)>";
    }

    @Override
    public String getPermission() {
        return null;
    }
}
