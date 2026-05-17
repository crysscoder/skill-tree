package dev.crysscoder.skilltree.inv;

import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import dev.crysscoder.skilltree.enums.Skill;
import dev.crysscoder.skilltree.manager.ItemManager;
import dev.crysscoder.skilltree.manager.PanelManager;
import dev.crysscoder.skilltree.util.ItemMetaUtils;

import java.util.Collections;


@AllArgsConstructor
public class AlchemistMenu implements InventoryHolder, Listener {
    private final PanelManager panelManager;
    private final ItemManager itemManager;
    private final JavaPlugin plugin;

    private final Inventory inventory = Bukkit.createInventory(this, 54, "Путь алхимика");

    private void build(Player player) {
        panelManager.setPanels(player, Skill.ALCHEMIST, inventory);
        panelManager.fillPanelSlots(inventory, new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
        itemManager.parseHead(player).thenAccept(head -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                inventory.setItem(10, head);
                player.updateInventory();
            });
        });
    }


    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void openInventory(Player player){
        build(player);
        player.openInventory(inventory);

    }
}
