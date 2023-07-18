package com.bluenova.floorislava.config;

public enum Message {

    PLAYER_NO_LOBBY("", "%PREFIX% &cYou're not in a lobby!"),
    PLAYER_ALREADY_INGAME("", "%PREFIX% &cYou're already in a game!"),
    PLAYER_ALREADY_INLOBBY("", "%PREFIX% &cYou're already in a lobby!"),
    PLAYER_NOT_LOBBY_OWNER("", "%PREFIX% &cYou're not the owner of this lobby!"),

    /*Player Permission Handle*/
    PLAYER_NO_PERMISSION(" ", "&cYou do not have the Permissions required to execute this command!"),

    INVITE_FAIL_INLOBBY("", "%PREFIX% %RECEIVER_NAME% &cis already in a lobby!"),
    INVITE_SENDER("", "%PREFIX% &aYou have invited %RECEIVER_NAME% &ato a game of %cThe floor is Lava&a!"),
    INVITE_RECEIVER("", "%PREFIX% &aYou have been invited by %SENDER_NAME% &ato a game of %cThe floor is Lava&a!"),
    INVITE_UNKNOWN_PLAYER("", "%PREFIX% %RECEIVER_NAME% &cEither doesn't exist, or is offline!"),
    INVITE_NONE("", "%PREFIX% &cNo player(s) were invited!"),
    INVITE_USAGE("", "%PREFIX% &cPlease enter the name(s) of the player(s) you would like to invite. Usage: &f/fil invite <playername(s)>"),

    ACCEPT_NO_ARGS("", "%PREFIX% &cName of sender needed. Usage: &f/fil game accept <playername>"),
    ACCEPT_UNKNOWN_PLAYER("", "%PREFIX% %ACCEPTING_NAME% &cEither doesn't exist, or is offline!"),
    ACCEPT_SENDER_NOLOBBY("", "%PREFIX% %ACCEPTING_NAME% &cisn't in a lobby!"),
    ACCEPT_NO_INVITE("", "%PREFIX% %ACCEPTING_NAME% &cdidn't invite you!"),
    ACCEPT_SENDER("", "%PREFIX% %RECIEVER_NAME% &aaccepted your invite to a game of &cThe floor is Lave&a!"),
    ACCEPT_RECEIVER("", "%PREFIX% &aYou accepted %SENDER_NAME's% &aInvite to a game of &cThe floor is Lava&a!"),

    GAME_LEAVE("", "%PREFIX% &aYou left the Game!"),

    CREATE_LOBBY_SUCCESS("", ""),
    LOBBY_NOT_ENOUGH("", "%PREFIX% &cYour lobby must have at least %MIN_PLAYER% players to begin a game!"),
    LOBBY_LEAVE("", "%PREFIX% &aYou left the Lobby!"),

    REMOVE_UNKNOWN_PLAYER("", "%PREFIX% %REMOVE_NAME% &cEither doesn't exist, or is offline!"),


    XD("", "");

    private final String fromConfig;
    private final String backUP;

    Message(String fromConfig, String backUP) {
        this.fromConfig = fromConfig;
        this.backUP = backUP;
    }

    private String getBackUP() {
        return backUP;
    }

    public String getFromConfig() {
        return fromConfig == null ? getBackUP() : fromConfig;
    }
}
