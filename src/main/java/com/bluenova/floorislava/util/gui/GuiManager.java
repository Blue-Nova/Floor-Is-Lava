package com.bluenova.floorislava.util.gui;


import com.bluenova.floorislava.util.gui.inventories.game.InGameMenu;
import com.bluenova.floorislava.util.gui.inventories.lobby.LobbyInv;
import com.bluenova.floorislava.util.gui.inventories.main.MainMenu;
import com.bluenova.floorislava.util.gui.objects.InventoryGui;
import com.bluenova.floorislava.util.gui.util.PageIds;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import java.util.HashMap;
import java.util.Map;

public class GuiManager {

    private final Map<Inventory, InventoryHandler> activeInventories = new HashMap<>();

    public void openGUI(InventoryGui gui, Player player) {
        this.registerHandledInventory(gui.getInventory(), gui);
        Inventory inventory = gui.getInventory();
        player.openInventory(inventory);
    }

    public void registerHandledInventory(Inventory inventory, InventoryHandler handler) {
        this.activeInventories.put(inventory, handler);
    }

    public void unregisterInventory(Inventory inventory) {
        this.activeInventories.remove(inventory);
    }

    public void handleClick(InventoryClickEvent event) {
        // make sure event slot is in our inventory
        InventoryHandler handler = this.activeInventories.get(event.getInventory());
        if (handler != null) {
            handler.onClick(event);
        }
    }

    public void handleOpen(InventoryOpenEvent event) {
        Inventory inventory = event.getInventory();
        InventoryHandler handler = this.activeInventories.get(inventory);
        handler.onOpen(event);
    }

    public void handleClose(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();
        InventoryHandler handler = this.activeInventories.get(inventory);
        if (handler != null) {
            handler.onClose(event);
            this.unregisterInventory(inventory);
        }
    }

    public InventoryGui createInventoryFromPageId(PageIds pageId) {
        return switch (pageId) {
            case MAIN_MENU -> new MainMenu();
            case LOBBY -> new LobbyInv();
            case GAME_MENU -> new InGameMenu();
            default -> null;
        };
    }
}
