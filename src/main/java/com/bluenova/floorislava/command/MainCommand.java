package com.bluenova.floorislava.command;


import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.bluenova.floorislava.game.object.InviteLobby;
import com.bluenova.floorislava.util.Tools;
import com.sk89q.worldedit.WorldEditException;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import static com.bluenova.floorislava.config.Message.*;

@CommandAlias("fil|floorislava")
@CommandPermission("fil.command.fil")
@Description("The MainCommand for Floor Is Lava")
public class MainCommand extends BaseCommand {

    @Subcommand("admin")
    public void debugCommand(Player player) {
        player.sendMessage(String.valueOf(values().length));
        player.sendMessage(PLAYER_NO_PERMISSION.replaceColor().replacePrefix().format());
    }

    @Subcommand("lobby")
    @CommandPermission("fil.command.fil.lobby")
    public class lobbyCommands extends BaseCommand {
        @Subcommand("create")
        @CommandPermission("fil.command.fil.lobby.create")
        @Description("Create a lobby")
        public void createLobbyCommand(Player player) {
            if (Tools.isPlayerIngame(player)) {
                player.sendMessage(PLAYER_ALREADY_INGAME.replaceColor().replacePrefix().format());
                return;
            }
            new InviteLobby(player);
            player.sendMessage(CREATE_LOBBY_SUCCESS.replaceColor().replacePrefix().format());
        }

        @Subcommand("list")
        @CommandPermission("fil.command.fil.lobby.list")
        @Description("List's all player(s), in current lobby")
        public void listLobbyPlayersCommand(Player player) {
            if (!Tools.isPlayerInLobby(player)) {
                player.sendMessage(PLAYER_NO_LOBBY.replaceColor().replacePrefix().format());
                return;
            }
            Tools.getLobbyFromOwner(player).listPlayers();
        }

        @Subcommand("leave")
        @CommandPermission("fil.command.fil.lobby.leave")
        @Description("Leaves the current Lobby")
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

        @Subcommand("remove")
        @CommandPermission("fil.command.fil.lobby.remove")
        @Description("Removed player(s) from Lobby")
        public void removeCommand(Player player, String[] args) {
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

        @Subcommand("start")
        @CommandPermission("fil.command.fil.lobby.start")
        @Description("Starts the game")
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
    }

    @Subcommand("invite")
    @CommandPermission("fil.command.fil.invite")
    public class inviteCommands extends BaseCommand {
        @Default
        @CommandPermission("fil.command.fil.invite")
        @Description("Invite player(s)")
        public void inviteCommand(Player player, String[] users) {
            if (!Tools.isPlayerInLobby(player)) {
                player.sendMessage(PLAYER_NO_LOBBY.replaceColor().replacePrefix().format());
                return;
            }
            if (!Tools.isLobbyOwner(player)) {
                player.sendMessage(PLAYER_NOT_LOBBY_OWNER.replaceColor().replacePrefix().format());
                return;
            }
            if (users.length == 0) {
                player.sendMessage(INVITE_USAGE.replaceColor().replacePrefix().format());
                return;
            }
            InviteLobby lobby = Tools.getLobbyFromOwner(player);
            boolean sentOneInvite = false;
            for (String playerName : users) {
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

        @Subcommand("accept")
        @CommandPermission("fil.command.fil.invite.accept")
        @Description("Accept Invite")
        public void acceptCommand(Player player, String[] args) {
            if (args.length == 0) {
                player.sendMessage(ACCEPT_NO_ARGS.replaceColor().replacePrefix().format());
                return;
            }
            if (Tools.isPlayerInLobby(player) || Tools.isLobbyOwner(player)) {
                player.sendMessage(PLAYER_ALREADY_INLOBBY.replaceColor().replacePrefix().format());
                return;
            }
            if (Bukkit.getPlayer(args[0]) == null || !(Bukkit.getPlayer(args[0]).isOnline())) {
                player.sendMessage(ACCEPT_UNKNOWN_PLAYER.replaceColor().replacePrefix().replaceSender(args[0]).format());
                return;
            }
            Player inviter = Bukkit.getPlayer(args[0]);
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

    }

    @Subcommand("game")
    @CommandPermission("fil.command.fil.game")
    public class gameCommands extends BaseCommand {
        @Subcommand("leave")
        @Description("Leaves the game")
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
}

