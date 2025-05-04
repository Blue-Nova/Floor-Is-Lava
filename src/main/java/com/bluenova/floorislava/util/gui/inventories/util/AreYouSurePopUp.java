package com.bluenova.floorislava.util.gui.inventories.util;

import com.bluenova.floorislava.FloorIsLava;
import com.bluenova.floorislava.util.gui.InventoryButton;
import com.bluenova.floorislava.util.gui.objects.InventoryGui;
import com.bluenova.floorislava.util.gui.objects.PopUp;
import com.bluenova.floorislava.util.gui.util.PageIds;
import com.bluenova.floorislava.util.messages.MiniMessages;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.function.Consumer;

public class AreYouSurePopUp extends PopUp {

    private final Consumer<InventoryClickEvent> eventConsumer;
    private final PageIds cameFrom;
    private Component titleComponent;

    public AreYouSurePopUp(Consumer<InventoryClickEvent> eventConsumer, PageIds cameFrom) {
        super();
        this.eventConsumer = eventConsumer;
        this.cameFrom = cameFrom;
    }

    @Override
    public void getPopUpButton() {
        this.addButton(4, new InventoryButton()
                .creator(player -> {
                    ItemStack item = new ItemStack(Material.RED_GLAZED_TERRACOTTA);
                    ItemMeta meta = item.getItemMeta();
                    meta.displayName(MiniMessages.miniMessage.deserialize("<red><bold>Are you sure?"));
                    ArrayList<Component> lore = new ArrayList<>();
                    lore.add(MiniMessages.miniMessage.deserialize("<gray>Click to confirm"));
                    meta.lore(lore);
                    meta.addEnchant(Enchantment.LURE, 1, true);
                    meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
                    item.setItemMeta(meta);
                    return item;
                })
                .consumer(eventConsumer)
        );

        this.addButton(getInventory().getSize()-1, new InventoryButton()
                .creator(player -> {
                    ItemStack item = new ItemStack(Material.RED_WOOL);
                    ItemMeta meta = item.getItemMeta();
                    meta.displayName(MiniMessages.miniMessage.deserialize("<red>Cancel"));
                    item.setItemMeta(meta);
                    return item;
                })
                .consumer(event -> {
                    InventoryGui gui = FloorIsLava.getInstance().getGuiManager().createInventoryFromPageId(cameFrom);
                    if (gui == null) {
                        FloorIsLava.getInstance().getPluginLogger().severe("Paginator Failed to create inventory from pageId: " + cameFrom);
                        return;
                    }
                    FloorIsLava.getInstance().getGuiManager().openGUI(gui, (Player) event.getWhoClicked());
                    Player whoClicked = (Player) event.getWhoClicked();
                    whoClicked.playSound(whoClicked.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_BASS, 0.6f, 1.2f);
                })
        );
    }

    public String getTitle() {
        return this.titleComponent == null ? "" : this.titleComponent.toString();
    }

    @Override
    protected Inventory createInventory() {
        titleComponent = MiniMessages.miniMessage.deserialize("<red><bold>Are you sure?");
        return Bukkit.createInventory(null, 9, titleComponent);
    }
}
