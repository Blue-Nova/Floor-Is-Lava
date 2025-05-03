package com.bluenova.floorislava.util.gui;

import com.bluenova.floorislava.FloorIsLava;
import com.bluenova.floorislava.util.gui.objects.InventoryGui;
import com.bluenova.floorislava.util.gui.objects.Paginator;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class GuiManager {

    private final Map<Inventory, InventoryHandler> activeInventories = new HashMap<>();

    public void openGUI(InventoryGui gui, Player player) {
        this.registerHandledInventory(gui.getInventory(), gui);
        player.openInventory(gui.getInventory());
    }

    public void registerHandledInventory(Inventory inventory, InventoryHandler handler) {
        // if inventory key already exists, remove it and replace entry
        if (this.activeInventories.containsKey(inventory)) {
            FloorIsLava.getInstance().getPluginLogger().info("Inventory already registered, unregistering: " + inventory);
            this.unregisterInventory(inventory);
        }
        this.activeInventories.put(inventory, handler);
        FloorIsLava.getInstance().getPluginLogger().info("Active inventories: " + Arrays.toString(this.activeInventories.entrySet().toArray()));
    }

    public void unregisterInventory(Inventory inventory) {
        this.activeInventories.remove(inventory);
    }

    public void handleClick(InventoryClickEvent event) {
        InventoryHandler handler = this.activeInventories.get(event.getInventory());
        if (handler != null) {
            handler.onClick(event);
        }
    }

    public void handleOpen(InventoryOpenEvent event) {
        Inventory inventory = event.getInventory();
        InventoryHandler handler = this.activeInventories.get(inventory);
        if (handler == null) {
            FloorIsLava.getInstance().getPluginLogger().info("No handler found for inventory: " + inventory);
            return;
        }
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

}
