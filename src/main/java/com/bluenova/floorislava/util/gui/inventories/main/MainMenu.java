package com.bluenova.floorislava.util.gui.inventories.main;

import com.bluenova.floorislava.util.gui.InventoryButton;
import com.bluenova.floorislava.util.gui.objects.InventoryGui;
import com.bluenova.floorislava.util.gui.util.PageIds;
import com.bluenova.floorislava.util.messages.MiniMessages;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

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
        return Bukkit.createInventory(null, width*height, MiniMessages.createComponent("<bold><gold>Main <red>Menu"));
    }

    @Override
    public void decorate(Player player) {
        bordersWithExit(width, height, 1);
        renderHeader(pageId ,player, width);
        addButton(width*2+(4), renderContactButton());
        super.decorate(player);
    }

    private InventoryButton renderContactButton() {
        return new InventoryButton()
                .creator(player -> {
                    ItemStack item = new ItemStack(Material.RECOVERY_COMPASS);
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null){
                        meta.displayName(MiniMessages.createComponent("<bold><blue>Contact <aqua>Us"));
                        ArrayList<Component> lore = new ArrayList<>();
                        lore.add(MiniMessages.createComponent("<white>Found a bug? Want to suggest something?"));
                        lore.add(MiniMessages.createComponent("<aqua>Reach me on Discord! :)"));
                        lore.add(MiniMessages.createComponent("<white>Click to get Discord invite"));
                        meta.lore(lore);
                        item.setItemMeta(meta);
                    }
                    return item;
                })
                .consumer(event -> {
                    Player player = (Player) event.getWhoClicked();
                    player.closeInventory();
                    player.playSound(player.getLocation(), Sound.ENTITY_CAT_AMBIENT, 1f, 1.2f);
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 0.8f);
                    player.sendMessage(MiniMessages.createComponent("<aqua><click:open_url:https://discord.gg/ZNSuMUfEDe>Click Here</click><yellow> to Join the discord!"));
                });
    }
}
