package floorIsLava.command.invite;

import floorIsLava.util.Tools;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RemovePlayerCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player owner = (Player) sender;
        if (!Tools.checkOwnerInLobby(owner)) {
            owner.sendMessage("You are " + ChatColor.RED + "not the owner" + ChatColor.RESET + " of a lobby to do that.");
            return true;
        }
        for (String playername : args) {
            if ((Bukkit.getPlayer(playername) == null) || !(Bukkit.getPlayer(playername).isOnline())) {
                owner.sendMessage(ChatColor.RED + playername + ChatColor.RESET + " either does not exist or is offline. Ignoring that player.");
                continue;
            }
            Player removingPlayer = Bukkit.getPlayer(playername);
            Tools.getLobbyFromOwner(owner).removePlayer(removingPlayer);
        }
        return true;
    }
}
