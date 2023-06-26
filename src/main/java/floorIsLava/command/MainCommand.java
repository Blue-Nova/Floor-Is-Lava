package floorIsLava.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import com.sk89q.worldedit.WorldEditException;
import floorIsLava.FloorIsLava;
import floorIsLava.gameobject.InviteLobby;
import floorIsLava.util.Tools;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@CommandAlias("fil|floorislava")
@Description("The MainCommand for Floor Is Lava")
public class MainCommand extends BaseCommand {

    @Subcommand("lobby")
    public class lobbyCommands {
        @Subcommand("create")
        @Description("Create a lobby")
        public void createCommand(Player player) {
            if (Tools.isPlayerIngame(player)) {
                player.sendMessage(FloorIsLava.getInstance().getPrefix() + ChatColor.RED + "You are already in a game");
                return;
            }
            new InviteLobby(player);
            player.sendMessage(FloorIsLava.getInstance().getPrefix() + ChatColor.GREEN + "Successfully created Game!");
        }
    }

    @Subcommand("game")
    public class gameCommands {
        @Subcommand("invite")
        @Description("Invite player(s)")
        public void inviteCommand(Player player, String[] users) {
            if (!Tools.isLobbyOwner(player)) {
                player.sendMessage(FloorIsLava.getInstance().getPrefix() + ChatColor.RED + "You're not the owner of this Lobby, you cannot invite players!");
            }
            if (!Tools.isPlayerInLobby(player)) {
                player.sendMessage(FloorIsLava.getInstance().getPrefix() + ChatColor.RED + "You are not in a lobby!");
                return;
            }
            if (users.length == 0) {
                player.sendMessage(FloorIsLava.getInstance().getPrefix() + ChatColor.RED + "Please enter the name(s) of the player(s) you would like to invite. Usage: " + ChatColor.WHITE + "/fil game invite <playername(s)>");
                return;
            }
            InviteLobby lobby = Tools.getLobbyFromOwner(player);
            boolean sentOneInvite = false;
            for (String playerName : users) {
                if (Bukkit.getPlayer(playerName) == null || !Bukkit.getPlayer(playerName).isOnline()) {
                    player.sendMessage(FloorIsLava.getInstance().getPrefix() + ChatColor.GRAY + "'" + playerName + ChatColor.GRAY + "'" + ChatColor.RED + " Either doesnt exist, or is offline. Ignoring player.");
                    continue;
                }
                Player invitedPlayer = Bukkit.getPlayer(playerName);
                if (Tools.checkPlayerInvitedBy(invitedPlayer, player)) continue;
                if (Tools.isPlayerInLobby(invitedPlayer) || Tools.isLobbyOwner(invitedPlayer)) {
                    player.sendMessage(FloorIsLava.getInstance().getPrefix() + ChatColor.GRAY + "'" + playerName + ChatColor.GRAY + "'" + ChatColor.RED + " is already in a lobby!");
                    continue;
                }
                lobby.invitePlayer(invitedPlayer);
                sentOneInvite = true;
            }
            if (!sentOneInvite)
                player.sendMessage(FloorIsLava.getInstance().getPrefix() + ChatColor.RED + "No player(s) were invited!");
        }

