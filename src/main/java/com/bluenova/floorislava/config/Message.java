package com.bluenova.floorislava.config;

import org.bukkit.ChatColor;

import java.util.ArrayList;

public enum Message {

    PLAYER_NO_LOBBY(0, Group.PLAYER, null, "%PREFIX% &cYou're not in a lobby!"),
    PLAYER_ALREADY_INGAME(1, Group.PLAYER, null, "%PREFIX% &cYou're already in a game!"),
    PLAYER_ALREADY_INLOBBY(2, Group.PLAYER, null, "%PREFIX% &cYou're already in a lobby!"),
    PLAYER_NOT_LOBBY_OWNER(3, Group.PLAYER, null, "%PREFIX% &cYou're not the owner of this lobby!"),
    PLAYER_REMOVE_UNKNOWN(4, Group.PLAYER, null, "%PREFIX% %REMOVE_NAME% &cEither doesn't exist, or is offline!"),
    PLAYER_NO_PERMISSION(18, Group.PLAYER, null, "&cYou do not have the Permissions required to execute this command!"),

    INVITE_FAIL_INLOBBY(5, Group.INVITE, null, "%PREFIX% %RECEIVER_NAME% &cis already in a lobby!"),
    INVITE_SENDER(6, Group.INVITE, null, "%PREFIX% &aYou have invited %RECEIVER_NAME% &ato a game of %cThe floor is Lava&a!"),
    INVITE_RECEIVER(7, Group.INVITE, null, "%PREFIX% &aYou have been invited by %SENDER_NAME% &ato a game of %cThe floor is Lava&a!"),
    INVITE_UNKNOWN_PLAYER(8, Group.INVITE, null, "%PREFIX% %RECEIVER_NAME% &cEither doesn't exist, or is offline!"),
    INVITE_NONE(9, Group.INVITE, null, "%PREFIX% &cNo player(s) were invited!"),
    INVITE_USAGE(10, Group.INVITE, null, "%PREFIX% &cPlease enter the name(s) of the player(s) you would like to invite. Usage: &f/fil invite <playername(s)>"),

    ACCEPT_NO_ARGS(11, Group.ACCEPT, null, "%PREFIX% &cName of sender needed. Usage: &f/fil game accept <playername>"),
    ACCEPT_UNKNOWN_PLAYER(12, Group.ACCEPT, null, "%PREFIX% %SENDER_NAME% &cEither doesn't exist, or is offline!"),
    ACCEPT_SENDER_NOLOBBY(13, Group.ACCEPT, null, "%PREFIX% %SENDER_NAME% &cisn't in a lobby!"),
    ACCEPT_NO_INVITE(14, Group.ACCEPT, null, "%PREFIX% %SENDER_NAME% &cdidn't invite you!"),
    ACCEPT_SENDER(15, Group.ACCEPT, null, "%PREFIX% %RECEIVER_NAME% &aaccepted your invite to a game of &cThe floor is Lave&a!"),
    ACCEPT_RECEIVER(16, Group.ACCEPT, null, "%PREFIX% &aYou accepted %SENDER_NAME%'s &aInvite to a game of &cThe floor is Lava&a!"),

    GAME_LEAVE(17, Group.GAME, null, "%PREFIX% &aYou left the Game!")

    /*Player Permission Handle*/,
    CREATE_LOBBY_SUCCESS(19, Group.LOBBY, null, "yeet"),
    LOBBY_NOT_ENOUGH(20, Group.LOBBY, null, "%PREFIX% &cYour lobby must have at least %MIN_PLAYER% players to begin a game!"),
    LOBBY_LEAVE(21, Group.LOBBY, null, "%PREFIX% &aYou left the Lobby!")

    /*Util*/,
    PREFIX(01, Group.UTIL, null, "&7[&fF&eI&cL&7]");

    private static final ArrayList<Message> lobbyMessages = new ArrayList<>();
    private final String backUP;
    private static final ArrayList<Message> gameMessages = new ArrayList<>();
    private static final ArrayList<Message> acceptMessages = new ArrayList<>();
    private static final ArrayList<Message> inviteMessages = new ArrayList<>();
    private static final ArrayList<Message> playerMessages = new ArrayList<>();
    private static final ArrayList<Message> utilityMessages = new ArrayList<>();
    private String fromConfig;
    private final int id;
    private final Group group;
    private String current;

    Message(int ID, Group group, String fromConfig, String backUP) {
        this.fromConfig = fromConfig;
        this.backUP = backUP;
        this.id = ID;
        this.group = group;
        this.current = getFromConfig();
    }

    public static String getPrefix() {
        return ChatColor.translateAlternateColorCodes('&', PREFIX.getFromConfig());
    }

    public static void writeToArrays() {
        for (Message value : Message.values()) {
            switch (value.group) {
                case LOBBY -> lobbyMessages.add(value);
                case GAME -> gameMessages.add(value);
                case ACCEPT -> acceptMessages.add(value);
                case INVITE -> inviteMessages.add(value);
                case PLAYER -> playerMessages.add(value);
                case UTIL -> utilityMessages.add(value);
            }
        }
    }

    public static ArrayList<Message> getLobbyMessages() {
        return lobbyMessages;
    }

    public static ArrayList<Message> getGameMessages() {
        return gameMessages;
    }

    public static ArrayList<Message> getAcceptMessages() {
        return acceptMessages;
    }

    public static ArrayList<Message> getInviteMessages() {
        return inviteMessages;
    }

    public static ArrayList<Message> getPlayerMessages() {
        return playerMessages;
    }

    public static ArrayList<Message> getUtilityMessages() {
        return utilityMessages;
    }

    public static ArrayList<Message> getGroup(Group group) {
        switch (group) {
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

    public Group getGroup() {
        return this.group;
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

    public String getBackUP() {
        return backUP;
    }

    public String setFromConfig(String toInsert) {
        return this.fromConfig = toInsert;
    }

}
