package com.bluenova.floorislava.game.object.invitelobby;

import com.bluenova.floorislava.FloorIsLava; // Keep for getPluginNameFormatted (can be injected too)
import com.bluenova.floorislava.game.object.Lobby;
import com.bluenova.floorislava.game.object.gamelobby.GameLobbyManager;
// Import MiniMessages & Adventure components
import com.bluenova.floorislava.util.messages.MiniMessages;
import com.bluenova.floorislava.util.worldguard.FILRegionManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
// Other Imports
import com.sk89q.worldedit.WorldEditException; // Still needed for startGame signature if not changed
import org.bukkit.Bukkit; // Needed for Bukkit.getPlayer
import org.bukkit.ChatColor; // Can likely remove now
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.stream.Collectors; // For joining names

public class InviteLobby extends Lobby {

    public ArrayList<Player> sentList = new ArrayList<>();
    // --- Dependencies Injected via Constructor ---
    private final InviteLobbyManager inviteLobbyManager;
    private final FILRegionManager regionManager; // Not used in this class, but might be needed for region management
    // ---

    // Constructor now accepts managers
    public InviteLobby(Player owner, InviteLobbyManager lobbyManager, FILRegionManager regionManager) {
        super(new ArrayList<>(), owner); // Pass owner to superclass
        this.inviteLobbyManager = lobbyManager;
        this.regionManager = regionManager;
        this.players.add(owner); // Add owner to inherited 'players' list
        // Registration with manager should happen in InviteLobbyManager.createLobby
    }

    // Refactored to use MiniMessages and handle feedback better
    public void invitePlayers(ArrayList<Player> inviteList) {
        ArrayList<String> successfullyInvitedNames = new ArrayList<>();
        ArrayList<String> failedInvitesReasons = new ArrayList<>(); // Store name + reason

        for (Player invitedPlayer: inviteList) {
            // Check if already invited/joined using internal helper
            if (checkPlayerAlreadyInvited(invitedPlayer)) {
                failedInvitesReasons.add(invitedPlayer.getName() + " (Already Invited/Joined)");
                continue;
            }
            // Check if target is already in another lobby using the manager
            if (inviteLobbyManager.isPlayerInLobby(invitedPlayer) && !this.players.contains(invitedPlayer)) {
                failedInvitesReasons.add(invitedPlayer.getName() + " (In Another Lobby)");
                continue;
            }

            // If checks pass, send invite and track success
            invite(invitedPlayer); // Sends the actual invite message
            sentList.add(invitedPlayer);
            successfullyInvitedNames.add(invitedPlayer.getName());
        }

        // Send feedback to owner
        if (!successfullyInvitedNames.isEmpty()) {
            TagResolver successPlaceholders = TagResolver.resolver(
                    Placeholder.unparsed("player_list", String.join("<newline>", successfullyInvitedNames))
            );
            // Assumes key: lobby.invite_sent_feedback: "<white>Invites sent to: <aqua><player_list></aqua>.</white>"
            MiniMessages.send(this.getOwner(), "lobby.invite_sent_header", successPlaceholders);
        }
        if (!failedInvitesReasons.isEmpty()) {
            TagResolver failPlaceholders = TagResolver.resolver(
                    Placeholder.unparsed("player_list", String.join("<gray>, </gray><br>", failedInvitesReasons))
            );
            // Assumes key: lobby.invite_failed_feedback: "<red>Could not invite: <gray><player_list></gray>.</red>"
            MiniMessages.send(this.getOwner(), "lobby.invite_failed_header", failPlaceholders);
        }
    }

    // Refactored to use MiniMessages and integrated click event
    private void invite(Player invitedPlayer){
        invitedPlayer.playSound(invitedPlayer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);

        String ownerName = this.getOwner().getName();

// --- Create the clickable [Accept] component ---
// 1. Get button text (uses getParsedComponent - OK as no placeholders needed here)
        Component acceptText = MiniMessages.getParsedComponent("lobby.invite_accept_button_text");

// 2. Create command string
        String acceptCommand = "/fil lobby accept " + ownerName;

// 3. Create click event
        ClickEvent clickEvent = ClickEvent.runCommand(acceptCommand);

// 4. Create hover text component - CORRECTED LOGIC
        Component hoverText = Component.empty(); // Default value
        String rawHoverText = MiniMessages.getRawMessage("lobby.invite_accept_button_hover"); // Get raw string first
        if (rawHoverText != null) {
            // Create placeholder needed *specifically for the hover text*
            TagResolver hoverPlaceholder = TagResolver.resolver(Placeholder.unparsed("inviter", ownerName));
            // Use the public parser instance directly with the placeholder
            hoverText = MiniMessages.miniMessage.deserialize(rawHoverText, hoverPlaceholder);
        }
        HoverEvent<Component> hoverEvent = HoverEvent.showText(hoverText); // Create hover event

// 5. Apply events to the button text component
        acceptText = acceptText.clickEvent(clickEvent).hoverEvent(hoverEvent);
// --- Clickable component created ---


// --- Prepare placeholders for the main message ---
        TagResolver mainPlaceholders = TagResolver.resolver(
                Placeholder.unparsed("inviter", ownerName), // For main message text
                Placeholder.component("plugin_name", MiniMessages.getParsedComponent("general.plugin_name_formatted")),
                Placeholder.component("accept_button", acceptText) // Insert the fully built button component
        );

// Send the main invite_received message
        MiniMessages.send(invitedPlayer, "lobby.invite_received", mainPlaceholders);
    }

