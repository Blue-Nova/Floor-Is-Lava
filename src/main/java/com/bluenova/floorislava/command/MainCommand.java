package com.bluenova.floorislava.command;


import com.bluenova.floorislava.game.object.InviteLobby;
import com.bluenova.floorislava.util.Tools;
import com.sk89q.worldedit.WorldEditException;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import static com.bluenova.floorislava.config.json.message.Message.*;

public class MainCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player)) return true;
        Player commander = (Player) sender;
        if(command.getName().equalsIgnoreCase("fil")) {
            if (args.length==0 || args[0].equalsIgnoreCase("help")){
                showHelp(commander);
            }else if (args[0].equalsIgnoreCase("lobby")) {
                if(args[1].equalsIgnoreCase("create")){
                    createLobbyCommand(commander);
                }else if (args[1].equalsIgnoreCase("list")) {
                    listLobbyPlayersCommand(commander);
                }else if (args[1].equalsIgnoreCase("leave")) {
                    leaveLobbyCommand(commander);
                }else if (args[1].equalsIgnoreCase("remove")) {
                    ArrayList<String> playersToRemove = new ArrayList<>();
                    for (int i = 1; i < args.length; i++) {
                        playersToRemove.add(args[i]);
                    }
                    removeCommand(commander, playersToRemove);
                }else if (args[1].equalsIgnoreCase("start")) {
                    startCommand(commander);
                }else{
                    showHelp(commander);
                }
            }else if (args[0].equalsIgnoreCase("invite")) {
                if(args.length == 1){
                    commander.sendMessage("[FIL] wrong usage; /fil invite (accept) <playername>");
                    return false;
                }
                if(args[1].equalsIgnoreCase("accept")){
                    acceptCommand(commander,args[2]);
                }else{
                    ArrayList<String> playersToInvite = new ArrayList<>();
                    for (int i = 1; i < args.length; i++) {
                        playersToInvite.add(args[i]);
                    }
                    inviteCommand(commander,playersToInvite);
                }
            }else if (args[0].equalsIgnoreCase("game")) {
                if (args[1].equalsIgnoreCase("leave")){
                    leaveCommand(commander);
                }
            }
        }

        return false;
    }

    public void showHelp(Player p){

    }

    public void createLobbyCommand(Player player) {
        if (Tools.isPlayerIngame(player)) {
            player.sendMessage(PLAYER_ALREADY_INGAME.replaceColor().replacePrefix().format());
            return;
        }
        new InviteLobby(player);
        player.sendMessage(CREATE_LOBBY_SUCCESS.replaceColor().replacePrefix().format());
    }

    public void listLobbyPlayersCommand(Player player) {
        if (!Tools.isPlayerInLobby(player)) {
            player.sendMessage(PLAYER_NO_LOBBY.replaceColor().replacePrefix().format());
            return;
        }
        Tools.getLobbyFromOwner(player).listPlayers();
    }

    public void leaveLobbyCommand(Player player) {
        if (!Tools.isPlayerInLobby(player)) {
            player.sendMessage(PLAYER_NO_LOBBY.replaceColor().replacePrefix().format());
            return;
        }
        if (Tools.isPlayerIngame(player)) {
            Tools.getGameFromPlayer(player).remove(player, false);
            return;
        }
        if (!(Tools.isLobbyOwner(player) || !Tools.isPlayerInLobby(player))) {
            player.sendMessage(PLAYER_NO_LOBBY.replaceColor().replacePrefix().format());
        } else {
            InviteLobby lobby;
            if (Tools.isLobbyOwner(player)) lobby = Tools.getLobbyFromOwner(player);
            else lobby = Tools.getLobbyFromPlayer(player);
            lobby.removePlayer(player);
        }
    }

    public void removeCommand(Player player, ArrayList<String> args) {
        if (!Tools.isPlayerInLobby(player)) {
            player.sendMessage(PLAYER_NO_LOBBY.replaceColor().replacePrefix().format());
            return;
        }
        if (!Tools.isLobbyOwner(player)) {
            player.sendMessage(PLAYER_NOT_LOBBY_OWNER.replaceColor().replacePrefix().format());
            return;
        }
        for (String playername : args) {
            if ((Bukkit.getPlayer(playername) == null) || !(Bukkit.getPlayer(playername).isOnline())) {
                player.sendMessage(PLAYER_REMOVE_UNKNOWN.replaceColor().replacePrefix().replaceRemove(playername).format());
                continue;
            }
            Tools.getLobbyFromOwner(player).removePlayer(Bukkit.getPlayer(playername));
        }
    }

    public void startCommand(Player player) {
        if (!Tools.isPlayerInLobby(player)) {
            player.sendMessage(PLAYER_NO_LOBBY.replaceColor().replacePrefix().format());
            return;
        }
        if (Tools.isPlayerIngame(player)) {
            player.sendMessage(PLAYER_ALREADY_INGAME.replaceColor().replacePrefix().format());
            return;
        }
        if (!Tools.isLobbyOwner(player)) {
            player.sendMessage(PLAYER_NOT_LOBBY_OWNER.replaceColor().replacePrefix().format());
            return;
        }
        if (!(Tools.getLobbyFromOwner(player).joinedList.size() >= 2)) {
            player.sendMessage(LOBBY_NOT_ENOUGH.replaceColor().replacePrefix().replaceMinPlayerCount(2).format());
            return;
        }
        try {
            Tools.getLobbyFromOwner(player).startGame();
        } catch (WorldEditException ex) {
            throw new RuntimeException(ex);
        }
    }
    public void inviteCommand(Player player, ArrayList<String> users) {
        if (!Tools.isPlayerInLobby(player)) {
            player.sendMessage(PLAYER_NO_LOBBY.replaceColor().replacePrefix().format());
            return;
        }
        if (!Tools.isLobbyOwner(player)) {
            player.sendMessage(PLAYER_NOT_LOBBY_OWNER.replaceColor().replacePrefix().format());
            return;
        }
        if (users.size() == 0) {
            player.sendMessage(INVITE_USAGE.replaceColor().replacePrefix().format());
            return;
        }
        InviteLobby lobby = Tools.getLobbyFromOwner(player);
        boolean sentOneInvite = false;
        for (String playerName : users) {
            try{Bukkit.getPlayer(playerName);}
            catch(Exception e){player.sendMessage(playerName + " doesnt exist/isnt online, skipping");
                continue;}
            if (Bukkit.getPlayer(playerName) == null || !Bukkit.getPlayer(playerName).isOnline()) {
                player.sendMessage(INVITE_UNKNOWN_PLAYER.replaceColor().replacePrefix().format());
                continue;
            }
            Player invitedPlayer = Bukkit.getPlayer(playerName);
            if (Tools.checkPlayerInvitedBy(invitedPlayer, player)) continue;
            if (Tools.isPlayerInLobby(invitedPlayer) || Tools.isLobbyOwner(invitedPlayer)) {
                player.sendMessage(INVITE_FAIL_INLOBBY.replaceColor().replacePrefix().replaceReceiver(invitedPlayer.getDisplayName()).format());
                continue;
            }
            lobby.invitePlayer(invitedPlayer);
            sentOneInvite = true;
        }
        if (!sentOneInvite)
            player.sendMessage(INVITE_NONE.replaceColor().replacePrefix().format());
    }
    public void acceptCommand(Player player, String acceptingPlayer) {
        if (acceptingPlayer==null) {
            player.sendMessage(ACCEPT_NO_ARGS.replaceColor().replacePrefix().format());
            return;
        }
        if (Tools.isPlayerInLobby(player) || Tools.isLobbyOwner(player)) {
            player.sendMessage(PLAYER_ALREADY_INLOBBY.replaceColor().replacePrefix().format());
            return;
        }
        if (Bukkit.getPlayer(acceptingPlayer) == null || !(Bukkit.getPlayer(acceptingPlayer).isOnline())) {
            player.sendMessage(ACCEPT_UNKNOWN_PLAYER.replaceColor().replacePrefix().replaceSender(acceptingPlayer).format());
            return;
        }
        Player inviter = Bukkit.getPlayer(acceptingPlayer);
        if (!Tools.isLobbyOwner(inviter)) {
            player.sendMessage(ACCEPT_SENDER_NOLOBBY.replaceColor().replacePrefix().replaceSender(inviter.getDisplayName()).format());
        }
        if (!Tools.checkPlayerInvitedBy(inviter, player)) {
            player.sendMessage(ACCEPT_NO_INVITE.replaceColor().replacePrefix().replaceSender(inviter.getDisplayName()).format());
        } else {
            player.sendMessage(ACCEPT_RECEIVER.replaceColor().replacePrefix().replaceSender(inviter.getDisplayName()).format());
            inviter.sendMessage(ACCEPT_SENDER.replaceColor().replacePrefix().replaceReceiver(player.getName()).format());
            Tools.getLobbyFromOwner(inviter).inviteAccept(player);
        }
    }
    public void leaveCommand(Player player) {
        if (player.hasPermission("fil.command.fil.game.noleave")) {
            player.sendMessage(PLAYER_NO_PERMISSION.replaceColor().format());
            return;
        }
        if (Tools.isPlayerIngame(player)) {
            Tools.getGameFromPlayer(player).remove(player, false);
            return;
        }
        if (!(Tools.isLobbyOwner(player) || !Tools.isPlayerInLobby(player))) {
            player.sendMessage(PLAYER_NO_LOBBY.replaceColor().replacePrefix().format());
        } else {
            InviteLobby lobby;
            if (Tools.isLobbyOwner(player)) lobby = Tools.getLobbyFromOwner(player);
            else lobby = Tools.getLobbyFromPlayer(player);
            player.sendMessage(LOBBY_LEAVE.replaceColor().format());
            lobby.removePlayer(player);
        }
    }
}

