package com.bluenova.floorislava.command;


import com.bluenova.floorislava.config.MessageConfig;
import com.bluenova.floorislava.game.object.InviteLobby;
import com.bluenova.floorislava.util.Tools;
import com.bluenova.floorislava.util.messages.MessageUtils;
import com.sk89q.worldedit.WorldEditException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;

public class MainCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player)) return true;
        Player commander = (Player) sender;
        if(command.getName().equalsIgnoreCase("fil")) {
            if (args.length==0 || args[0].equalsIgnoreCase("help")){
                showHelp(commander);
            }else if (args[0].equalsIgnoreCase("lobby")) {
                if(args.length == 1)return true;
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
                if(args.length == 1)return true;
                if (args[1].equalsIgnoreCase("leave")){
                    leaveCommand(commander);
                }
            }else{return true;}
        }

        return true;
    }

    public void showHelp(Player p){

    }

    public void createLobbyCommand(Player player) {
        if (Tools.isPlayerIngame(player)) {
            MessageUtils.sendFILMessage(player, MessageConfig.getInstance().getAlreadyInGame());
            return;
        }
        new InviteLobby(player);
        MessageUtils.sendFILMessage(player, MessageConfig.getInstance().getLobbyCreated());
    }

    public void listLobbyPlayersCommand(Player player) {
        if (!Tools.isPlayerInLobby(player)) {
            MessageUtils.sendFILMessage(player, MessageConfig.getInstance().getNotInLobby());
            return;
        }
        Tools.getLobbyFromOwner(player).listPlayers();
    }

    public void leaveLobbyCommand(Player player) {
        if (!Tools.isPlayerInLobby(player)) {
            MessageUtils.sendFILMessage(player, MessageConfig.getInstance().getNotInLobby());
            return;
        }
        if (Tools.isPlayerIngame(player)) {
            Tools.getGameFromPlayer(player).remove(player, false);
            return;
        }
        if (!(Tools.isLobbyOwner(player) || !Tools.isPlayerInLobby(player))) {
            MessageUtils.sendFILMessage(player, MessageConfig.getInstance().getNotInLobby());
        } else {
            InviteLobby lobby;
            if (Tools.isLobbyOwner(player)) lobby = Tools.getLobbyFromOwner(player);
            else lobby = Tools.getLobbyFromPlayer(player);
            lobby.removePlayer(player);
        }
    }

    public void removeCommand(Player player, ArrayList<String> args) {
        if (!Tools.isPlayerInLobby(player)) {
            MessageUtils.sendFILMessage(player, MessageConfig.getInstance().getNotInLobby());
            return;
        }
        if (!Tools.isLobbyOwner(player)) {
            MessageUtils.sendFILMessage(player, MessageConfig.getInstance().getNotLobbyOwner());
            return;
        }
        for (String playername : args) {
            if ((Bukkit.getPlayer(playername) == null) || !(Bukkit.getPlayer(playername).isOnline())) {
                MessageUtils.sendFILMessage(player, MessageConfig.getInstance().getPlayerNotFound(playername));
                continue;
            }
            Tools.getLobbyFromOwner(player).removePlayer(Bukkit.getPlayer(playername));
        }
    }

    public void startCommand(Player player) {
        if (!Tools.isPlayerInLobby(player)) {
            MessageUtils.sendFILMessage(player, MessageConfig.getInstance().getNotInLobby());
            return;
        }
        if (Tools.isPlayerIngame(player)) {
            MessageUtils.sendFILMessage(player, MessageConfig.getInstance().getAlreadyInGame());
            return;
        }
        if (!Tools.isLobbyOwner(player)) {
            MessageUtils.sendFILMessage(player, MessageConfig.getInstance().getNotLobbyOwner());
            return;
        }
        if (!(Tools.getLobbyFromOwner(player).joinedList.size() >= 2)) {
            MessageUtils.sendFILMessage(player, MessageConfig.getInstance().getLobbyNotLargeEnough());
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
            MessageUtils.sendFILMessage(player, MessageConfig.getInstance().getNotInLobby());
            return;
        }
        if (!Tools.isLobbyOwner(player)) {
            MessageUtils.sendFILMessage(player, MessageConfig.getInstance().getNotLobbyOwner());
            return;
        }
        if (users.size() == 0) {
            MessageUtils.sendFILMessage(player, MessageConfig.getInstance().getInviteUsage());
            return;}
        InviteLobby lobby = Tools.getLobbyFromOwner(player);
        boolean sentOneInvite = false;
        ArrayList<String> failedInvites = new ArrayList<>();
        ArrayList<Player> invitedPlayers = new ArrayList<>();
        for (String playerName : users) {
            try{Bukkit.getPlayer(playerName);}
            catch(Exception e){
                failedInvites.add(playerName);
                continue;}
            if (Bukkit.getPlayer(playerName) == null || !Bukkit.getPlayer(playerName).isOnline()) {
                failedInvites.add(playerName);
                continue;
            }
            Player invitedPlayer = Bukkit.getPlayer(playerName);
            if (Tools.checkPlayerInvitedBy(invitedPlayer, player)) continue;
            if (Tools.isPlayerInLobby(invitedPlayer) || Tools.isLobbyOwner(invitedPlayer)) {
                continue;
            }
            invitedPlayers.add(invitedPlayer);
            sentOneInvite = true;
        }

        if (!sentOneInvite){
            //player.sendMessage(INVITE_NONE.replaceColor().replacePrefix().format());
            return;
        }
        lobby.invitePlayers(invitedPlayers,failedInvites);
    }
    public void acceptCommand(Player player, String acceptingPlayer) {
        if (acceptingPlayer==null) {
            //player.sendMessage(ACCEPT_NO_ARGS.replaceColor().replacePrefix().format());
            return;
        }
        if (Tools.isPlayerInLobby(player) || Tools.isLobbyOwner(player)) {
            MessageUtils.sendFILMessage(player, MessageConfig.getInstance().getAlreadyInLobby());
            return;
        }
        if (Bukkit.getPlayer(acceptingPlayer) == null || !(Bukkit.getPlayer(acceptingPlayer).isOnline())) {
            //player.sendMessage(ACCEPT_UNKNOWN_PLAYER.replaceColor().replacePrefix().replaceSender(acceptingPlayer).format());
            return;
        }
        Player inviter = Bukkit.getPlayer(acceptingPlayer);
        if (!Tools.isLobbyOwner(inviter)) {
            //player.sendMessage(ACCEPT_SENDER_NOLOBBY.replaceColor().replacePrefix().replaceSender(inviter.getDisplayName()).format());
        }
        if (!Tools.checkPlayerInvitedBy(inviter, player)) {
            //player.sendMessage(ACCEPT_NO_INVITE.replaceColor().replacePrefix().replaceSender(inviter.getDisplayName()).format());
        } else {
            //player.sendMessage(ACCEPT_RECEIVER.replaceColor().replacePrefix().replaceSender(inviter.getDisplayName()).format());
            MessageUtils.sendFILMessage(inviter, MessageConfig.getInstance().getAcceptingInvite(player));
            Tools.getLobbyFromOwner(inviter).inviteAccept(player);
        }
    }
    public void leaveCommand(Player player) {
        if (Tools.isPlayerIngame(player)) {
            Tools.getGameFromPlayer(player).remove(player, false);
            return;
        }
        if (!(Tools.isLobbyOwner(player) || !Tools.isPlayerInLobby(player))) {
            MessageUtils.sendFILMessage(player, MessageConfig.getInstance().getNotInLobby());
        } else {
            InviteLobby lobby;
            if (Tools.isLobbyOwner(player)) lobby = Tools.getLobbyFromOwner(player);
            else lobby = Tools.getLobbyFromPlayer(player);
            MessageUtils.sendFILMessage(player, MessageConfig.getInstance().getLeavingLobby(lobby.ownerPlayer));
            lobby.removePlayer(player);
        }
    }
}

