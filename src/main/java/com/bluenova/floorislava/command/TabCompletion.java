package com.bluenova.floorislava.command;

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
        if(!(sender instanceof Player))return null;
        Player commander = (Player) sender;
        List<String> options = new ArrayList<>();

        if (args.length==0) {
            options.add("lobby");
            options.add("invite");
            options.add("game");
            return options;
        }

        if(args.length==1){
            if(args[1].equalsIgnoreCase("lobby")){
                options.add("create");
                options.add("remove");
                options.add("leave");
                return options;
            }
        }

        return null;
    }
}
