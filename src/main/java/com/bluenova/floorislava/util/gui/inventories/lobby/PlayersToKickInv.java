package com.bluenova.floorislava.util.gui.inventories.lobby;

import com.bluenova.floorislava.FloorIsLava;
import com.bluenova.floorislava.game.object.invitelobby.InviteLobbyManager;
import com.bluenova.floorislava.util.gui.InventoryButton;
import com.bluenova.floorislava.util.gui.objects.Paginator;
import com.bluenova.floorislava.util.gui.util.PageIds;
import com.bluenova.floorislava.util.messages.MiniMessages;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;

public class PlayersToKickInv extends Paginator<Player> {

    InviteLobbyManager inviteLobbyManager;

    public PlayersToKickInv(ArrayList<Player> itemList, PageIds cameFrom, Player viewer) {
        super(itemList, cameFrom, viewer);
    }

    @Override
    protected Inventory createInventory() {
        this.inviteLobbyManager = FloorIsLava.getInviteLobbyManager();
        return Bukkit.createInventory(null, (9 * 6), MiniMessages.miniMessage.deserialize("<bold><gold>Players <white>to <red>Kick"));
    }

    @Override
    public void decorate(Player player) {
        updateInventoryContent();
        super.decorate(player);
    }

    @Override
    protected InventoryButton getUnitButton(Player playerToKick, Player viewer) {
        boolean playerInLobby = inviteLobbyManager.isPlayerInOwnersLobby(playerToKick, viewer);

        return new InventoryButton()
                .creator(p -> {
                    if (!playerInLobby) {
                        ItemStack item = new ItemStack(Material.WITHER_SKELETON_SKULL);
                        SkullMeta skullMeta = (SkullMeta) item.getItemMeta();
                        if (skullMeta != null) {
                            skullMeta.displayName(MiniMessages.miniMessage.deserialize("<red>" + playerToKick.getName()));
                            ArrayList<Component> lore = new ArrayList<>();
                            lore.add(MiniMessages.miniMessage.deserialize("<gray>has been kicked"));
                            lore.add(MiniMessages.miniMessage.deserialize("<font:uniform><gray>ouch..."));
                            skullMeta.lore(lore);
                            item.setItemMeta(skullMeta);
                        }
                        return item;
                    }else{
                        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
                        SkullMeta skullMeta = (SkullMeta) item.getItemMeta();
                        if (skullMeta != null) {
                            skullMeta.displayName(MiniMessages.miniMessage.deserialize("<red>" + playerToKick.getName()));
                            skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(playerToKick.getUniqueId()));
                            ArrayList<Component> lore = new ArrayList<>();
                            lore.add(MiniMessages.miniMessage.deserialize("<gray>Click to kick " + playerToKick.getName()));
                            item.setItemMeta(skullMeta);
                        }
                    return item;
                    }
                })
                .consumer(event -> {
                    if (!playerInLobby) {
                        Player player = (Player) event.getWhoClicked();
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.6f, 1.2f);
                        this.decorate(viewer);
                    }else{
                        inviteLobbyManager.kickPlayerFromLobby(playerToKick, viewer);
                        Player player = (Player) event.getWhoClicked();
                        player.playSound(player.getLocation(), Sound.UI_STONECUTTER_TAKE_RESULT, 0.7f, 0.7f);
                        this.decorate(viewer);
                    }
                });
    }
}
