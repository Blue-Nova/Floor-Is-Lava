package com.bluenova.floorislava.event.events;

import com.bluenova.floorislava.util.gui.GuiManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;


public class GuiListener implements Listener {

    private final GuiManager guiManager;

    public GuiListener(GuiManager guiManager) {
        this.guiManager = guiManager;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        this.guiManager.handleClick(event);
    }

    @EventHandler
    public void onOpen(InventoryOpenEvent event) {
        // --- START OF ADDED LOGGING ---
        Player player = (Player) event.getPlayer();
        Inventory inventory = event.getInventory();
        String invHash = "N/A";
        if (inventory != null) { // prevent NPE if inventory is somehow null
            invHash = String.valueOf(System.identityHashCode(inventory));
        }
        String playerName = (player == null) ? "NULL" : player.getName();

        // Use System.out.println to guarantee visibility in console
        // Also use plugin logger at SEVERE level
        // --- END OF ADDED LOGGING ---

        // Call the GuiManager method that handles this event
        try {
            if (this.guiManager == null) {
                System.err.println("[FloorIsLava DEBUG GuiListener] onOpen --- ERROR: guiManager field is NULL! ---");
                return;
            }
            this.guiManager.handleOpen(event); // Use 'this.' for clarity
        } catch (Throwable t) {
            t.printStackTrace(); // Print stack trace
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        this.guiManager.handleClose(event);
    }

}
