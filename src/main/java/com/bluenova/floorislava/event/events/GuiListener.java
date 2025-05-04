package com.bluenova.floorislava.event.events;

import com.bluenova.floorislava.util.gui.GuiManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;


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
