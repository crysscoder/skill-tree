package dev.crysscoder.skilltree.command;

import dev.crysscoder.skilltree.enums.Skill;
import dev.crysscoder.skilltree.inv.AlchemistMenu;
import dev.crysscoder.skilltree.inv.ChoiceMenu;
import dev.crysscoder.skilltree.inv.FarmerMenu;
import dev.crysscoder.skilltree.inv.WarriorMenu;
import dev.crysscoder.skilltree.storage.MySqlStorage;
import lombok.AllArgsConstructor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
public class AdminCommand implements CommandExecutor, TabCompleter {
    private final JavaPlugin plugin;
    private final MySqlStorage mySqlStorage;
    private final ChoiceMenu choiceMenu;
    private final WarriorMenu warriorMenu;
    private final FarmerMenu farmerMenu;
    private final AlchemistMenu alchemistMenu;

    private final String[] subcommands = {"info", "addtask", "start"};

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("Use /skilltree <info|addtask|start>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "info" -> showInfoPlayer(player);
            case "addtask" -> handleAddTask(player, args);
            case "start" -> menuCommandExecutor(player);
            default -> player.sendMessage("Use /skilltree <info|addtask|start>");
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            String current = args[0].toLowerCase();
            return Arrays.stream(subcommands)
                    .filter(value -> value.startsWith(current))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    public void showInfoPlayer(Player player) {
        player.sendMessage("SkillTree menu: /skilltree start");
    }

    private void handleAddTask(Player player, String[] args) {
        if (args.length != 3) {
            player.sendMessage("Use /skilltree addtask <player> <task>");
            return;
        }

        int taskNumber;
        try {
            taskNumber = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            player.sendMessage("Task must be a number.");
            return;
        }

        if (taskNumber < 1 || taskNumber > 9) {
            player.sendMessage("Task number must be from 1 to 9.");
        }
    }

    private void menuCommandExecutor(Player player) {
        mySqlStorage.getPlayer(player.getName()).thenAccept(playerData -> {
            if (playerData != null && playerData.getSkill() != null) {
                player.getScheduler().run(plugin, task -> {
                    Skill skill = playerData.getSkill();
                    switch (skill) {
                        case WARRIOR -> warriorMenu.openInventory(player);
                        case FARMER -> farmerMenu.openInventory(player);
                        case ALCHEMIST -> alchemistMenu.openInventory(player);
                        case SOME_DEFAULT -> choiceMenu.openInventory(player);
                    }
                }, null);
                return;
            }

            player.getScheduler().run(plugin, task -> {
                player.openInventory(choiceMenu.getInventory());
                player.sendMessage("Choose your class.");
            }, null);
        });
    }
}
