package dev.crysscoder.skilltree.service;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import dev.crysscoder.skilltree.inv.AlchemistMenu;
import dev.crysscoder.skilltree.inv.FarmerMenu;
import dev.crysscoder.skilltree.inv.WarriorMenu;

public class GuiService implements Listener {

    @EventHandler
    public void on(InventoryClickEvent event) {
        if (event.getInventory().getHolder(false) instanceof WarriorMenu wm) {
            onClick(event);
        }

        if (event.getInventory().getHolder(false) instanceof FarmerMenu) {
            onClick(event);
        }

        if (event.getInventory().getHolder(false) instanceof AlchemistMenu) {
            onClick(event);
        }
    }

    public void onClick(InventoryClickEvent event) {
        event.setCancelled(true);
    }

}
