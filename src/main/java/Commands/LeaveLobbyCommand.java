package Commands;

import GameObjects.InviteLobby;
import Utils.Tools;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LeaveLobbyCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) return true;
        Player player = (Player) sender;
        if(Tools.checkPlayerInGame(player)){
            Tools.getGameFromPlayer(player).remove(player, false);
            return true;
        }

        if(!(Tools.checkOwnerInLobby(player)||Tools.checkPlayerInLobby(player))){
            player.sendMessage("You " + ChatColor.RED + "are not" + ChatColor.RESET + " in a lobby.");
        }else{
            InviteLobby lobby;
            if (Tools.checkOwnerInLobby(player)) lobby = Tools.getLobbyFromOwner(player);
            else lobby = Tools.getLobbyFromPlayer(player);
            lobby.removePlayer(player);
        }
        return true;
    }
}