    // Refactored to use MiniMessages and coordinate with manager
    public void inviteAccept(Player player) {
        // This check might be redundant if LobbyAcceptCmd already does it fully
        if (!sentList.contains(player)) {
            MiniMessages.send(player, "lobby.invite_not_pending", Placeholder.unparsed("player", this.getOwner().getName()));
            return;
        }

        sentList.remove(player);
        this.players.add(player);

        // --- Notify Manager ---
        // Important: Tell the manager this player is now in *this* lobby
        // inviteLobbyManager.addPlayerToLobbyMap(player, this); // Manager needs this method!
        // ---

        // Send feedback using MiniMessages
        MiniMessages.send(this.getOwner(), "lobby.invite_accepted_to_owner",
                Placeholder.unparsed("player", player.getName())
        );
        MiniMessages.send(player, "lobby.invite_accepted_to_player",
                Placeholder.unparsed("lobby_owner", this.getOwner().getName())
        );
    }

    // Refactored to use MiniMessages and coordinate with manager
    public void removePlayer(Player removingPlayer) {
        // Use injected manager instance to check ownership
        boolean isOwner = this.getOwner().equals(removingPlayer);

        if (isOwner) {
            // Owner is leaving - Manager handles disbanding
            inviteLobbyManager.closeLobby(this); // closeLobby should handle map cleanup

            // Notify remaining players
            TagResolver ownerPlaceholder = Placeholder.unparsed("lobby_owner", this.getOwner().getName());
            ArrayList<Player> playersToNotify = new ArrayList<>(this.players); // Copy list to avoid issues
            for (Player player : playersToNotify) {
                if (!player.equals(removingPlayer)) { // Don't message the owner who left
                    player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 0.5f, 1f);
                    MiniMessages.send(player, "lobby.lobby_disbanded_notification", ownerPlaceholder);
                }
            }
            // Notify owner separately
            MiniMessages.send(this.getOwner(), "lobby.lobby_disbanded_owner");
            return; // Manager handles removal from list/maps via closeLobby
        }

        // Member is leaving or being kicked
        boolean wasInJoined = this.players.contains(removingPlayer);
        boolean wasInSent = sentList.contains(removingPlayer);

        if (!wasInJoined && !wasInSent) {
            // This check might be redundant if calling command checks first
            MiniMessages.send(this.getOwner(), "lobby.player_not_found_in_lobby", Placeholder.unparsed("player", removingPlayer.getName())); // Add key: "<red><player> is not in your lobby.</red>"
            return;
        }

        // Remove from internal lists
        this.players.remove(removingPlayer);
        sentList.remove(removingPlayer);

        // --- Notify Manager ---
        // Important: Tell manager player is no longer associated with this lobby
        // if (wasInJoined) { // Only need to remove from player map if they were actually joined
        //     inviteLobbyManager.removePlayerFromLobbyMap(removingPlayer); // Manager needs this method!
        // }
        // ---

        // Send feedback
        TagResolver ownerPlaceholder = Placeholder.unparsed("lobby_owner", this.getOwner().getName());
        TagResolver playerPlaceholder = Placeholder.unparsed("player", removingPlayer.getName());

        // Send different message depending on whether it was a leave or kick?
        // Assuming leave for now, called by LobbyLeaveCmd
        MiniMessages.send(removingPlayer, "lobby.leaving_lobby_feedback", ownerPlaceholder);
        MiniMessages.send(this.getOwner(), "lobby.player_left_notification", playerPlaceholder);
        // Also notify other players?
        // for (Player p : this.players) { MiniMessages.send(p, "lobby.player_left_notification", playerPlaceholder); }
    }

    // checkPlayerAlreadyInvited unchanged - logic seems fine
    public boolean checkPlayerAlreadyInvited(Player targetPlayer) {
        if (sentList.contains(targetPlayer)) return true;
        if (this.players.contains(targetPlayer)) return true;
        return false;
    }

    public void kickPlayers(ArrayList<Player> playersToKick) {
        for (Player player : playersToKick) {
            if (this.players.contains(player)) {
                this.players.remove(player);
                MiniMessages.send(player, "lobby.kicked_player_notification", Placeholder.unparsed("lobby_owner", this.getOwner().getName()));
                MiniMessages.send(this.getOwner(), "lobby.kicked_player_feedback", Placeholder.unparsed("player", player.getName()));
            }
        }
    }
}