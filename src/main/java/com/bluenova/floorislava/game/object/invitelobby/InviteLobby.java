package com.bluenova.floorislava.game.object.invitelobby;

import com.bluenova.floorislava.FloorIsLava;
import com.bluenova.floorislava.config.MessageConfig;
import com.bluenova.floorislava.game.object.Lobby;
import com.bluenova.floorislava.game.object.gamelobby.GameLobby;
import com.bluenova.floorislava.game.object.GamePlot;
import com.bluenova.floorislava.game.object.gamelobby.GameLobbyManager;
import com.bluenova.floorislava.util.messages.MessageUtils;
import com.sk89q.worldedit.WorldEditException;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class InviteLobby extends Lobby {

    public ArrayList<Player> sentList = new ArrayList<>();
    private final InviteLobbyManager inviteLobbyManager = FloorIsLava.getInviteLobbyManager();
    private final GameLobbyManager gameLobbyManager = FloorIsLava.getGameLobbyManager();

    public InviteLobby(Player owner) {
        super(new ArrayList<>(), owner);
        players.add(owner);
    }

    public void invitePlayers(ArrayList<Player> invitedPlayers, ArrayList<String> failedInvites) {
        ArrayList<Player> sentTemp = new ArrayList<>();
        for (Player invitedPlayer: invitedPlayers) {
            if (sentList.contains(invitedPlayer)) {
                failedInvites.add(invitedPlayer.getName());
                continue;
            }
            if (this.players.contains(invitedPlayer)){
                failedInvites.add(invitedPlayer.getName());
                continue;
            }
            invite(invitedPlayer);
            sentList.add(invitedPlayer);
            sentTemp.add(invitedPlayer);
        }
        String invitedListMessage = MessageConfig.getInstance().getSendingInvite();
        for (Player player:sentTemp){
            invitedListMessage = ChatColor.translateAlternateColorCodes('&',
                            invitedListMessage + "&b"+player.getName());
            if(sentTemp.get(sentTemp.size()-1)!=player) invitedListMessage =
                    ChatColor.translateAlternateColorCodes('&',invitedListMessage + "&f, ");
        }
        MessageUtils.sendFILMessage(this.getOwner(),invitedListMessage);
        String failedListMessage = MessageConfig.getInstance().getFailedInvites();
        for (String playerName:failedInvites){
            failedListMessage = ChatColor.translateAlternateColorCodes('&',
                    failedListMessage + "&b"+playerName);
            if(failedInvites.get(failedInvites.size()-1)!=playerName) failedListMessage =
                    ChatColor.translateAlternateColorCodes('&',failedListMessage + "&c, ");
        }
        MessageUtils.sendFILMessage(this.getOwner(),failedListMessage);
    }

    public void invite(Player invitedPlayer){
        invitedPlayer.playSound(invitedPlayer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
        MessageUtils.sendFILMessage(invitedPlayer, MessageConfig.getInstance().getReciveingInvite(this.getOwner()));
        TextComponent acceptmessage = new TextComponent("[Accept Invite]");
        acceptmessage.setColor(ChatColor.GREEN.asBungee());
        acceptmessage.setBold(true);
        acceptmessage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/fil invite accept " + this.getOwner().getName()));
        acceptmessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder("Click here to accept invite from " + this.getOwner().getName()).color(ChatColor.GREEN.asBungee()).italic(true).create()));
        invitedPlayer.spigot().sendMessage(acceptmessage);
    }

    public void inviteAccept(Player player) {
        if (!sentList.contains(player)) {
            player.sendMessage("You are not in this player's invite list.");
            return;
        }
        sentList.remove(player);
        this.players.add(player);
        this.getOwner().sendMessage(ChatColor.RED + player.getName() + ChatColor.RESET + " accepted your invite!");
        player.sendMessage("You have " + ChatColor.GREEN + "joined " + ChatColor.RESET + this.getOwner().getName() + "'s lobby. Wait till they start the game!");
    }

    public void listPlayers() {
        this.getOwner().sendMessage(ChatColor.RED + "Ready Players:");
        for (Player readyPlayer : this.players) {
            this.getOwner().sendMessage(readyPlayer.getName());
        }
        this.getOwner().sendMessage(ChatColor.GREEN + "Invite Sent:");
        for (Player sentPlayer : sentList) {
            this.getOwner().sendMessage(sentPlayer.getName());
        }
    }

    public void removePlayer(Player removingPlayer) {
        if (inviteLobbyManager.isPlayerOwner(removingPlayer)) {
            inviteLobbyManager.closeLobby(this);
            for (Player player : this.players) {
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 0.5f, 1f);
                player.sendMessage(ChatColor.RED + this.getOwner().getName() + ChatColor.RESET + " has left your lobby and it was disbanded.");
            }
            this.getOwner().sendMessage(ChatColor.DARK_RED + "Your lobby was disbanded");
            return;
        }
        if (!(this.players.contains(removingPlayer) || sentList.contains(removingPlayer))) {
            this.getOwner().sendMessage(ChatColor.RED + removingPlayer.getName() + ChatColor.RESET + " is not in your lobby.");
            return;
        }
        this.players.remove(removingPlayer);
        sentList.remove(removingPlayer);
        removingPlayer.sendMessage("You have been " + ChatColor.RED + "removed" + ChatColor.RESET + " from " + this.getOwner().getName() + "'s lobby.");
        this.getOwner().sendMessage(ChatColor.RESET + removingPlayer.getName() + ChatColor.RED + " left your lobby.");
    }

    public void startGame() throws WorldEditException {
        gameLobbyManager.createLobby(this.players, this.getOwner());
    }
}
