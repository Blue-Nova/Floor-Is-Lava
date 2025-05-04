package com.bluenova.floorislava.util.gui.objects;

import org.bukkit.entity.Player;

public abstract class PopUp extends InventoryGui{

    public PopUp() {
        super();
    }

    @Override
    public void decorate(Player player) {
        surroundWithStale(9, 1, 1);
        getPopUpButton();
        super.decorate(player);
    }

    public abstract void getPopUpButton();

}
