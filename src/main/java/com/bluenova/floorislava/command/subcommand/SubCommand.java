package com.bluenova.floorislava.command.subcommand; // Or a subpackage like command.subcommands

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player; // Often useful to have player directly

import java.util.List;

/**
 * Interface for executable subcommands.
 */
public interface SubCommand {

    /**
     * Executes the subcommand logic.
     * @param sender The CommandSender who issued the command.
     * @param args   Arguments passed after the subcommand itself.
     * @return true if the command was handled, false otherwise.
     */
    boolean execute(CommandSender sender, String[] args);

    /**
     * Provides tab completions for the subcommand arguments.
     * @param sender The CommandSender requesting completions.
     * @param args   Arguments passed after the subcommand itself.
     * @return A List of possible completions, or null for default behavior.
     */
    List<String> tabComplete(CommandSender sender, String[] args); // Optional for simple commands

    /**
     * A brief description of the command for help messages.
     * @return String description.
     */
    String getUsage(); // Optional but helpful

    /**
     * Required permission node for this subcommand.
     * @return Permission string or null if none.
     */
    String getPermission(); // Optional
}