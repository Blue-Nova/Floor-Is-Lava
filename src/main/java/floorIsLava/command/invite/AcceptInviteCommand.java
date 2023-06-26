package floorIsLava.command.invite;

import floorIsLava.gameobject.InviteLobby;
import floorIsLava.util.Tools;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AcceptInviteCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;
        if (args.length == 0) {
            player.sendMessage("type the command along with the inviter's name. Eg: " + ChatColor.AQUA + "/filaccept X_LavaMaster_X");
            return true;
        }
        if (Tools.checkPlayerInLobby(player) || Tools.checkOwnerInLobby(player)) {
            player.sendMessage(ChatColor.RED + "You are already in a lobby");
            return true;
        }
        if ((Bukkit.getPlayer(args[0]) == null) || !(Bukkit.getPlayer(args[0]).isOnline())) {
            player.sendMessage(ChatColor.RED + args[0] + ChatColor.RESET + " does not exist or is offline.");
            return true;
        }
        Player owner = Bukkit.getPlayer(args[0]);
        if (!Tools.checkOwnerInLobby(owner)) {
            player.sendMessage(owner.getName() + ChatColor.RED + " doesnt not have a lobby");
        }
        if (!Tools.checkPlayerInvitedBy(owner, player)) {
            player.sendMessage(owner.getName() + ChatColor.RED + " did not invite" + ChatColor.RESET + " you.");
        } else {
            InviteLobby lobby = Tools.getLobbyFromOwner(owner);
            lobby.inviteAccept(player);
        }
        return true;
    }
}
