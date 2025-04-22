package com.bluenova.floorislava.game.object;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public abstract class Lobby {

    public final ArrayList<Player> players = new ArrayList<>();
    final protected Player owner;

    protected Lobby(List<Player> players, Player owner) {
        this.players.addAll(players);
        this.owner = owner;
    }

    public Player getOwner() {
        return owner;
    }
}
