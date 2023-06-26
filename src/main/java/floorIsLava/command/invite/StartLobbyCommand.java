package floorIsLava.command.invite;

import floorIsLava.gameobject.InviteLobby;
import floorIsLava.util.Tools;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StartLobbyCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;
        if (Tools.checkPlayerInGame(player)) {
            player.sendMessage("You are " + ChatColor.RED + "already in a game!");
            return true;
        }
        new InviteLobby(player);
        for (String playername : args) {
            if ((Bukkit.getPlayer(playername) == null) || !(Bukkit.getPlayer(playername).isOnline())) {
                player.sendMessage(ChatColor.RED + playername + ChatColor.RESET + " either does not exist or is offline. Ignoring that player.");
                continue;
            }
            Player addedPlayer = Bukkit.getPlayer(playername);
            Tools.getLobbyFromOwner(player).invitePlayer(addedPlayer);
        }
        if (args.length == 0) {
            player.sendMessage(ChatColor.GREEN + "successfully" + ChatColor.RESET + " made a lobby.");
            player.sendMessage("Type " + ChatColor.AQUA + "/filinvite <playername>" + ChatColor.RESET + " to invite friends to your lobby and " +
                    ChatColor.AQUA + "/filstart" + ChatColor.RESET + " to begin the game.");
        }
        return true;
    }

}