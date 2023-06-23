package Commands.InviteCommands;

import GameObjects.InviteLobby;
import Utils.Tools;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class InvitePlayersCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player owner = (Player) sender;
        if(!Tools.checkOwnerInLobby(owner)){
            owner.sendMessage("You are not in a lobby. Create one using "+ ChatColor.AQUA + "/fillobby");
            return true;
        }

        if(args.length == 0){
            owner.sendMessage("Please the name(s) of the player(s) after the command. Eg: " + ChatColor.AQUA + "/filinvite X_LavaMaster_X");
            return true;
        }

        InviteLobby lobby = Tools.getLobbyFromOwner(owner);

        boolean sentOneInvite = false;

        for (String playername: args) {
            if((Bukkit.getPlayer(playername) == null)||!(Bukkit.getPlayer(playername).isOnline())){
                owner.sendMessage(ChatColor.RED + playername + ChatColor.RESET + " either does not exist or is offline. Ignoring that player.");
                continue;
            }
            Player addedPlayer = Bukkit.getPlayer(playername);
            if(Tools.checkPlayerInvitedBy(addedPlayer,owner))
                continue;
            if(Tools.checkPlayerInLobby(addedPlayer)||Tools.checkOwnerInLobby(addedPlayer)) {
                owner.sendMessage(ChatColor.RED + addedPlayer.getName() + " is already in a lobby");
                continue;
            }
            lobby.invitePlayer(addedPlayer);
            sentOneInvite = true;
        }
        if (!sentOneInvite) owner.sendMessage(ChatColor.RED + "No players" + ChatColor.RESET + " to invite.");
        return true;
    }
}
