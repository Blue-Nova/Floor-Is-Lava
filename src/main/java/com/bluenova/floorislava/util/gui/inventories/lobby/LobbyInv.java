package com.bluenova.floorislava.util.gui.inventories.lobby;

import com.bluenova.floorislava.FloorIsLava;
import com.bluenova.floorislava.game.object.gamelobby.GameLobbyManager;
import com.bluenova.floorislava.game.object.invitelobby.InviteLobby;
import com.bluenova.floorislava.game.object.invitelobby.InviteLobbyManager;
import com.bluenova.floorislava.util.gui.InventoryButton;
import com.bluenova.floorislava.util.gui.inventories.util.AreYouSurePopUp;
import com.bluenova.floorislava.util.gui.objects.InventoryGui;
import com.bluenova.floorislava.util.gui.util.PageIds;
import com.bluenova.floorislava.util.messages.MiniMessages;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class LobbyInv extends InventoryGui {

    int width;
    int height;

    private GameLobbyManager gameLobbyManager;
    private InviteLobbyManager inviteLobbyManager;
    public LobbyInv() {
        super();
        this.pageId = PageIds.LOBBY;
        this.gameLobbyManager = FloorIsLava.getGameLobbyManager();
        this.inviteLobbyManager = FloorIsLava.getInviteLobbyManager();
    }

    @Override
    protected Inventory createInventory() {
        width = 9;
        height = 6;
        return Bukkit.createInventory(null, width*height, MiniMessages.legacy("<bold><gold>Lobby <red>Menu"));
    }

    @Override
    public void decorate(org.bukkit.entity.Player player) {
        if (!gameLobbyManager.isPlayerIngame(player) && !inviteLobbyManager.isPlayerInLobby(player)) {
            addButton((getInventory().getSize()/2)-5,createLobbyButton());
        }else{
            if (inviteLobbyManager.isLobbyOwner(player)){
                renderLobbyAsOwner(player);
            }else{
                renderLobbyAsMember(player);
            }
        }
        bordersWithExit(width, height, 1);
        renderHeader(pageId,player, width);

        super.decorate(player);
    }

    private void renderLobbyAsMember(Player player) {
        int slot = width*2+(6);
        InviteLobby inviteLobby = inviteLobbyManager.getLobbyFromPlayer(player);
        Player owner = inviteLobbyManager.getOwnnerFromLobby(inviteLobby);
        renderLobbyList(slot, owner);
        slot = width*4+(7);
        renderLobbyLeaveButton(slot, player);
    }

    private void renderLobbyLeaveButton(int slot, Player player) {
        boolean playerIsOwner = inviteLobbyManager.isLobbyOwner(player);
        addButton(slot, new InventoryButton()
                .creator(p -> {
                    ItemStack item = new ItemStack(Material.ANVIL);
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null) {
                        ArrayList<String> lore = new ArrayList<>();
                        if (playerIsOwner){
                            meta.setDisplayName(MiniMessages.legacy("<bold><red>Disband Lobby"));
                            lore.add(MiniMessages.legacy("<gray>Caution! This will Kick all players!"));
                            meta.setLore(lore);
                            meta.addEnchant(Enchantment.LURE, 1, true);
                            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                        } else {
                            meta.setDisplayName(MiniMessages.legacy("<bold><red>Leave Lobby"));
                            lore.add(MiniMessages.legacy("<gray>Click to leave the lobby"));
                            meta.setLore(lore);
                        }
                        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                    }
                    item.setItemMeta(meta);
                    return item;
                })
                .consumer(event -> {
                    if (playerIsOwner){
                        Player owner = (Player) event.getWhoClicked();
                        AreYouSurePopUp popUp = new AreYouSurePopUp(event1 -> {
                            if (event1.getWhoClicked() instanceof Player) {
                                inviteLobbyManager.getLobbyFromOwner(owner).removePlayer(owner);
                                FloorIsLava.getInstance().getGuiManager().openGUI(new LobbyInv(), owner);
                                owner.playSound(owner.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.3f, 1.5f);
                            }
                        }, this.pageId);
                        owner.playSound(owner.getLocation(), Sound.ENTITY_GHAST_AMBIENT, 0.5f, 1.5f);
                        FloorIsLava.getInstance().getGuiManager().openGUI(popUp, (Player) event.getWhoClicked());
                    } else {
                        Player member = (Player) event.getWhoClicked();
                        InviteLobby lobby = inviteLobbyManager.getLobbyFromPlayer(member);
                        if (lobby != null) {
                            lobby.removePlayer(member);
                            FloorIsLava.getInstance().getGuiManager().openGUI(new LobbyInv(), member);
                            member.playSound(member.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.3f, 1.5f);
                        }
                    }
                }));
    }

    private void renderLobbyList(int slot, Player owner) {
        addButton(slot, new InventoryButton()
                .creator(p -> {
                    ItemStack item = new ItemStack(Material.GOLDEN_HELMET);
                    ItemMeta meta = item.getItemMeta();

                    if (meta != null) {
                        meta.setDisplayName(MiniMessages.legacy("<aqua>Lobby Members"));
                        ArrayList<String> lore = new ArrayList<>();
                        lore.add(MiniMessages.legacy("<gray>Click to refresh list"));
                        ArrayList<Player> lobbyMembers = inviteLobbyManager.getPlayersFromOwner(owner);
                        lore.add(MiniMessages.legacy("<bold><green>Joined:"));
                        for (Player member : lobbyMembers) {
                            lore.add(MiniMessages.legacy("<white>- " + member.getName()));
                        }

                        ArrayList<Player> invitedMembers = inviteLobbyManager.getInvitedPlayersFromOwner(owner);
                        lore.add(MiniMessages.legacy("<bold><yellow>Invted:"));
                        for (Player member : invitedMembers) {
                            lore.add(MiniMessages.legacy("<gray>- " + member.getName()));
                        }
                        meta.setLore(lore);
                        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                        item.setItemMeta(meta);
                    }
                    return item;
                })
                .consumer(event -> {
                    FloorIsLava.getInstance().getGuiManager().openGUI(new LobbyInv(), (Player) event.getWhoClicked());
                    Player p = (Player) event.getWhoClicked();
                    p.playSound(p.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_STEP, 1, 1);
                }));
    }

    private void renderLobbyAsOwner(Player player) {
        addButton(width*2+(2), new InventoryButton()
                .creator(p -> {
                    ItemStack item = new ItemStack(Material.FILLED_MAP);
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null) {
                        meta.setDisplayName(MiniMessages.legacy("<white>Invite Players"));
                        ArrayList<String> lore = new ArrayList<>();
                        lore.add(MiniMessages.legacy("<gray>Click to invite players"));
                        meta.setLore(lore);
                        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                    }
                    item.setItemMeta(meta);
                    return item;
                })
                .consumer(event -> {
                    if (event.getWhoClicked() instanceof Player) {
                        Player viewer = (Player) event.getWhoClicked();
                        ArrayList<Player> allOnlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
                        allOnlinePlayers.remove(viewer); // Remove the player themselves
                        viewer.playSound(viewer.getLocation(), Sound.BLOCK_CHISELED_BOOKSHELF_INSERT_ENCHANTED, 1, 1);
                        FloorIsLava.getInstance().getGuiManager().openGUI(new PlayersToInviteInv(allOnlinePlayers, this.pageId, viewer), viewer);
                    }
                }));
        // start game button
        addButton(width*2+(4), new InventoryButton()
                .creator(p -> {
                    ItemStack item = new ItemStack(Material.CLOCK);
                    ItemMeta meta = item.getItemMeta();
                    // if game already is generating, show clock
                    if (gameLobbyManager.isPlayerIngame(player)) {
                        if (meta != null) {
                            meta.setDisplayName(MiniMessages.legacy("<aqua>Generating Game..."));
                            ArrayList<String> lore = new ArrayList<>();
                            lore.add(MiniMessages.legacy("<gray>You can exit the menu now"));
                            meta.setLore(lore);
                            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                        }
                        item.setItemMeta(meta);
                        return item;
                    }
                    // else, check if can start game
                    InviteLobby lobby = inviteLobbyManager.getLobbyFromOwner(player);
                    if (inviteLobbyManager.isLobbyReadyForStart(lobby)){
                        item = new ItemStack(Material.DIAMOND_SWORD);
                        if (meta != null) {
                            meta.setDisplayName(MiniMessages.legacy("<white>Start Game"));
                            ArrayList<String> lore = new ArrayList<>();
                            lore.add(MiniMessages.legacy("<gray>Click to start the game"));
                            meta.setLore(lore);
                            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                        }
                    }else {
                        item = new ItemStack(Material.WOODEN_SWORD);
                        meta = item.getItemMeta();
                        if (meta != null) {
                            meta.setDisplayName(MiniMessages.legacy("<red>Missing Requirements"));
                            meta.setLore(inviteLobbyManager.generateRequirementsLore(lobby));
                            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                        }
                    }
                    item.setItemMeta(meta);
                    return item;
                })
                .consumer(event -> {
                    InviteLobby lobby = inviteLobbyManager.getLobbyFromOwner(player);
                    if (!inviteLobbyManager.isLobbyReadyForStart(lobby)){
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                        return;
                    }
                    gameLobbyManager.createLobby(new ArrayList<>(lobby.players), player);
                    player.closeInventory();
                }));
        // lobby members list
        renderLobbyList(width*2+(6), player);
        // Kick player button
        addButton(width*4+(1), new InventoryButton()
                .creator(p -> {
                    ItemStack item = new ItemStack(Material.IRON_SWORD);
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null) {
                        meta.setDisplayName(MiniMessages.legacy("<red>Kick Player"));
                        ArrayList<String> lore = new ArrayList<>();
                        lore.add(MiniMessages.legacy("<gray>Click to kick a player"));
                        meta.setLore(lore);
                        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                    }
                    item.setItemMeta(meta);
                    return item;
                })
                .consumer(event -> {
                    if (event.getWhoClicked() instanceof Player) {
                        ArrayList<Player> lobbyMembers = new ArrayList<>(inviteLobbyManager.getPlayersFromOwner(player));
                        lobbyMembers.remove(player); // Remove the player themselves
                        FloorIsLava.getInstance().getGuiManager().openGUI(new PlayersToKickInv(lobbyMembers, this.pageId, player), player);
                        player.playSound(player.getLocation(), Sound.BLOCK_ANCIENT_DEBRIS_BREAK, 1, 1);
                    }
                }));

        // Leave lobby button
        renderLobbyLeaveButton(width*4+(7), player);
    }

    private InventoryButton createLobbyButton() {
        return new InventoryButton()
                .creator(p -> {
                    ItemStack item = new ItemStack(Material.WRITABLE_BOOK);
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null) {
                        meta.setDisplayName(MiniMessages.legacy("<white>Create lobby<gold>+"));
                        ArrayList<String> lore = new ArrayList<>();
                        lore.add(MiniMessages.legacy("<gray>Click to create a lobby"));
                        meta.setLore(lore);
                        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                    }
                    item.setItemMeta(meta);
                    return item;
                })
                .consumer(event -> {
                    if (event.getWhoClicked() instanceof Player) {
                        Player player = (Player) event.getWhoClicked();
                        inviteLobbyManager.createLobby(player);
                        player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1, 1);
                        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.2f, 0.5f);
                        FloorIsLava.getInstance().getGuiManager().openGUI(new LobbyInv(), player);
                    }
                });
    }

}
