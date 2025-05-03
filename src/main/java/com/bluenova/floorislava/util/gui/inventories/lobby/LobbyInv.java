package com.bluenova.floorislava.util.gui.inventories.lobby;

import com.bluenova.floorislava.FloorIsLava;
import com.bluenova.floorislava.game.object.gamelobby.GameLobbyManager;
import com.bluenova.floorislava.game.object.invitelobby.InviteLobby;
import com.bluenova.floorislava.game.object.invitelobby.InviteLobbyManager;
import com.bluenova.floorislava.util.gui.InventoryButton;
import com.bluenova.floorislava.util.gui.objects.InventoryGui;
import com.bluenova.floorislava.util.gui.inventories.util.PlayersToInviteInv;
import com.bluenova.floorislava.util.gui.util.PageIds;
import com.bluenova.floorislava.util.messages.MiniMessages;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
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
        return Bukkit.createInventory(null, width*height, MiniMessages.miniMessage.deserialize("<bold><gold>Lobby <red>Menu"));
    }

    @Override
    public void decorate(org.bukkit.entity.Player player) {
        if (!gameLobbyManager.isPlayerIngame(player) && !inviteLobbyManager.isPlayerInLobby(player)) {
            surroundWithStale(width, height, 4);
            addButton((getInventory().getSize()/2)-5,createLobbyButton());
        }else{
            surroundWithStale(width, height,1);
            // make invite button
            if (inviteLobbyManager.isLobbyOwner(player)){
                renderLobbyAsOwner(player);
            }else{
                renderLobbyAsMember(player);
            }
            // Player is in a lobby or game, show the leave button
            addButton(getInventory().getSize()-5, new InventoryButton()
                    .creator(p -> {
                        ItemStack item = new ItemStack(Material.BARRIER);
                        ItemMeta meta = item.getItemMeta();
                        if (meta != null) {
                            ArrayList<Component> lore = new ArrayList<>();
                            if (inviteLobbyManager.isLobbyOwner(player)) {
                                meta.displayName(MiniMessages.miniMessage.deserialize("<red>Disband Lobby"));
                                lore.add(MiniMessages.miniMessage.deserialize("<gray>Caution! This will Kick all players!"));
                            }else{
                                meta.displayName(MiniMessages.miniMessage.deserialize("<red>Leave Lobby"));
                                lore.add(MiniMessages.miniMessage.deserialize("<gray>Click to leave the lobby"));
                            }
                            meta.lore(lore);
                            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                        }
                        item.setItemMeta(meta);
                        return item;
                    })
                    .consumer(event -> {
                        if (event.getWhoClicked() instanceof Player) {
                            Player player1 = (Player) event.getWhoClicked();
                            inviteLobbyManager.getLobbyFromPlayer(player1).removePlayer(player1);
                            FloorIsLava.getInstance().getGuiManager().openGUI(new LobbyInv(), player1);
                        }
                    }));
        }
        renderHeader(pageId,player, width);
        super.decorate(player);
    }

    private void renderLobbyAsMember(Player player) {
    }

    private void renderLobbyAsOwner(Player player) {
        addButton(width*2+(2), new InventoryButton()
                .creator(p -> {
                    ItemStack item = new ItemStack(Material.FILLED_MAP);
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null) {
                        meta.displayName(MiniMessages.miniMessage.deserialize("<white>Invite Players"));
                        ArrayList<Component> lore = new ArrayList<>();
                        lore.add(MiniMessages.miniMessage.deserialize("<gray>Click to invite players"));
                        meta.lore(lore);
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
                        FloorIsLava.getInstance().getGuiManager().openGUI(new PlayersToInviteInv(allOnlinePlayers, this.pageId, viewer), viewer);
                    }
                }));
        // start game button
        addButton(width*2+(4), new InventoryButton()
                .creator(p -> {
                    ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
                    if (gameLobbyManager.isPlayerIngame(player)) {
                        item = new ItemStack(Material.CLOCK);
                        ItemMeta meta = item.getItemMeta();
                        if (meta != null) {
                            meta.displayName(MiniMessages.miniMessage.deserialize("<aqua>Generating Game..."));
                            ArrayList<Component> lore = new ArrayList<>();
                            lore.add(MiniMessages.miniMessage.deserialize("<gray>You can exit the menu now"));
                            meta.lore(lore);
                            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                        }
                        item.setItemMeta(meta);
                    }else {
                        ItemMeta meta = item.getItemMeta();
                        if (meta != null) {
                            meta.displayName(MiniMessages.miniMessage.deserialize("<white>Start Game"));
                            ArrayList<Component> lore = new ArrayList<>();
                            lore.add(MiniMessages.miniMessage.deserialize("<gray>Click to start the game"));
                            meta.lore(lore);
                            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                        }
                        item.setItemMeta(meta);
                    }
                    return item;
                })
                .consumer(event -> {
                    Player player1 = (Player) event.getWhoClicked();
                    if (gameLobbyManager.isPlayerIngame(player1)) {
                        return;
                    }
                    // 4. Get the lobby
                    InviteLobby lobby = inviteLobbyManager.getLobbyFromOwner(player);
                    if (lobby == null) {
                        // Should not happen if isLobbyOwner passed, but good practice
                        player.sendMessage(ChatColor.RED + "Error: Could not find your lobby."); // Fallback message
                        return;
                    }
                    // 5. Check lobby size (Moved check here from old MainCommand)
                    // Assuming lobby.players holds joined list including owner
                    if (lobby.players.size() < 2) {
                        MiniMessages.send(player, "lobby.start_lobby_too_small");
                        return;
                    }
                    // 6. Tell GameManager to create the game
                    // This now handles getting a plot and creating the GameLobby instance.
                    // The "No free plots" message is handled inside gameManager.createLobby
                    // based on the implementation we saw earlier.
                    gameLobbyManager.createLobby(new ArrayList<>(lobby.players), player);
                    FloorIsLava.getInstance().getGuiManager().openGUI(new LobbyInv(), player);
                    // Optional: Send a "Starting..." message? The GameLobby countdown handles the main alerts.// MiniMessages.send(player, "lobby.starting_game"); // If you add this key to config
                    // NOTE: The removal of the InviteLobby from InviteLobbyManager needs to be handled
                    // This could happen within gameManager.createLobby on success
                    // or the GameLobby could notify InviteLobbyManager when it flly starts.
                }));
    }

    private InventoryButton createLobbyButton() {
        return new InventoryButton()
                .creator(p -> {
                    ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null) {
                        meta.displayName(MiniMessages.miniMessage.deserialize("<white>Create Lobby<gold>+"));
                        ArrayList<Component> lore = new ArrayList<>();
                        lore.add(MiniMessages.miniMessage.deserialize("<gray>Click to create a lobby"));
                        meta.lore(lore);
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
                        FloorIsLava.getInstance().getGuiManager().openGUI(new LobbyInv(), player);
                        MiniMessages.send(player, "lobby.created"); // CORRECTED KEY
                    }
                });
    }

}
