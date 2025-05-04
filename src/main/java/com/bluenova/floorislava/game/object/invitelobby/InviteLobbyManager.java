package com.bluenova.floorislava.game.object.invitelobby;

import com.bluenova.floorislava.FloorIsLava;
import com.bluenova.floorislava.util.messages.MiniMessages;
import com.bluenova.floorislava.util.messages.PluginLogger;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.*;

public class InviteLobbyManager {

    private final List<InviteLobby> inviteLobbyList = new ArrayList<>();
    private final PluginLogger pluginLogger;

    public InviteLobbyManager(PluginLogger pluginLogger) {
        this.pluginLogger = pluginLogger;
    }

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
        InviteLobby lobby = new InviteLobby(owner, FloorIsLava.getInviteLobbyManager(), FloorIsLava.getFILRegionManager());
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
                if (invited == player) {
                    playerList.add(lobby.getOwner());
                }
                break;
            }
        }
        return playerList;
    }

    public boolean isPlayerInvitedBy(Player player, Player owner) {
        for (InviteLobby lobby : inviteLobbyList) {
            if (lobby.getOwner() == owner) {
                if (lobby.sentList.contains(player)) return true;
            }
        }
        return false;
    }

    public boolean isPlayerInOwnersLobby(Player player, Player owner) {
        for (InviteLobby lobby : inviteLobbyList) {
            if (lobby.getOwner() == owner) {
                if (lobby.players.contains(player)) return true;
            }
        }
        return false;
    }

    public ArrayList<Player> getPlayersFromOwner(Player player) {
        for (InviteLobby lobby : inviteLobbyList) {
            if (lobby.getOwner() == player) {
                return lobby.players;
            }
        }
        return null;
    }

    public ArrayList<Player> getInvitedPlayersFromOwner(Player player) {
        for (InviteLobby lobby : inviteLobbyList) {
            if (lobby.getOwner() == player) {
                return lobby.sentList;
            }
        }
        return null;
    }

    public boolean isLobbyReadyForStart(InviteLobby lobby) {
        if (lobby.players.size() >= 2) {
            return true;
        } else {
            return false;
        }
    }

    public @Nullable List<? extends Component> generateRequirementsLore(InviteLobby lobby) {
        List<Component> lore = new ArrayList<>();
        if (lobby.players.size() < 2) {
            lore.add(MiniMessages.miniMessage.deserialize("<red>- At least 2 players in Lobby"));
        }else{
            lore.add(MiniMessages.miniMessage.deserialize("<green>- At least 2 players in Lobby"));
        }
        return lore;
    }

    public void kickPlayerFromLobby(Player playerToKick, Player owner) {
        InviteLobby lobby = getLobbyFromOwner(owner);
        if (lobby != null) {
            if (lobby.players.contains(playerToKick)) {
                lobby.kickPlayer(playerToKick);
            }
        }
    }

    public Player getOwnnerFromLobby(InviteLobby inviteLobby) {
        for (InviteLobby lobby : inviteLobbyList) {
            if (lobby == inviteLobby) {
                return lobby.getOwner();
            }
        }
        return null;
    }
}
