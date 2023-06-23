package Commands.InviteCommands;

import Utils.Tools;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ListPlayersCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;
        if(!Tools.checkPlayerInLobby(player)){
            player.sendMessage("You " + ChatColor.RED + "are not in a lobby");
            return true;
        }

        Tools.getLobbyFromOwner(player).listPlayers();
        return true;
    }
}
