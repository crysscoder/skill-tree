package dev.crysscoder.skilltree.inv;

import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import dev.crysscoder.skilltree.data.PlayerData;
import dev.crysscoder.skilltree.data.Task;
import dev.crysscoder.skilltree.enums.Status;
import dev.crysscoder.skilltree.manager.ChallengeManager;
import dev.crysscoder.skilltree.manager.ConfigManager;
import dev.crysscoder.skilltree.storage.MySqlStorage;
import dev.crysscoder.skilltree.util.ItemMetaUtils;
import dev.crysscoder.skilltree.enums.Skill;

import java.util.Arrays;


public class ChoiceMenu implements InventoryHolder, Listener {
    final Inventory inventory = Bukkit.createInventory(this, 27, ChatColor.DARK_BLUE + "Выберите класс");

    private final WarriorMenu warriorMenu;
    private final FarmerMenu farmerMenuHolder;
    private final AlchemistMenu alchemistMenu;
    private final MySqlStorage mySqlStorage;
    private final ChallengeManager challengeManager;

    public ChoiceMenu(WarriorMenu warriorMenu, FarmerMenu farmerMenuHolder, AlchemistMenu alchemistMenu, MySqlStorage mySqlStorage, ChallengeManager challengeManager) {
        this.warriorMenu = warriorMenu;
        this.farmerMenuHolder = farmerMenuHolder;
        this.alchemistMenu = alchemistMenu;
        this.mySqlStorage = mySqlStorage;
        this.challengeManager = challengeManager;

        setupInventory();
    }


    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void setupInventory(){
        final ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        filler.setItemMeta(fillerMeta);
        for (int i = 0; i < 27; i++) {
            inventory.setItem(i, filler);
        }

        final ItemStack headWarrior = new ItemStack(Material.IRON_SWORD);
        final ItemStack headFarmer = new ItemStack(Material.WHEAT);
        final ItemStack headAlchemist = new ItemStack(Material.POTION);


        ItemMetaUtils.applyItemMeta(headWarrior, ChatColor.RED + "Путь воина", Arrays.asList(ChatColor.GRAY + "Стань мастером боя", ChatColor.GREEN + "Доступно"));
        ItemMetaUtils.applyItemMeta(headFarmer, ChatColor.GREEN + "Путь фермера", Arrays.asList(ChatColor.GRAY + "Выращивай урожай", ChatColor.GREEN + "Доступно"));
        ItemMetaUtils.applyItemMeta(headAlchemist, ChatColor.YELLOW + "Путь алхимика", Arrays.asList(ChatColor.GRAY + "Создавай зелья", ChatColor.RED + "Доступно"));

        inventory.setItem(11, headWarrior);
        inventory.setItem(13, headFarmer);
        inventory.setItem(15, headAlchemist);

    }

    public void openInventory(Player player){
        player.openInventory(inventory);
    }

    @EventHandler
    public void onClickInventoryPlayer(InventoryClickEvent event) {
        final Player player = (Player) event.getWhoClicked();
        final Inventory inv = event.getClickedInventory();
        final ItemStack item = event.getCurrentItem();


        if (inv != null && item != null && inv.getHolder() instanceof ChoiceMenu) {
            event.setCancelled(true);

            switch (item.getType()) {
                case IRON_SWORD -> {
                    giveFirstChallengeToPlayer(player, "warrior", Skill.WARRIOR);
                    mySqlStorage.updatePlayer(new PlayerData(player.getName(), Skill.WARRIOR, 0));
                    warriorMenu.openInventory(player);
                }
                case WHEAT -> {
                    giveFirstChallengeToPlayer(player, "farmer", Skill.FARMER);
                    mySqlStorage.updatePlayer(new PlayerData(player.getName(), Skill.FARMER, 0));
                    farmerMenuHolder.openInventory(player);
                }
                case POTION -> {
                    giveFirstChallengeToPlayer(player, "alchemist", Skill.ALCHEMIST);
                    mySqlStorage.updatePlayer(new PlayerData(player.getName(), Skill.ALCHEMIST, 0));
                    alchemistMenu.openInventory(player);
                }
                default -> player.sendMessage("Выберите меню достижений");
            }
        }
    }

    private void giveFirstChallengeToPlayer(Player player, String classPrefix, Skill skill) {
        mySqlStorage.getPlayer(player.getName()).thenAccept(playerData -> {
            if (playerData == null) {
                PlayerData newPlayer = new PlayerData(player.getName(), skill, 0);
                mySqlStorage.addPlayer(newPlayer);
                mySqlStorage.getPlayer(player.getName()).thenAccept(addedPlayer -> {
                    if (addedPlayer != null) {
                        giveTask(addedPlayer, classPrefix);
                    }
                });
            } else {
                giveTask(playerData, classPrefix);
            }
        });
    }

    private void giveTask(PlayerData playerData, String classPrefix) {
        ConfigManager.Challenge challenge = challengeManager.getFirstChallengeForClass(classPrefix);
        if (challenge == null) {
            return;
        }

        Task task = new Task(0, playerData.getId(), challenge.getDisplayName(), challenge.getId(), Status.IN_PROGRESS, 0);
        mySqlStorage.addTask(task);
    }
}



