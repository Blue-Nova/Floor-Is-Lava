package GameObjects;

import Utils.Tools;
import com.sk89q.worldedit.WorldEditException;
import floorIsLava.FloorIsLava;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class InviteLobby {

    public static ArrayList<InviteLobby>INVITE_LOBBY_LIST = new ArrayList<InviteLobby>();

    public final Player OWNER;


    public ArrayList<Player> sentList = new ArrayList<Player>();
    public ArrayList<Player> joinedList = new ArrayList<Player>();

    public InviteLobby(Player owner) {
        INVITE_LOBBY_LIST.add(this);
        OWNER = owner;
        joinedList.add(OWNER);
    }

    public void invitePlayer(Player invitedPlayer){
        if(sentList.contains(invitedPlayer)) {
            return;
        }
        if(joinedList.contains(invitedPlayer)) {
            OWNER.sendMessage(ChatColor.RED + invitedPlayer.getName() + ChatColor.RESET + " is already in your lobby.");
            return;
        }
        invitedPlayer.playSound(invitedPlayer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1,1);
        invitedPlayer.sendMessage(ChatColor.GREEN + OWNER.getName() + ChatColor.RESET + " has invited you to a game of " + ChatColor.YELLOW + "The Floor is Lava");
        invitedPlayer.sendMessage("Type " + ChatColor.ITALIC + ChatColor.AQUA + "/filaccept <inviter's name>" + ChatColor.RESET + " to join the game.");
        invitedPlayer.sendMessage("If you " + ChatColor.RED + "do not" + ChatColor.RESET + " wish to play, ignore this message");

        sentList.add(invitedPlayer);
        OWNER.sendMessage(ChatColor.GREEN + "Invited " + ChatColor.RED + invitedPlayer.getName());
    }

    public void inviteAccept(Player player){
        if(!sentList.contains(player)){
            player.sendMessage("You are not in this player's invite list.");
            return;
        }
        sentList.remove(player);
        joinedList.add(player);
        OWNER.sendMessage(ChatColor.RED + player.getName() + ChatColor.RESET + " accepted your invite!");
        player.sendMessage("You have " + ChatColor.GREEN + "joined " + ChatColor.RESET + OWNER.getName()+ "'s lobby. Wait till they start the game!");
    }

    public void listPlayers(){
        OWNER.sendMessage(ChatColor.RED  + "Ready Players:");
        for (Player readyPlayer: joinedList) {
            OWNER.sendMessage(readyPlayer.getName());
        }
        OWNER.sendMessage(ChatColor.GREEN +  "Invite Sent:");
        for (Player sentPlayer: sentList) {
            OWNER.sendMessage(sentPlayer.getName());
        }
    }

    public void removePlayer(Player removingPlayer){
        if(removingPlayer == OWNER){
            InviteLobby.INVITE_LOBBY_LIST.remove(this);
            for (Player player:joinedList) {
                player.playSound(player.getLocation(),Sound.BLOCK_ANVIL_PLACE,0.5f,1f);
                player.sendMessage(ChatColor.RED + OWNER.getName() + ChatColor.RESET + " has left your lobby and it was disbanded.");
            }
            OWNER.sendMessage(ChatColor.DARK_RED + "You lobby was disbanded");
            return;
        }
        if(!(joinedList.contains(removingPlayer)||sentList.contains(removingPlayer))){
            OWNER.sendMessage(ChatColor.RED + removingPlayer.getName() + ChatColor.RESET + " is not in your lobby.");
            return;
        }
        joinedList.remove(removingPlayer);
        sentList.remove(removingPlayer);
        removingPlayer.sendMessage("You have been " + ChatColor.RED + "removed" + ChatColor.RESET + " from " + OWNER.getName() + "'s lobby.");
        OWNER.sendMessage(ChatColor.RESET + removingPlayer.getName() + ChatColor.RED + " left your lobby.");

    }

    public void startGame() throws WorldEditException {
        GamePlot gp = FloorIsLava.GPD.getFirstEmptyPlot();
        FloorIsLava.GPD.getFirstEmptyPlot().inUse = true;
        new GameLobby(Tools.getLobbyFromOwner(OWNER).joinedList,OWNER,gp);

    }
}
