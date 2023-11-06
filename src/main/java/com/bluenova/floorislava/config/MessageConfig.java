package com.bluenova.floorislava.config;

import com.bluenova.floorislava.FloorIsLava;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;

public class MessageConfig {

    private final static MessageConfig instance = new MessageConfig();


    private File file;
    private YamlConfiguration config;

    private String prefix;
    private String FLOORISLAVA;
    private String LobbyCreated;
    private String NoPermission;
    private String NotInLobby;
    private String SendingInvite;
    private String ReciveingInvite;
    private String AcceptingInvite;
    private String AlreadyInvited;
    private String AlreadyInLobby;
    private String AlreadyInGame;
    private String KickingPlayer;
    private String LeavingLobby;
    private String PlayerLeftLobby;
    private String LobbyDisband;
    private String NotLobbyOwner;
    private String PlayerNotFound;
    private String LobbyNotLargeEnough;
    private String FailedInvites;
    private String NotInInviteList;
    private String AcceptedInvite;
    private String YouJoinedLobby;
    private String ReadyList;
    private String InvitedList;
    private String RemovedFromLobby;
    private String NoFreePlots;
    private String NotInYourLobby;

    private String InviteUsage;

    private MessageConfig() {
    }

    public void load() {
        file = new File(FloorIsLava.getInstance().getDataFolder(), "MessageConfig.yml");

        if (!file.exists())
            FloorIsLava.getInstance().saveResource("MessageConfig.yml", false);

        config = new YamlConfiguration();
        config.options().parseComments(true);

        try {
            config.load(file);

        } catch (Exception e) {
            e.printStackTrace();
        }
        prefix = config.getString("prefix");
        FLOORISLAVA = config.getString("FLOORISLAVA");
        LobbyCreated = config.getString("LobbyCreated");
        NoPermission = config.getString("NoPermission");
        NotInLobby = config.getString("NotInLobby");
        SendingInvite = config.getString("SendingInvite");
        ReciveingInvite = config.getString("ReciveingInvite");
        AcceptingInvite = config.getString("AcceptingInvite");
        AlreadyInvited = config.getString("AlreadyInvited");
        AlreadyInLobby = config.getString("AlreadyInLobby");
        AlreadyInGame = config.getString("AlreadyInGame");
        KickingPlayer = config.getString("KickingPlayer");
        LeavingLobby = config.getString("LeavingLobby");
        PlayerLeftLobby = config.getString("PlayerLeftLobby");
        LobbyDisband = config.getString("LobbyDisband");
        NotLobbyOwner = config.getString("NotLobbyOwner");
        PlayerNotFound = config.getString("PlayerNotFound");
        LobbyNotLargeEnough = config.getString("LobbyNotLargeEnough");
        FailedInvites = config.getString("FailedInvites");
        NotInInviteList = config.getString("NotInInviteList");
        AcceptedInvite = config.getString("AcceptedInvite");
        YouJoinedLobby = config.getString("YouJoinedLobby");
        ReadyList = config.getString("ReadyList");
        InvitedList = config.getString("InvitedList");
        RemovedFromLobby = config.getString("RemovedFromLobby");
        NoFreePlots = config.getString("NoFreePlots");
        NotInYourLobby = config.getString("NotInYourLobby");
        InviteUsage = config.getString("commandUsage.inviteUsage");
    }

    public void save() {
        try {
            config.save(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static MessageConfig getInstance() {
        return instance;
    }

    private String getFLOORISLAVA(){return prepare(FLOORISLAVA);}

    public String getPrefix() {
        return prepare(prefix);
    }
    public String getLobbyCreated() {
        return prepare(LobbyCreated);
    }
    public String getNoPermission() {
        return prepare(NoPermission);
    }
    public String getNotInLobby() {
        return prepare(NotInLobby);
    }
    public String getSendingInvite() {
        return prepare(SendingInvite);
    }
    public String getReciveingInvite(Player ownerPlayer) {
        return prepare(ReciveingInvite.replaceAll("%INVITERNAME%","&b"+ownerPlayer.getName()).replaceAll("%FLOORISLAVA%",getFLOORISLAVA()));
    }
    public String getAcceptingInvite(Player player) {
        return prepare(AcceptingInvite.replaceAll("%INVITEDPLAYER%","&b"+player.getName()));
    }
    public String getAlreadyInvited()                           {
        return prepare(AlreadyInvited);
    }
    public String getAlreadyInLobby()       {
        return prepare(AlreadyInLobby);
    }
    public String getAlreadyInGame()                            {
        return prepare(AlreadyInGame);
    }
    public String getKickingPlayer(Player kickedPlayer) {
        return prepare(KickingPlayer.replaceAll("%KICKEDPLAYER%",kickedPlayer.getName()));
    }
    public String getLeavingLobby(Player ownerPlayer) {
        return prepare(LeavingLobby.replaceAll("%LOBBYOWNER%", "&b"+ownerPlayer.getName()));
    }
    public String getPlayerLeftLobby(Player removingPlayer) {
        return prepare(PlayerLeftLobby.replaceAll("%LEAVINGPLAYER%", "&b"+removingPlayer));
    }
    public String getLobbyDisband() {
        return prepare(LobbyDisband);
    }
    public String getNotLobbyOwner() {
        return prepare(NotLobbyOwner);
    }
    public String getLobbyNotLargeEnough() {
        return prepare(LobbyNotLargeEnough);
    }
    public String getPlayerNotFound(String playername) {
        return prepare(PlayerNotFound.replaceAll("%NOTFOUNDPLAYER%", "&b"+playername));
    }
    public String getNotInInviteList(Player playername) {
        return prepare(NotInInviteList.replaceAll("%OWNER%", "&b"+playername));
    }
    public String getYouJoinedLobby(Player ownerPlayer) {
        return prepare(YouJoinedLobby.replaceAll("%OWNER%","&b"+ownerPlayer));
    }
    public String getAcceptedInvite(Player player) {
        return prepare(AcceptedInvite.replaceAll("%JOININGPLAYER%","&b"+player));
    }
    public String getReadyList() {
        return prepare(ReadyList);
    }
    public String getInvitedList() {
        return prepare(InvitedList);
    }
    public String getRemovedFromLobby(Player ownerPlayer) {
        return prepare(RemovedFromLobby.replaceAll("%OWNER%","&b"+ownerPlayer));
    }
    public String getNoFreePlots() {
        return prepare(NoFreePlots);
    }
    public String getFailedInvites() {
        return prepare(FailedInvites);}
    public String getNotInYourLobby(Player missingPlayer){
        return prepare(NotInLobby.replaceAll("%Player%","&b"+missingPlayer));
    }
    public String getInviteUsage(){
        return prepare(InviteUsage);
    }

    private String prepare(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }



}


