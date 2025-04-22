package com.bluenova.floorislava.game.object.invitelobby;

import org.bukkit.entity.Player;

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
        InviteLobby lobby = new InviteLobby(owner);
        inviteLobbyList.add(lobby);
    }

    public boolean isPlayerOwner(Player removingPlayer) {
        for (InviteLobby lobby : inviteLobbyList) {
            if (lobby.getOwner() == removingPlayer) return true;
        }
        return false;
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
}
