package com.bluenova.floorislava.util.gui.objects;

import com.bluenova.floorislava.FloorIsLava;
import com.bluenova.floorislava.util.gui.GuiManager;
import com.bluenova.floorislava.util.gui.InventoryButton;
import com.bluenova.floorislava.util.gui.util.PageIds;
import com.bluenova.floorislava.util.messages.MiniMessages;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public abstract class Paginator<T> extends InventoryGui {

    protected final int itemsPerPage;
    protected List<T> itemList;
    protected int currentPage = 0;
    protected Player viewer = null; // This is set in the constructor of the Paginator

    protected PageIds cameFrom = null; // This is set in the constructor of the Paginator

    // This inventory is created by the InventoryGui constructor (super())

    public Paginator(ArrayList<T> itemList, PageIds cameFrom, Player viewer) {
        super(); // Creates the inventory via InventoryGui constructor -> createInventory()
        // Ensure createInventory() in your Paginator implementation creates the right size (e.g., 54 slots)
        this.itemsPerPage = getInventory().getSize() - 9; // Calculate based on actual inv size minus one row for controls
        this.itemList = itemList;
        this.cameFrom = cameFrom; // Set the pageId to the one we came from
        this.viewer = viewer; // Set the viewer to the player who opened the inventory
        updateInventoryContent(); // Initial population of buttonMap. Decoration happens in onOpen.
    }

    protected void updateInventoryContent() {
        if (this.getInventory() != null) {
            this.getInventory().clear();
        }

        // *** 3. Clear the button map to prepare for the new page's buttons ***
        this.buttonMap.clear();

        // --- Add Item Buttons ---
        int startIndex = currentPage * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, itemList.size());

        for (int i = startIndex; i < endIndex; i++) {
            int itemSlot = i - startIndex; // Slot index within the item display area (0 to itemsPerPage-1)
            InventoryButton button = getUnitButton(itemList.get(i), viewer);
            if (button != null) {
                this.addButton(itemSlot, button);
            }
        }

        // --- Add Navigation Buttons ---
        int inventorySize = getInventory().getSize();
        int previousButtonSlot = inventorySize - 9; // e.g., slot 45 in a 54-slot inventory
        int nextButtonSlot = inventorySize - 1;     // e.g., slot 53 in a 54-slot inventory

        if (currentPage > 0) {
            this.addButton(previousButtonSlot, new InventoryButton()
                    .creator(player -> {
                        ItemStack item = new ItemStack(Material.ARROW);
                        ItemMeta itemMeta = item.getItemMeta();
                        if (itemMeta != null) {
                            itemMeta.displayName(MiniMessages.createComponent("<white>Previous Page"));
                            item.setItemMeta(itemMeta);
                        }
                        return item;
                    }) // Creator used by decorate
                    .consumer(event -> this.previousPage((Player) event.getWhoClicked())));
        }

        if (itemList != null && (currentPage + 1) * itemsPerPage < itemList.size()) {
            this.addButton(nextButtonSlot, new InventoryButton()
                    .creator(p -> {
                        ItemStack item = new ItemStack(Material.ARROW);
                        ItemMeta itemMeta = item.getItemMeta();
                        if (itemMeta != null) {
                            itemMeta.displayName(MiniMessages.createComponent("<white>Next Page"));
                            item.setItemMeta(itemMeta);
                        }
                        return item;
                    })
                    .consumer(event -> {
                        this.nextPage((Player) event.getWhoClicked());
                    }));
        }

        // return button
        this.addButton(inventorySize - 5, new InventoryButton()
                .creator(player -> {
                    ItemStack item =  new ItemStack(Material.BARRIER);
                    ItemMeta itemMeta = item.getItemMeta();
                    if (itemMeta != null) {
                        itemMeta.displayName(MiniMessages.createComponent("<red>Return"));
                        item.setItemMeta(itemMeta);
                    }
                    return item;
                })
                .consumer(event -> {
                    GuiManager guiManager = FloorIsLava.getInstance().getGuiManager();
                    InventoryGui gui = guiManager.createInventoryFromPageId(cameFrom);
                    if (gui == null) {
                        FloorIsLava.getInstance().getPluginLogger().severe("Paginator Failed to create inventory from pageId: " + cameFrom);
                        return;
                    }
                    Player whoClicked = (Player) event.getWhoClicked();
                    guiManager.openGUI(gui, whoClicked);
                    whoClicked.playSound(whoClicked.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_BASS, 0.6f, 1.2f);
                }));

    }


    public void nextPage(Player player) {
        boolean condition = (itemList != null && (currentPage + 1) * itemsPerPage < itemList.size());
        if (condition) {
            currentPage++;
            updateInventoryContent();
            this.decorate(player);
        }
    }

    public void previousPage(Player player) {
        if (currentPage > 0) {
            currentPage--;
            updateInventoryContent();
            this.decorate(player);
        }
    }

    protected abstract InventoryButton getUnitButton(T item, Player viewer);

}