package com.bluenova.floorislava.util.gui.objects;

import com.bluenova.floorislava.FloorIsLava;
import com.bluenova.floorislava.util.gui.InventoryButton;
import com.bluenova.floorislava.util.gui.InventoryHandler;
import com.bluenova.floorislava.util.gui.inventories.lobby.LobbyInv;
import com.bluenova.floorislava.util.gui.inventories.main.MainMenu;
import com.bluenova.floorislava.util.gui.util.PageIds;
import com.bluenova.floorislava.util.messages.MiniMessages;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;

public abstract class InventoryGui implements InventoryHandler {

    Inventory inventory;
    protected final Map<Integer, InventoryButton> buttonMap = new HashMap<>();

    protected PageIds pageId;

    public InventoryGui() {
        this.inventory = this.createInventory();
    }

    public Inventory getInventory() {
        return this.inventory;
    }

    public void addButton(int slot, InventoryButton button) {
        this.buttonMap.put(slot, button);
    }

    public void decorate(Player player) {
        if (this.inventory == null) {
            return;
        }
        this.buttonMap.forEach((slot, button) -> {
            ItemStack icon = null;
                if (button == null) {
                    return;
                }
                if (button.getIconCreator() == null) {
                    this.inventory.setItem(slot, null); // Explicitly clear slot
                    return;
                }

                icon = button.getIconCreator().apply(player); // Generate the ItemStack

                if (icon == null) {
                } else {
                    this.inventory.setItem(slot, icon);
                }

        });
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getSlot();
        InventoryButton button = this.buttonMap.get(slot);
        if (button != null) {
            button.getEventConsumer().accept(event);
        }
    }

    @Override
    public void onOpen(InventoryOpenEvent event) {
        this.decorate((Player) event.getPlayer());
    }

    @Override
    public void onClose(InventoryCloseEvent event) {
    }

    protected abstract Inventory createInventory();

    protected void renderHeader(PageIds fromPage, Player player, int width) {
        for (int i = 0; i < width; i++) {
            switch (i) {
                case 0, 3, 4, 5, 6, 7, 8 -> this.addButton(i, this.createStaleButton());
                case 1 -> this.addButton(i , this.createNavigation(fromPage, PageIds.MAIN_MENU, player));
                case 2 -> this.addButton(i, this.createNavigation(fromPage,PageIds.LOBBY, player));
            }
        }
    }

    protected InventoryButton createStaleButton() {
        return new InventoryButton()
                .creator(player -> {
                    ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE); // Example item
                    ItemMeta itemMeta = item.getItemMeta();
                    if (itemMeta != null) {
                        itemMeta.setDisplayName(" ");
                        item.setItemMeta(itemMeta);
                    }
                    return item;
                })
                .consumer(event -> {
                    // Do nothing on click
                });
    }

    protected InventoryButton createNavigation(PageIds fromPage, PageIds toPage, Player eventPlayer) {

        switch (toPage) {
            case MAIN_MENU -> {
                return new InventoryButton()
                        .creator(player -> {
                            ItemStack item = new ItemStack(Material.GRASS_BLOCK); // Example item
                            ItemMeta itemMeta = item.getItemMeta();
                            if (itemMeta != null) {
                                // set display name with formatting
                                itemMeta.displayName(MiniMessages.miniMessage.deserialize("<bold><gold>Main <red>Menu"));
                                item.setItemMeta(itemMeta);
                            }
                            return item;
                        }).consumer(event -> {
                            FloorIsLava.getInstance().getGuiManager().openGUI(new MainMenu(), eventPlayer);
                        });
            }
            case LOBBY -> {
                return new InventoryButton()
                        .creator(player -> {
                            ItemStack item = new ItemStack(Material.MAGMA_BLOCK); // Example item
                            ItemMeta meta = item.getItemMeta();
                            if (meta != null) {
                                // set display name with formatting
                                meta.displayName(MiniMessages.miniMessage.deserialize("<bold><gold>Lobby <red>Menu"));
                                item.setItemMeta(meta);
                            }
                            return item;
                        }).consumer(event -> {
                            FloorIsLava.getInstance().getGuiManager().openGUI(new LobbyInv(), eventPlayer);
                        });
            }
        }
        return null; // Default case, should not be reached
    }

    protected void surroundWithStale(int width, int height, int thickness) {
        // Loop through the inventory size
        // place the stale items on the outer edge of the inventory with the given thickness
        int thicknessWidth = thickness;
        if (thickness > 1) {
            thicknessWidth = thickness - 1;
        }
        for (int i = 0; i < getInventory().getSize(); i++) {
            // Check if the slot is allowed (as explained above)
            if (i < width * thicknessWidth || i >= getInventory().getSize() - width * thicknessWidth || i % width < thickness || i % width >= width - thickness) {
                // Create a stale button and add it to the inventory
                this.addButton(i, this.createStaleButton());
            }
        }
    }
}