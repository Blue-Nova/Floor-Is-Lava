package com.bluenova.floorislava.util.gui.inventories.util;

import com.bluenova.floorislava.FloorIsLava;
import com.bluenova.floorislava.game.object.invitelobby.InviteLobby;
import com.bluenova.floorislava.util.gui.InventoryButton;
import com.bluenova.floorislava.util.gui.objects.Paginator;
import com.bluenova.floorislava.util.messages.MiniMessages;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;

public class PlayersToInviteInv extends Paginator<Player> {

    public PlayersToInviteInv(ArrayList<Player> allOnlinePlayers) {
        super(allOnlinePlayers);
        FloorIsLava.getInstance().getPluginLogger().info("Creating PlayersToInviteInv with " + allOnlinePlayers.size() + " players.");
    }

    @Override
    protected Inventory createInventory() {
        return Bukkit.createInventory(null, (9 * 6), MiniMessages.miniMessage.deserialize("<aqua>Players to Invite"));
    }

    @Override
    public void decorate(Player player) {
        FloorIsLava.getInstance().getPluginLogger().info("Decorating PlayersToInviteInv for player: " + player.getName());
        super.decorate(player);
    }

    @Override
    protected InventoryButton getUnitButton(Player player) {
        return new InventoryButton()
                .creator(p -> {
                    ItemStack item = new ItemStack(Material.PLAYER_HEAD);
                    SkullMeta skullMeta = (SkullMeta) item.getItemMeta();
                    if (skullMeta != null) {
                        skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(player.getUniqueId()));
                        skullMeta.displayName(MiniMessages.miniMessage.deserialize("<white>" + player.getName()));
                        ArrayList<Component> lore = new ArrayList<>();
                        lore.add(MiniMessages.miniMessage.deserialize("<gray>Click to invite"));
                        skullMeta.lore(lore);
                        skullMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                        item.setItemMeta(skullMeta);
                    }
                    return item;
                })
                .consumer(event -> {
                    Player eventPlayer = (Player) event.getWhoClicked();
                    InviteLobby lobby = FloorIsLava.getInviteLobbyManager().getLobbyFromOwner(eventPlayer);
                    if (lobby != null){
                        lobby.invite(player);
                    }
                });
    }
}