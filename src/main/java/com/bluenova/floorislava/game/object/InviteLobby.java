package com.bluenova.floorislava.game.object;

import com.bluenova.floorislava.FloorIsLava;
import com.bluenova.floorislava.config.MessageConfig;
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
import java.util.Objects;

public class InviteLobby {

    public static ArrayList<InviteLobby> inviteLobbyList = new ArrayList<>();

    public final Player ownerPlayer;

    public ArrayList<Player> sentList = new ArrayList<>();
    public ArrayList<Player> joinedList = new ArrayList<>();

    public InviteLobby(Player owner) {
        inviteLobbyList.add(this);
        ownerPlayer = owner;
        joinedList.add(ownerPlayer);
    }

    public void invitePlayers(ArrayList<Player> invitedPlayers, ArrayList<String> failedInvites) {
        ArrayList<Player> sentTemp = new ArrayList<>();
        for (Player invitedPlayer: invitedPlayers) {
            if (sentList.contains(invitedPlayer)) {
                failedInvites.add(invitedPlayer.getName());
                continue;
            }
            if (joinedList.contains(invitedPlayer)){
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
        MessageUtils.sendFILMessage(ownerPlayer,invitedListMessage);
        String failedListMessage = MessageConfig.getInstance().getFailedInvites();
        for (String playerName:failedInvites){
            failedListMessage = ChatColor.translateAlternateColorCodes('&',
                    failedListMessage + "&b"+playerName);
            if(!Objects.equals(failedInvites.get(failedInvites.size() - 1), playerName)) failedListMessage =
                    ChatColor.translateAlternateColorCodes('&',failedListMessage + "&c, ");
        }
        MessageUtils.sendFILMessage(ownerPlayer,failedListMessage);
    }

    public void invite(Player invitedPlayer){
        invitedPlayer.playSound(invitedPlayer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
        MessageUtils.sendFILMessage(invitedPlayer, MessageConfig.getInstance().getReciveingInvite(ownerPlayer));
        TextComponent acceptmessage = new TextComponent("[Accept Invite]");
        acceptmessage.setColor(ChatColor.GREEN.asBungee());
        acceptmessage.setBold(true);
        acceptmessage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/fil invite accept " + ownerPlayer.getName()));
        acceptmessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder("Click here to accept invite from " + ownerPlayer.getName()).color(ChatColor.GREEN.asBungee()).italic(true).create()));
        invitedPlayer.spigot().sendMessage(acceptmessage);
    }

    public void inviteAccept(Player player) {
        if (!sentList.contains(player)) {
            MessageUtils.sendFILMessage(player,MessageConfig.getInstance().getNotInInviteList(ownerPlayer));
            return;
        }
        sentList.remove(player);
        joinedList.add(player);
        MessageUtils.sendFILMessage(player,MessageConfig.getInstance().getYouJoinedLobby(ownerPlayer));
        MessageUtils.sendFILMessage(ownerPlayer, MessageConfig.getInstance().getAcceptedInvite(player));
    }

    public void listPlayers() {
        MessageUtils.sendFILMessage(ownerPlayer,MessageConfig.getInstance().getReadyList());
        for (Player readyPlayer : joinedList) {
            ownerPlayer.sendMessage(readyPlayer.getName());
        }
        MessageUtils.sendFILMessage(ownerPlayer,MessageConfig.getInstance().getInvitedList());
        for (Player sentPlayer : sentList) {
            ownerPlayer.sendMessage(sentPlayer.getName());
        }
    }

    public void removePlayer(Player removingPlayer) {
        if (removingPlayer == ownerPlayer) {
            InviteLobby.inviteLobbyList.remove(this);
            for (Player player : joinedList) {
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 0.5f, 1f);
                MessageUtils.sendFILMessage(player,MessageConfig.getInstance().getPlayerLeftLobby(ownerPlayer));
                MessageUtils.sendFILMessage(player,MessageConfig.getInstance().getLobbyDisband());
            }
            MessageUtils.sendFILMessage(ownerPlayer,MessageConfig.getInstance().getLobbyDisband());
            return;
        }
        if (!(joinedList.contains(removingPlayer) || sentList.contains(removingPlayer))) {
            MessageUtils.sendFILMessage(ownerPlayer,MessageConfig.getInstance().getNotInYourLobby(removingPlayer));
            return;
        }
        joinedList.remove(removingPlayer);
        sentList.remove(removingPlayer);
        MessageUtils.sendFILMessage(removingPlayer,MessageConfig.getInstance().getRemovedFromLobby(ownerPlayer));
        MessageUtils.sendFILMessage(ownerPlayer,MessageConfig.getInstance().getPlayerLeftLobby(removingPlayer));
    }

    public void startGame() throws WorldEditException {
        GamePlot gp = FloorIsLava.getGamePlotDivider().getFirstEmptyPlot();
        if (gp == null){
            MessageUtils.sendFILMessage(ownerPlayer,MessageConfig.getInstance().getNoFreePlots());
            return;
        }
        gp.inUse = true;
        new GameLobby(joinedList, ownerPlayer, gp);
    }
}