        @Subcommand("accept")
        @Description("Accept Invite")
        public void acceptCommand(Player player, String[] args) {
            if (args.length == 0) {
                player.sendMessage(FloorIsLava.getInstance().getPrefix() + ChatColor.RED + "Name of Inviter needed. Usage: " + ChatColor.WHITE + "/fil game accept <playername>");
                return;
            }
            if (Tools.isPlayerInLobby(player) || Tools.isLobbyOwner(player)) {
                player.sendMessage(FloorIsLava.getInstance().getPrefix() + ChatColor.RED + "You are already in a lobby!");
                return;
            }
            if (Bukkit.getPlayer(args[0]) == null || !(Bukkit.getPlayer(args[0]).isOnline())) {
                player.sendMessage(FloorIsLava.getInstance().getPrefix() + ChatColor.GRAY + "'" + args[0] + ChatColor.GRAY + "'" + ChatColor.RED + " Either doesnt exist, or is offline. Ignoring player.");
                return;
            }
            Player inviter = Bukkit.getPlayer(args[0]);
            if (!Tools.isLobbyOwner(inviter)) {
                player.sendMessage(FloorIsLava.getInstance().getPrefix() + ChatColor.GRAY + "'" + inviter.getName() + ChatColor.GRAY + "'" + ChatColor.RED + " is not in a Lobby!");
            }
            if (!Tools.checkPlayerInvitedBy(inviter, player)) {
                player.sendMessage(FloorIsLava.getInstance().getPrefix() + ChatColor.GRAY + "'" + inviter.getName() + ChatColor.GRAY + "'" + ChatColor.RED + " didn't invite you!");
            } else {
                Tools.getLobbyFromOwner(inviter).inviteAccept(player);
            }
        }

        @Subcommand("list")
        @Description("List's all player(s), in current lobby")
        public void listCommand(Player player) {
            if (!Tools.isPlayerInLobby(player)) {
                player.sendMessage(FloorIsLava.getInstance().getPrefix() + ChatColor.RED + "You are not in a lobby!");
                return;
            }
            Tools.getLobbyFromOwner(player).listPlayers();
        }

        @Subcommand("remove")
        @Description("Removed player(s) from Lobby")
        public void removeCommand(Player player, String[] args) {
            if (!Tools.isLobbyOwner(player)) {
                player.sendMessage(FloorIsLava.getInstance().getPrefix() + ChatColor.RED + "You're not the owner of this lobby!");
                return;
            }
            for (String playername : args) {
                if ((Bukkit.getPlayer(playername) == null) || !(Bukkit.getPlayer(playername).isOnline())) {
                    player.sendMessage(FloorIsLava.getInstance().getPrefix() + ChatColor.GRAY + "'" + playername + ChatColor.GRAY + "'" + ChatColor.RED + " Either doesnt exist, or is offline. Ignoring player.");
                    continue;
                }
                Tools.getLobbyFromOwner(player).removePlayer(Bukkit.getPlayer(playername));
            }
        }

        @Subcommand("start")
        @Description("Starts the game")
        public void startCommand(Player player) {
            if (Tools.isPlayerIngame(player)) {
                player.sendMessage(FloorIsLava.getInstance().getPrefix() + ChatColor.RED + "You are already in a game!");
                return;
            }
            if (!Tools.isLobbyOwner(player)) {
                player.sendMessage(FloorIsLava.getInstance().getPrefix() + ChatColor.RED + "You aren't the owner of a lobby!");
                return;
            }
            if (!(Tools.getLobbyFromOwner(player).joinedList.size() >= 2)) {
                player.sendMessage(FloorIsLava.getInstance().getPrefix() + ChatColor.RED + "Your lobby must have at least 2 players to begin a game!");
                return;
            }
            try {
                Tools.getLobbyFromOwner(player).startGame();
            } catch (WorldEditException ex) {
                throw new RuntimeException(ex);
            }
        }

        @Subcommand("leave")
        @Description("Leaves the game/lobby")
        public void leaveCommand(Player player) {
            if (Tools.isPlayerIngame(player)) {
                Tools.getGameFromPlayer(player).remove(player, false);
                return;
            }
            if (!(Tools.isPlayerInLobby(player) || Tools.isPlayerInLobby(player))) {
                player.sendMessage(FloorIsLava.getInstance().getPrefix() + ChatColor.RED + "You are not in a lobby!");
            } else {
                InviteLobby lobby;
                if (Tools.isLobbyOwner(player)) lobby = Tools.getLobbyFromOwner(player);
                else lobby = Tools.getLobbyFromPlayer(player);
                lobby.removePlayer(player);
            }
        }
    }
}
