package com.bluenova.floorislava.util.messages;

import com.bluenova.floorislava.config.MessageConfig;
import org.bukkit.entity.Player;

public class MessageUtils {

    public static void sendFILMessage(Player p, String msg){
        p.sendMessage(MessageConfig.getInstance().getPrefix() + msg);
    }

}
