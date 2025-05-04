package com.bluenova.floorislava.util.gui.inventories.game;

import com.bluenova.floorislava.FloorIsLava;
import com.bluenova.floorislava.game.object.gamelobby.GameLobbyManager;
import com.bluenova.floorislava.util.gui.InventoryButton;
import com.bluenova.floorislava.util.gui.inventories.util.AreYouSurePopUp;
import com.bluenova.floorislava.util.gui.objects.InventoryGui;
import com.bluenova.floorislava.util.gui.util.PageIds;
import com.bluenova.floorislava.util.messages.MiniMessages;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class InGameMenu extends InventoryGui {

    private GameLobbyManager gameManager;

    @Override
    protected Inventory createInventory() {

        gameManager = FloorIsLava.getGameLobbyManager();

        return org.bukkit.Bukkit.createInventory(null, 9, MiniMessages.miniMessage.deserialize("<dark_red>In-Game Menu"));
    }

    @Override
    public void decorate(Player player) {
        surroundWithStale(9, 1, 1);

        if (gameManager.isAPlayer(player)){
            renderAsPlayer();
        }else if (gameManager.isASpectator(player)){
            renderAsSpectator();
        }
        super.decorate(player);
    }

    private void renderAsSpectator() {
        addButton(4, new InventoryButton()
                .creator(p -> {
                    ItemStack item = new ItemStack(Material.SKELETON_SKULL); // Example item
                    SkullMeta skullMeta = (SkullMeta) item.getItemMeta();
                    if (skullMeta != null) {
                        skullMeta.displayName(MiniMessages.miniMessage.deserialize("<red><bold>Leave Game"));
                        item.setItemMeta(skullMeta);
                    }
                    return item;
                }).consumer(event -> {
                    Player player1 = (Player) event.getWhoClicked();
                    gameManager.getGameFromPlayer(player1).remove(player1,true, false);
                }));
    }

    private void renderAsPlayer(){
        // Add buttons to the inventory
        addButton(2, new InventoryButton()
                .creator(p -> {
                    ItemStack item = new ItemStack(Material.SKELETON_SKULL); // Example item
                    SkullMeta skullMeta = (SkullMeta) item.getItemMeta();
                    if (skullMeta != null) {
                        skullMeta.displayName(MiniMessages.miniMessage.deserialize("<red><bold>Leave Game"));
                        item.setItemMeta(skullMeta);
                    }
                    return item;
                }).consumer(event -> {
                    Component title = MiniMessages.miniMessage.deserialize("<red><bold>Are you sure?");
                    FloorIsLava.getInstance().getPluginLogger().debug("Title for AreYouSurePopUp:" + title);
                    AreYouSurePopUp popUp = new AreYouSurePopUp(event1 -> {
                        Player player1 = (Player) event1.getWhoClicked();
                        gameManager.getGameFromPlayer(player1).remove(player1, false, false);
                    }, PageIds.GAME_MENU);
                    FloorIsLava.getInstance().getPluginLogger().debug("Opening AreYouSurePopUp Title:" + popUp.getTitle());
                    FloorIsLava.getInstance().getGuiManager().openGUI(popUp, (Player) event.getWhoClicked());
                }));
        // lose and spectate
        addButton(6, new InventoryButton()
                .creator(p -> {
                    // item with a ghost like material that resembles spectating
                    ItemStack item = new ItemStack(Material.ENDER_EYE); // Example item
                    ItemMeta itemMeta = item.getItemMeta();
                    if (itemMeta != null) {
                        itemMeta.displayName(MiniMessages.miniMessage.deserialize("<red><bold>Lose and Spectate"));
                        item.setItemMeta(itemMeta);
                    }
                    return item;
                }).consumer(event -> {
                    Component title = MiniMessages.miniMessage.deserialize("<red><bold>Are you sure?");
                    FloorIsLava.getInstance().getPluginLogger().debug("Title for AreYouSurePopUp:" + title);
                    AreYouSurePopUp popUp = new AreYouSurePopUp(event1 -> {
                        Player player1 = (Player) event1.getWhoClicked();
                        gameManager.getGameFromPlayer(player1).remove(player1, true, false);
                        player1.closeInventory();
                    }, PageIds.GAME_MENU);
                    FloorIsLava.getInstance().getPluginLogger().debug("Opening AreYouSurePopUp Title:" + popUp.getTitle());
                    FloorIsLava.getInstance().getGuiManager().openGUI(popUp, (Player) event.getWhoClicked());
                }));
    }
}
