package com.bluenova.floorislava.config.json.message;

import lombok.Getter;
import org.bukkit.ChatColor;

import java.util.ArrayList;

public enum Message {

    PLAYER_NO_LOBBY(0, MessageGroup.PLAYER, "%PREFIX% &cYou're not in a lobby!"),
    PLAYER_ALREADY_INGAME(1, MessageGroup.PLAYER, "%PREFIX% &cYou're already in a game!"),
    PLAYER_ALREADY_INLOBBY(2, MessageGroup.PLAYER, "%PREFIX% &cYou're already in a lobby!"),
    PLAYER_NOT_LOBBY_OWNER(3, MessageGroup.PLAYER, "%PREFIX% &cYou're not the owner of this lobby!"),
    PLAYER_REMOVE_UNKNOWN(4, MessageGroup.PLAYER, "%PREFIX% %REMOVE_NAME% &cEither doesn't exist, or is offline!"),
    PLAYER_NO_PERMISSION(18, MessageGroup.PLAYER, "&cYou do not have the Permissions required to execute this command!"),

    INVITE_FAIL_INLOBBY(5, MessageGroup.INVITE, "%PREFIX% %RECEIVER_NAME% &cis already in a lobby!"),
    INVITE_SENDER(6, MessageGroup.INVITE, "%PREFIX% &aYou have invited %RECEIVER_NAME% &ato a game of %cThe floor is Lava&a!"),
    INVITE_RECEIVER(7, MessageGroup.INVITE, "%PREFIX% &aYou have been invited by %SENDER_NAME% &ato a game of %cThe floor is Lava&a!"),
    INVITE_UNKNOWN_PLAYER(8, MessageGroup.INVITE, "%PREFIX% %RECEIVER_NAME% &cEither doesn't exist, or is offline!"),
    INVITE_NONE(9, MessageGroup.INVITE, "%PREFIX% &cNo player(s) were invited!"),
    INVITE_USAGE(10, MessageGroup.INVITE, "%PREFIX% &cPlease enter the name(s) of the player(s) you would like to invite. Usage: &f/fil invite <playername(s)>"),

    ACCEPT_NO_ARGS(11, MessageGroup.ACCEPT, "%PREFIX% &cName of sender needed. Usage: &f/fil game accept <playername>"),
    ACCEPT_UNKNOWN_PLAYER(12, MessageGroup.ACCEPT, "%PREFIX% %SENDER_NAME% &cEither doesn't exist, or is offline!"),
    ACCEPT_SENDER_NOLOBBY(13, MessageGroup.ACCEPT, "%PREFIX% %SENDER_NAME% &cisn't in a lobby!"),
    ACCEPT_NO_INVITE(14, MessageGroup.ACCEPT, "%PREFIX% %SENDER_NAME% &cdidn't invite you!"),
    ACCEPT_SENDER(15, MessageGroup.ACCEPT, "%PREFIX% %RECEIVER_NAME% &aaccepted your invite to a game of &cThe floor is Lave&a!"),
    ACCEPT_RECEIVER(16, MessageGroup.ACCEPT, "%PREFIX% &aYou accepted %SENDER_NAME%'s &aInvite to a game of &cThe floor is Lava&a!"),

    GAME_LEAVE(17, MessageGroup.GAME, "%PREFIX% &aYou left the Game!")

    /*Player Permission Handle*/,
    CREATE_LOBBY_SUCCESS(19, MessageGroup.LOBBY, "yeet"),
    LOBBY_NOT_ENOUGH(20, MessageGroup.LOBBY, "%PREFIX% &cYour lobby must have at least %MIN_PLAYER% players to begin a game!"),
    LOBBY_LEAVE(21, MessageGroup.LOBBY, "%PREFIX% &aYou left the Lobby!")

    /*Util*/,
    PREFIX(01, MessageGroup.UTIL, "&7[&fF&eI&cL&7]");

    @Getter
    private static final ArrayList<Message> lobbyMessages = new ArrayList<>();
    @Getter
    private final String backUP;
    @Getter
    private static final ArrayList<Message> gameMessages = new ArrayList<>();
    @Getter
    private static final ArrayList<Message> acceptMessages = new ArrayList<>();
    @Getter
    private static final ArrayList<Message> inviteMessages = new ArrayList<>();
    @Getter
    private static final ArrayList<Message> playerMessages = new ArrayList<>();
    @Getter
    private static final ArrayList<Message> utilityMessages = new ArrayList<>();
    private String fromConfig;
    private final int id;
    private final MessageGroup messageGroup;
    private String current;

    Message(int ID, MessageGroup messageGroup, String backUP) {
        this.backUP = backUP;
        this.id = ID;
        this.messageGroup = messageGroup;
        this.current = getFromConfig();
    }

    public static String getPrefix() {
        return ChatColor.translateAlternateColorCodes('&', PREFIX.getFromConfig());
    }

    public static void writeArray() {
        for (Message value : Message.values()) {
            switch (value.messageGroup) {
                case LOBBY -> lobbyMessages.add(value);
                case GAME -> gameMessages.add(value);
                case ACCEPT -> acceptMessages.add(value);
                case INVITE -> inviteMessages.add(value);
                case PLAYER -> playerMessages.add(value);
                case UTIL -> utilityMessages.add(value);
            }
        }
    }

    public static ArrayList<Message> getGroup(MessageGroup messageGroup) {
        switch (messageGroup) {
            case LOBBY -> {
                return lobbyMessages;
            }
            case GAME -> {
                return gameMessages;
            }
            case ACCEPT -> {
                return acceptMessages;
            }
            case INVITE -> {
                return inviteMessages;
            }
            case PLAYER -> {
                return playerMessages;
            }
            case UTIL -> {
                return utilityMessages;
            }
            default -> {
                return null;
            }
        }
    }

    public Message replacePrefix() {
        return replaceContent("%PREFIX%", Message.getPrefix());
    }

    public Message replaceColor() {
        current = ChatColor.translateAlternateColorCodes('&', current);
        return this;
    }

    public String format() {
        return current;
    }

    public String getFromConfig() {
        return fromConfig == null ? getBackUP() : fromConfig;
    }


    public int getID() {
        return this.id;
    }

    public MessageGroup getGroup() {
        return this.messageGroup;
    }

    public Message replaceReceiver(String receiverName) {
        return replaceContent("%RECEIVER_NAME%", receiverName);
    }

    public Message replaceSender(String senderName) {
        return replaceContent("%SENDER_NAME%", senderName);
    }

    public Message replaceRemove(String removeName) {
        return replaceContent("%REMOVE_NAME%", removeName);
    }

    public Message replaceMinPlayerCount(int min) {
        return replaceContent("%MIN_PLAYER%", String.valueOf(min));
    }

    public Message replaceMaxPlayerCount(int max) {
        return replaceContent("%MAX_PLAYER%", String.valueOf(max));
    }

    public Message replaceContent(String toRemove, String replacement) {
        current = current.replace(toRemove, replacement);
        return this;
    }

    public String setFromConfig(String toInsert) {
        return this.fromConfig = toInsert;
    }

}
