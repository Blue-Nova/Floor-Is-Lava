package com.bluenova.floorislava.game.object;

import com.bluenova.floorislava.FloorIsLava;
import com.bluenova.floorislava.util.MessageUtil;
import com.sk89q.worldedit.WorldEditException;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;

import static com.bluenova.floorislava.config.json.message.Message.INVITE_RECEIVER;
import static com.bluenova.floorislava.config.json.message.Message.INVITE_SENDER;

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

    public void invitePlayer(Player invitedPlayer) {
        if (sentList.contains(invitedPlayer)) {
            return;
        }
        if (joinedList.contains(invitedPlayer)) {
            invitedPlayer.sendMessage(FloorIsLava.getInstance().getPrefix() + ChatColor.GRAY + "'" + invitedPlayer.getName() + ChatColor.GRAY + "'" + ChatColor.RED + " is already in your lobby!");
            return;
        }
        invitedPlayer.playSound(invitedPlayer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
        MessageUtil.sendModifiedMessage(invitedPlayer, "&7'" + ownerPlayer.getName() + "&7' &ahas invited you to a game of &cThe Floor is Lava&a! ", "&a&l[CLICK HERE TO ACCEPT]", "fil invite accept " + ownerPlayer.getName(), "&a&lAccepts the invite and joins the game!");
        sentList.add(invitedPlayer);
        ownerPlayer.sendMessage("Invite Sent to " + invitedPlayer.getName());
    }

    public void inviteAccept(Player player) {
        if (!sentList.contains(player)) {
            player.sendMessage("You are not in this player's invite list.");
            return;
        }
        sentList.remove(player);
        joinedList.add(player);
        ownerPlayer.sendMessage(ChatColor.RED + player.getName() + ChatColor.RESET + " accepted your invite!");
        player.sendMessage("You have " + ChatColor.GREEN + "joined " + ChatColor.RESET + ownerPlayer.getName() + "'s lobby. Wait till they start the game!");
    }

    public void listPlayers() {
        ownerPlayer.sendMessage(ChatColor.RED + "Ready Players:");
        for (Player readyPlayer : joinedList) {
            ownerPlayer.sendMessage(readyPlayer.getName());
        }
        ownerPlayer.sendMessage(ChatColor.GREEN + "Invite Sent:");
        for (Player sentPlayer : sentList) {
            ownerPlayer.sendMessage(sentPlayer.getName());
        }
    }

    public void removePlayer(Player removingPlayer) {
        if (removingPlayer == ownerPlayer) {
            InviteLobby.inviteLobbyList.remove(this);
            for (Player player : joinedList) {
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 0.5f, 1f);
                player.sendMessage(ChatColor.RED + ownerPlayer.getName() + ChatColor.RESET + " has left your lobby and it was disbanded.");
            }
            ownerPlayer.sendMessage(ChatColor.DARK_RED + "You lobby was disbanded");
            return;
        }
        if (!(joinedList.contains(removingPlayer) || sentList.contains(removingPlayer))) {
            ownerPlayer.sendMessage(ChatColor.RED + removingPlayer.getName() + ChatColor.RESET + " is not in your lobby.");
            return;
        }
        joinedList.remove(removingPlayer);
        sentList.remove(removingPlayer);
        removingPlayer.sendMessage("You have been " + ChatColor.RED + "removed" + ChatColor.RESET + " from " + ownerPlayer.getName() + "'s lobby.");
        ownerPlayer.sendMessage(ChatColor.RESET + removingPlayer.getName() + ChatColor.RED + " left your lobby.");

    }

    public void startGame() throws WorldEditException {
        GamePlot gp = FloorIsLava.getInstance().getGamePlotDivider().getFirstEmptyPlot();
        gp.inUse = true;
        if (gp == null){
            ownerPlayer.sendMessage(ChatColor.RED + "No free plots" + ChatColor.RESET + " available. Please wait a moment for a game to end" +
                    "or message a server admin to increase max amount of plots allowed");
            return;
        }
        new GameLobby(joinedList, ownerPlayer, gp);
    }
}
