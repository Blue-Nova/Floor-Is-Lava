package com.bluenova.floorislava.util.gui.inventories.main;

import com.bluenova.floorislava.util.gui.objects.InventoryGui;
import com.bluenova.floorislava.util.gui.util.PageIds;
import com.bluenova.floorislava.util.messages.MiniMessages;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class MainMenu extends InventoryGui {

    int width;
    int height;

    public MainMenu() {
        super();
        this.pageId = PageIds.MAIN_MENU;
    }

    @Override
    protected Inventory createInventory() {
        width = 9;
        height = 6;
        return Bukkit.createInventory(null, width*height, MiniMessages.miniMessage.deserialize("<bold><gold>Main <red>Menu"));
    }

    @Override
    public void decorate(Player player) {
        renderHeader(pageId ,player, width);
        super.decorate(player);
    }
}
