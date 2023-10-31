package com.bluenova.floorislava.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TabCompletion implements TabCompleter {
    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> options = new ArrayList<>();
        if(!(sender instanceof Player))return options;
        if (args.length==1) {
            options.add("lobby");
            options.add("invite");
            options.add("game");
            return options;
        }
        if(args.length==2){
            if(args[0].equalsIgnoreCase("lobby")){
                options.add("create");
                options.add("remove");
                options.add("leave");
                options.add("start");
                return options;
            }
            if(args[0].equalsIgnoreCase("game")){
                options.add("leave");
                return options;
            }
            if(args[0].equalsIgnoreCase("invite")){
                for (Player player: Bukkit.getOnlinePlayers()) {
                    options.add(player.getName());
                }
                options.add("accept");
                return options;
            }
        }
        return null;
    }
}
