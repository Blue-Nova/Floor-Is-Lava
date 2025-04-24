package com.bluenova.floorislava.command;// Inside FILCommandHandler.java (Simplified Example)
import com.bluenova.floorislava.command.gamecommands.GameStartCmd;
import com.bluenova.floorislava.command.lobbycommands.*;
import com.bluenova.floorislava.command.subcommand.SubCommand;
import com.bluenova.floorislava.game.object.gamelobby.GameLobbyManager;
import com.bluenova.floorislava.game.object.invitelobby.InviteLobbyManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.*;
// ... other imports ...

public class FILCommandHandler implements CommandExecutor, TabCompleter {

    private final InviteLobbyManager lobbyManager;
    private final GameLobbyManager gameManager;
    // Map to hold top-level commands (lobby, invite, game)
    private final Map<String, Map<String, SubCommand>> commandGroups = new HashMap<>();

    public FILCommandHandler(InviteLobbyManager lobbyManager, GameLobbyManager gameManager) {
        this.lobbyManager = lobbyManager;
        this.gameManager = gameManager;
        registerCommands();
    }

    private void registerCommands() {
        // Register Lobby commands
        Map<String, SubCommand> lobbySubCommands = new HashMap<>();
        lobbySubCommands.put("create", new LobbyCreateCmd(lobbyManager, gameManager));
        lobbySubCommands.put("list", new LobbyListCmd(lobbyManager, gameManager));
        lobbySubCommands.put("invite", new LobbyInviteCmd(lobbyManager, gameManager));
        lobbySubCommands.put("accept", new LobbyAcceptCmd(lobbyManager, gameManager));
        lobbySubCommands.put("leave", new LobbyLeaveCmd(lobbyManager, gameManager));
        commandGroups.put("lobby", lobbySubCommands);

        // Register settings commands (if separate group)
        // Map<String, SubCommand> settingsSubCommands = new HashMap<>();
        // commandGroups.put("settings", settingsSubCommands);

        // Register Game commands
        Map<String, SubCommand> gameSubCommands = new HashMap<>();
        gameSubCommands.put("start", new GameStartCmd(gameManager, lobbyManager));
        commandGroups.put("game", gameSubCommands);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            // Show help message
            sender.sendMessage("Usage: /fil <group> <subcommand> [args...]");
            return true;
        }

        String commandGroupKey = args[0].toLowerCase();
        Map<String, SubCommand> subCommands = commandGroups.get(commandGroupKey);

        if (subCommands == null) {
            sender.sendMessage(ChatColor.RED + "Unknown command group: " + args[0]);
            return true;
        }

        if (args.length == 1) {
            // Show help for the specific group (e.g., /fil lobby)
            sender.sendMessage(ChatColor.YELLOW + "Available subcommands for " + commandGroupKey + ":");
            for (Map.Entry<String, SubCommand> entry : subCommands.entrySet()) {
                sender.sendMessage(ChatColor.GREEN + entry.getKey() + ": " + ChatColor.WHITE + entry.getValue().getUsage());
            }
            return true;
        }

        String subCommandKey = args[1].toLowerCase();
        SubCommand subCommand = subCommands.get(subCommandKey);

        if (subCommand == null) {
            sender.sendMessage(ChatColor.RED + "Unknown subcommand: " + args[1] + " for group " + args[0]);
            return true;
        }

        // Permission Check (Example)
        String permission = subCommand.getPermission();
        if (permission != null && !sender.hasPermission(permission)) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        // Execute the specific subcommand, passing remaining args
        String[] subArgs = Arrays.copyOfRange(args, 2, args.length);
        return subCommand.execute(sender, subArgs);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        String currentArg = args[args.length - 1].toLowerCase(); // Get the argument being typed

        // --- Completion for the FIRST argument (Command Group) ---
        // User typed: /fil <tab> or /fil l<tab>
        if (args.length == 1) {
            // Suggest keys from your top-level command group map
            StringUtil.copyPartialMatches(currentArg, commandGroups.keySet(), completions);
            Collections.sort(completions); // Optional: Sort alphabetically
            return completions;
        }

        String commandGroupKey = args[0].toLowerCase();
        Map<String, SubCommand> subCommands = commandGroups.get(commandGroupKey);

        // Check if the first argument is a valid command group
        if (subCommands == null) {
            return Collections.emptyList(); // No completions if first arg is invalid
        }

        // --- Completion for the SECOND argument (SubCommand Key) ---
        // User typed: /fil lobby <tab> or /fil lobby c<tab>
        if (args.length == 2) {
            // Suggest keys from the specific subcommand map for that group
            StringUtil.copyPartialMatches(currentArg, subCommands.keySet(), completions);
            Collections.sort(completions);
            return completions;
        }

        // --- Completion for arguments AFTER the subcommand ---
        // User typed: /fil lobby invite <tab> or /fil lobby remove p<tab> etc.
        if (args.length == 3) {
            String subCommandKey = args[1].toLowerCase();
            SubCommand subCommand = subCommands.get(subCommandKey);

            if (subCommand != null) {
                // Check permission before offering suggestions (optional but good)
                String permission = subCommand.getPermission();
                if (permission != null && !sender.hasPermission(permission)) {
                    return Collections.emptyList(); // No suggestions if no permission
                }

                // Delegate to the subcommand's tabComplete method
                // Pass only the arguments relevant to the subcommand
                String[] subArgs = Arrays.copyOfRange(args, 2, args.length);
                List<String> subCompletions = subCommand.tabComplete(sender, subArgs);

                // Filter the results from the subcommand based on what the user is currently typing
                // (This handles cases where the subcommand returns unfiltered player names, etc.)
                if (subCompletions != null) {
                    StringUtil.copyPartialMatches(currentArg, subCompletions, completions);
                    Collections.sort(completions);
                    return completions;
                }
            }
        }

        // If no specific completions were found, return an empty list or null
        return Collections.emptyList();
    }
}