package Commands.GameCommands;

import Utils.Tools;
import com.sk89q.worldedit.WorldEditException;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StartGameCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) return true;
        Player owner = (Player) sender;
        if(Tools.checkPlayerInGame(owner)){
            owner.sendMessage("You are " + ChatColor.RED + "already in a game!");
            return true;
        }
        if(!Tools.checkOwnerInLobby(owner)){
            owner.sendMessage("You are " + ChatColor.RED + "not an owner"+ ChatColor.RESET + " of a lobby.");
            owner.sendMessage("Create a lobby using " + ChatColor.AQUA + "/fillobby");
            return true;
        }
        if(!(Tools.getLobbyFromOwner(owner).joinedList.size() >=2)){
            owner.sendMessage("Your lobby " + ChatColor.RED + "must have atleast 2 players"+ ChatColor.RESET + " to begin a game.");
        }
        try {
            Tools.getLobbyFromOwner(owner).startGame();
        } catch (WorldEditException e) {
            throw new RuntimeException(e);
        }
        return true;
    }
}
