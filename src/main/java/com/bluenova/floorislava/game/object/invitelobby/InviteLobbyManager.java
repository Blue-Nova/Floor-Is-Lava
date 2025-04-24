package com.bluenova.floorislava.game.object.invitelobby;

import com.bluenova.floorislava.FloorIsLava;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Array;
import java.util.*;

public class InviteLobbyManager {

    private final List<InviteLobby> inviteLobbyList = new ArrayList<>();

    /*Checks if owner already invited a player*/
    public boolean checkPlayerInvitedBy(Player owner, Player player) {
        for (InviteLobby lobby : inviteLobbyList) {
            if (lobby.getOwner() == owner) {
                return lobby.sentList.contains(player);
            }
        }
        return false;
    }

    /* is player in lobby?*/
    public boolean isPlayerInLobby(Player player) {
        for (InviteLobby lobby : inviteLobbyList) {
            if (lobby.players.contains(player)) return true;
        }
        return false;
    }

    /*Checks in what lobby the player is the owner*/
    public InviteLobby getLobbyFromOwner(Player owner) {
        for (InviteLobby lobby : inviteLobbyList) {
            if (lobby.getOwner() == owner) return lobby;
        }
        return null;
    }

    public void createLobby(Player owner) {
        InviteLobby lobby = new InviteLobby(owner, FloorIsLava.getInviteLobbyManager(), FloorIsLava.getGameLobbyManager());
        inviteLobbyList.add(lobby);
    }

    public void closeLobby(InviteLobby inviteLobby) {
        inviteLobbyList.remove(inviteLobby);
    }

    public boolean isLobbyOwner(Player owner) {
        for (InviteLobby lobby : inviteLobbyList) {
            if (lobby.getOwner() == owner) return true;
        }
        return false;
    }

    public InviteLobby getLobbyFromPlayer(Player player) {
        for (InviteLobby lobby : inviteLobbyList) {
            if (lobby.players.contains(player)) return lobby;
        }
        return null;
    }

    public ArrayList<Player> getAllInviters(Player player) {
        ArrayList<Player> playerList = new ArrayList<>();
        for (InviteLobby lobby : inviteLobbyList) {
            ArrayList<Player> inviteList = lobby.sentList;
            for (Player invited : inviteList) {
                System.out.println("Invited: " + invited.getName());
                if (invited == player) {
                    playerList.add(lobby.getOwner());
                }
                break;
            }
        }
        return playerList;
    }

    public boolean isPlayerInvited(Player player) {
        for (InviteLobby lobby : inviteLobbyList) {
            if (lobby.sentList.contains(player)) return true;
        }
        return false;
    }
}
