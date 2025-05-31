package com.bluenova.floorislava.util.gui.inventories.lobby;

import com.bluenova.floorislava.FloorIsLava;
import com.bluenova.floorislava.game.object.invitelobby.InviteLobby;
import com.bluenova.floorislava.game.object.invitelobby.InviteLobbyManager;
import com.bluenova.floorislava.util.gui.InventoryButton;
import com.bluenova.floorislava.util.gui.objects.Paginator;
import com.bluenova.floorislava.util.gui.util.PageIds;
import com.bluenova.floorislava.util.messages.MiniMessages;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;

public class PlayersToInviteInv extends Paginator<Player> {

    private final InviteLobbyManager inviteLobbyManager = FloorIsLava.getInviteLobbyManager();

    public PlayersToInviteInv(ArrayList<Player> allOnlinePlayers, PageIds cameFrom, Player viewer) {
        super(allOnlinePlayers, cameFrom, viewer);
    }

    @Override
    protected Inventory createInventory() {
        return Bukkit.createInventory(null, (9 * 6), MiniMessages.createComponent("<bold><gold>Players <white>to <red>Invite"));
    }

    @Override
    public void decorate(Player player) {
        super.decorate(player);
    }

    @Override
    protected InventoryButton getUnitButton(Player player, Player viewer) {
        return new InventoryButton()
                .creator(p -> {
                    ItemStack item = new ItemStack(Material.PLAYER_HEAD);
                    SkullMeta skullMeta = (SkullMeta) item.getItemMeta();
                    if (skullMeta != null) {
                        skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(player.getUniqueId()));
                        ArrayList<Component> lore = new ArrayList<>();
                        if (inviteLobbyManager.isPlayerInOwnersLobby(player, viewer) || inviteLobbyManager.isPlayerInvitedBy(player, viewer)) {
                            item = new ItemStack(Material.PAPER);
                            ItemMeta meta = item.getItemMeta();
                            if (meta != null){
                                meta.displayName(MiniMessages.createComponent("<red>" + player.getName()));
                                lore.add(MiniMessages.createComponent("<gray>Already Invited or in your lobby"));
                                meta.lore(lore);
                                meta.addEnchant(Enchantment.LURE, 1, true);
                                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                            }
                            item.setItemMeta(meta);
                        } else {
                            skullMeta.displayName(MiniMessages.createComponent("<white>" + player.getName()));
                            lore.add(MiniMessages.createComponent("<gray>Click to invite"));
                            skullMeta.lore(lore);
                            skullMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                            item.setItemMeta(skullMeta);
                        }
                    }
                    return item;
                })
                .consumer(event -> {
                    Player eventPlayer = (Player) event.getWhoClicked();
                    InviteLobby lobby = FloorIsLava.getInviteLobbyManager().getLobbyFromOwner(eventPlayer);
                    if (lobby != null){
                        // if already invited or in lobby
                        if (inviteLobbyManager.isPlayerInOwnersLobby(player, eventPlayer) || inviteLobbyManager.isPlayerInvitedBy(player, eventPlayer)) {
                            eventPlayer.playSound(eventPlayer.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                            return;
                        }
                        // otherwise invite
                        lobby.invite(player);
                        this.decorate(eventPlayer);
                        eventPlayer.playSound(eventPlayer.getLocation(), Sound.ENTITY_PARROT_FLY, 1f, 1f);
                    }
                });
    }
}