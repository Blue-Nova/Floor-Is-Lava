package com.bluenova.floorislava.command;

// ... other imports ...
import com.bluenova.floorislava.command.gamecommands.GameStartCmd;
import com.bluenova.floorislava.command.lobbycommands.*;
import com.bluenova.floorislava.command.subcommand.SubCommand;
import com.bluenova.floorislava.game.object.gamelobby.GameLobbyManager;
import com.bluenova.floorislava.game.object.invitelobby.InviteLobbyManager;
// Import MiniMessages and placeholders
import com.bluenova.floorislava.util.messages.MiniMessages;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
// Bukkit/Spigot imports
import org.bukkit.ChatColor; // Remove this if no longer needed after conversion
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.*;

public class FILCommandHandler implements CommandExecutor, TabCompleter {

    private final InviteLobbyManager lobbyManager;
    private final GameLobbyManager gameManager;
    private final Map<String, Map<String, SubCommand>> commandGroups = new HashMap<>();

    public FILCommandHandler(InviteLobbyManager lobbyManager, GameLobbyManager gameManager) {
        this.lobbyManager = lobbyManager;
        this.gameManager = gameManager;
        registerCommands();
    }

    private void registerCommands() {
        // Lobby commands
        Map<String, SubCommand> lobbySubCommands = new HashMap<>();
        lobbySubCommands.put("create", new LobbyCreateCmd(lobbyManager, gameManager));
        lobbySubCommands.put("list", new LobbyListCmd(lobbyManager, gameManager));
        lobbySubCommands.put("invite", new LobbyInviteCmd(lobbyManager, gameManager));
        lobbySubCommands.put("accept", new LobbyAcceptCmd(lobbyManager, gameManager));
        lobbySubCommands.put("leave", new LobbyLeaveCmd(lobbyManager, gameManager));
        lobbySubCommands.put("kick", new LobbyKickCmd(lobbyManager, gameManager));
        // TODO: Add LobbyRemoveCmd registration
        commandGroups.put("lobby", lobbySubCommands);

        // Game commands
        Map<String, SubCommand> gameSubCommands = new HashMap<>();
        gameSubCommands.put("start", new GameStartCmd(gameManager, lobbyManager));
        // TODO: Add GameLeaveCmd registration (or confirm LobbyLeaveCmd handles it)
        commandGroups.put("game", gameSubCommands);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            // Use MiniMessages - Add general.base_usage key to config
            // Example YAML: general.base_usage: "<red>Usage: /fil <group> <subcommand> [args...]</red>"
            MiniMessages.send(sender, "general.base_usage");
            return true;
        }

        String commandGroupKey = args[0].toLowerCase();
        Map<String, SubCommand> subCommands = commandGroups.get(commandGroupKey);

        if (subCommands == null) {
            // Use MiniMessages - Add general.unknown_command_group key
            // Example YAML: general.unknown_command_group: "<red>Unknown command group: <aqua><group></aqua></red>"
            MiniMessages.send(sender, "general.unknown_command_group",
                    Placeholder.unparsed("group", args[0])
            );
            return true;
        }

        if (args.length == 1) {
            // Use MiniMessages - Add general.group_help_header and general.group_help_entry keys
            // Example YAML header: general.group_help_header: "<yellow>Available commands for <aqua>/fil <group></aqua>:</yellow>"
            // Example YAML entry: general.group_help_entry: " <green>/fil <group> <subcommand></green> <white>- <usage></white>"
            MiniMessages.send(sender, "general.group_help_header",
                    Placeholder.unparsed("group", commandGroupKey)
            );
            for (Map.Entry<String, SubCommand> entry : subCommands.entrySet()) {
                String usage = entry.getValue().getUsage(); // Get usage from subcommand
                if (usage == null || usage.isEmpty()) continue; // Skip if no usage provided

                // Skip if sender doesn't have permission for this specific subcommand
                String specificPerm = entry.getValue().getPermission();
                if (specificPerm != null && !specificPerm.isEmpty() && !sender.hasPermission(specificPerm)) {
                    continue;
                }

                MiniMessages.send(sender, "general.group_help_entry"); // Use usage string from subcommand
            }
            return true;
        }

        String subCommandKey = args[1].toLowerCase();
        SubCommand subCommand = subCommands.get(subCommandKey);

        if (subCommand == null) {
            // Use MiniMessages - Add general.unknown_subcommand key
            // Example YAML: general.unknown_subcommand: "<red>Unknown subcommand '<aqua><subcommand></aqua>' for group '<aqua><group></aqua>'.</red>"
            MiniMessages.send(sender, "general.unknown_subcommand");
            return true;
        }

        // Corrected Permission Check
        String permission = subCommand.getPermission();
        if (permission != null && !permission.isEmpty() && !sender.hasPermission(permission)) {
            // Use MiniMessages - Uses existing general.no_permission key
            MiniMessages.send(sender, "general.no_permission");
            return true;
        }

        // Execute the specific subcommand
        String[] subArgs = Arrays.copyOfRange(args, 2, args.length);
        return subCommand.execute(sender, subArgs);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        String currentArg = args[args.length - 1].toLowerCase();

        // Complete command group key
        if (args.length == 1) {
            StringUtil.copyPartialMatches(currentArg, commandGroups.keySet(), completions);
            Collections.sort(completions);
            return completions;
        }

        String commandGroupKey = args[0].toLowerCase();
        Map<String, SubCommand> subCommands = commandGroups.get(commandGroupKey);
        if (subCommands == null) return Collections.emptyList();

        // Complete subcommand key
        if (args.length == 2) {
            List<String> possibleSubs = new ArrayList<>();
            // Filter suggestions by permission
            for(Map.Entry<String, SubCommand> entry : subCommands.entrySet()){
                String perm = entry.getValue().getPermission();
                if(perm == null || perm.isEmpty() || sender.hasPermission(perm)){
                    possibleSubs.add(entry.getKey());
                }
            }
            StringUtil.copyPartialMatches(currentArg, possibleSubs, completions);
            Collections.sort(completions);
            return completions;
        }

        // Delegate to subcommand (Corrected length check)
        if (args.length > 2) {
            String subCommandKey = args[1].toLowerCase();
            SubCommand subCommand = subCommands.get(subCommandKey);
            if (subCommand != null) {
                String permission = subCommand.getPermission();
                if (permission != null && !permission.isEmpty() && !sender.hasPermission(permission)) {
                    return Collections.emptyList();
                }
                String[] subArgs = Arrays.copyOfRange(args, 2, args.length);
                List<String> subCompletions = subCommand.tabComplete(sender, subArgs);
                if (subCompletions != null) {
                    // Perform filtering here using StringUtil
                    return StringUtil.copyPartialMatches(currentArg, subCompletions, new ArrayList<>());
                    // We sort completions within the copyPartialMatches call implicitly sometimes
                    // Or sort explicitly: Collections.sort(completions); return completions;
                }
            }
        }
        return Collections.emptyList();
    }
}