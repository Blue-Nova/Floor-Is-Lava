package com.bluenova.floorislava.util.gui.objects;

import com.bluenova.floorislava.FloorIsLava;
import com.bluenova.floorislava.util.gui.GuiManager;
import com.bluenova.floorislava.util.gui.InventoryButton;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public abstract class Paginator<T> extends InventoryGui {

    protected final int itemsPerPage;
    protected List<T> itemList;
    protected int currentPage = 0;

    // This inventory is created by the InventoryGui constructor (super())

    public Paginator(ArrayList<T> itemList) {
        super(); // Creates the inventory via InventoryGui constructor -> createInventory()
        // Ensure createInventory() in your Paginator implementation creates the right size (e.g., 54 slots)
        this.itemsPerPage = getInventory().getSize() - 9; // Calculate based on actual inv size minus one row for controls
        this.itemList = itemList;
        updateInventoryContent(); // Initial population of buttonMap. Decoration happens in onOpen.
    }

    protected void updateInventoryContent() {
        FloorIsLava.getInstance().getPluginLogger().info("Updating inventory content map for page " + currentPage);

        if (this.inventory != null) {
            this.inventory.clear();
        }

        // *** 3. Clear the button map to prepare for the new page's buttons ***
        this.buttonMap.clear();

        // --- Add Item Buttons ---
        int startIndex = currentPage * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, itemList.size());

        FloorIsLava.getInstance().getPluginLogger().debug(
                "Calculating loop bounds: currentPage=" + currentPage +
                        ", itemsPerPage=" + itemsPerPage +
                        ", calculatedStartIndex=" + startIndex +
                        ", itemList.size=" + (itemList == null ? "null" : itemList.size()) + // Check for null list
                        ", calculatedEndIndex=" + endIndex
        );
        for (int i = startIndex; i < endIndex; i++) {
            int itemSlot = i - startIndex; // Slot index within the item display area (0 to itemsPerPage-1)
            InventoryButton button = getUnitButton(itemList.get(i));
            FloorIsLava.getInstance().getPluginLogger().info("Mapping button for item index " + i + " to slot " + itemSlot);
            if (button != null) {
                // Add button logic to map. Visual item placement is done by decorate().
                this.addButton(itemSlot, button);
            }
        }

        // --- Add Navigation Buttons ---
        int inventorySize = getInventory().getSize();
        int previousButtonSlot = inventorySize - 9; // e.g., slot 45 in a 54-slot inventory
        int nextButtonSlot = inventorySize - 1;     // e.g., slot 53 in a 54-slot inventory

        if (currentPage > 0) {
            this.addButton(previousButtonSlot, new InventoryButton()
                    .creator(player -> new ItemStack(Material.ARROW)) // Creator used by decorate
                    .consumer(event -> this.previousPage((Player) event.getWhoClicked())));
        } else {
            // Optional: Add a placeholder if you want the slot filled when disabled
            //addButton(previousButtonSlot, createStaleButton()); // Example using InventoryGui's method
        }

        if (itemList != null && (currentPage + 1) * itemsPerPage < itemList.size()) {
            this.addButton(nextButtonSlot, new InventoryButton()
                    .creator(p -> new ItemStack(Material.ARROW))
                    .consumer(event -> {
                        // *** ADD TRY-CATCH HERE ***
                        FloorIsLava.getInstance().getPluginLogger().info("Next Page Button CLICKED - Attempting to call nextPage()..."); // Log right before call
                        this.nextPage((Player) event.getWhoClicked());
                        FloorIsLava.getInstance().getPluginLogger().info("Next Page Button CLICKED - Call to nextPage() completed."); // Log right after call
                    }));
        } else {
            // Optional: Add a placeholder
            // addButton(nextButtonSlot, createStaleButton()); // Example
        }

        // *** 4. DO NOT call decorate() here. It runs in onOpen or after page change + reopen ***
    }


    public void nextPage(Player player) {
        boolean condition = (itemList != null && (currentPage + 1) * itemsPerPage < itemList.size());
        if (condition) {
            currentPage++;
            updateInventoryContent();
            GuiManager manager = FloorIsLava.getInstance().getGuiManager();
            FloorIsLava.getInstance().getPluginLogger().info("Next page clicked, THIS:" + this);
            FloorIsLava.getInstance().getPluginLogger().info("Next page clicked, THIS Inventory:" + this.getInventory());
            manager.openGUI(this, player);
        }
    }

    public void previousPage(Player player) {
        if (currentPage > 0) {
            currentPage--;
            updateInventoryContent();
            FloorIsLava.getInstance().getGuiManager().openGUI(this, player);
        }
    }

    protected abstract InventoryButton getUnitButton(T item);


    @Override
    public void onOpen(InventoryOpenEvent event) {
        FloorIsLava.getInstance().getPluginLogger().info("onOpen inventory for page " + currentPage);

        this.decorate((Player) event.getPlayer());

        ((Player) event.getPlayer()).updateInventory();
    }
}