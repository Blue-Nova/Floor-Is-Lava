package com.bluenova.floorislava.util.gui.inventories.lobby;

import com.bluenova.floorislava.FloorIsLava;
import com.bluenova.floorislava.game.object.invitelobby.InviteLobbyManager;
import com.bluenova.floorislava.util.gui.InventoryButton;
import com.bluenova.floorislava.util.gui.objects.Paginator;
import com.bluenova.floorislava.util.gui.util.PageIds;
import com.bluenova.floorislava.util.messages.MiniMessages;
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
        return Bukkit.createInventory(null, (9 * 6), MiniMessages.legacy("<bold><gold>Players <white>to <red>Kick"));
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
                            skullMeta.setDisplayName(MiniMessages.legacy("<red>" + playerToKick.getName()));
                            ArrayList<String> lore = new ArrayList<>();
                            lore.add(MiniMessages.legacy("<gray>has been kicked"));
                            lore.add(MiniMessages.legacy("<font:uniform><gray>ouch..."));
                            skullMeta.setLore(lore);
                            item.setItemMeta(skullMeta);
                        }
                        return item;
                    }else{
                        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
                        SkullMeta skullMeta = (SkullMeta) item.getItemMeta();
                        if (skullMeta != null) {
                            skullMeta.setDisplayName(MiniMessages.legacy("<red>" + playerToKick.getName()));
                            skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(playerToKick.getUniqueId()));
                            ArrayList<String> lore = new ArrayList<>();
                            lore.add(MiniMessages.legacy("<gray>Click to kick " + playerToKick.getName()));
                            skullMeta.setLore(lore);
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
