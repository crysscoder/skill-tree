package dev.crysscoder.skilltree;

import dev.crysscoder.skilltree.command.AdminCommand;
import dev.crysscoder.skilltree.event.*;
import dev.crysscoder.skilltree.inv.AlchemistMenu;
import dev.crysscoder.skilltree.inv.ChoiceMenu;
import org.bukkit.plugin.java.JavaPlugin;

import dev.crysscoder.skilltree.inv.FarmerMenu;
import dev.crysscoder.skilltree.inv.WarriorMenu;
import dev.crysscoder.skilltree.manager.*;
import dev.crysscoder.skilltree.service.GuiService;
import dev.crysscoder.skilltree.storage.MySqlStorage;

public final class SkillTree extends JavaPlugin {
    private MySqlStorage mySqlStorage;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadConfig();
        saveResource("bd.yml", false);
        final ConfigManager configManager = new ConfigManager(this);
        configManager.load(getConfig());

        mySqlStorage = new MySqlStorage(this);


        final ChallengeManager challengeManager = new ChallengeManager(configManager, mySqlStorage);
        final PanelManager panelManager = new PanelManager(configManager, mySqlStorage, challengeManager);
        final ItemManager itemManager = new ItemManager(mySqlStorage, configManager, this);
        final EventManager eventManager = new EventManager(challengeManager, mySqlStorage);
        panelManager.initializeSkillTasks();

        final WarriorMenu warriorMenu = new WarriorMenu(panelManager, itemManager, this);
        final FarmerMenu farmerMenuHolder = new FarmerMenu(panelManager, itemManager, this);
        final AlchemistMenu alchemistMenu = new AlchemistMenu(panelManager, itemManager, this);

        final ItemBreakEvent itemBreakEvent = new ItemBreakEvent(challengeManager, mySqlStorage, this, eventManager);


        final ChoiceMenu choiceMenu = new ChoiceMenu(
                warriorMenu,
                farmerMenuHolder,
                alchemistMenu,
                mySqlStorage,
                challengeManager);
        getCommand("skilltree").setExecutor(new AdminCommand(this, mySqlStorage, choiceMenu, warriorMenu, farmerMenuHolder, alchemistMenu));
        getServer().getPluginManager().registerEvents(choiceMenu, this);
        getServer().getPluginManager().registerEvents(new MobKillEvent(mySqlStorage, configManager, challengeManager, eventManager), this);
        getServer().getPluginManager().registerEvents(new BlocksBreakEvent(challengeManager, mySqlStorage, this, eventManager, itemBreakEvent), this);
        getServer().getPluginManager().registerEvents(new PlayersJoinEvent(mySqlStorage), this);
        getServer().getPluginManager().registerEvents(new BlockHitEvent(challengeManager, mySqlStorage, this, eventManager), this);
        getServer().getPluginManager().registerEvents(new LavaDamageEvent(mySqlStorage, challengeManager, this, eventManager), this);
        getServer().getPluginManager().registerEvents(itemBreakEvent, this);
        getServer().getPluginManager().registerEvents(new PotionDrinkEvent(mySqlStorage, eventManager, challengeManager), this);
        getServer().getPluginManager().registerEvents(new GuiService(), this);
        getServer().getPluginManager().registerEvents(new PotionDamageEvent(eventManager, this, mySqlStorage, challengeManager), this);
        getServer().getPluginManager().registerEvents(new ComboPotionDrinkEvent(mySqlStorage, eventManager,challengeManager), this);
        getServer().getPluginManager().registerEvents(new PotionDamageEntityEvent(mySqlStorage, eventManager, challengeManager), this);
    }

    @Override
    public void onDisable() {
        if (mySqlStorage != null) {
            mySqlStorage.shutdown();
        }
    }
}
