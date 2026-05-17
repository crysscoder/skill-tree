package dev.crysscoder.skilltree.command;

import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import dev.crysscoder.skilltree.enums.Skill;
import dev.crysscoder.skilltree.inv.AlchemistMenu;
import dev.crysscoder.skilltree.inv.ChoiceMenu;
import dev.crysscoder.skilltree.inv.FarmerMenu;
import dev.crysscoder.skilltree.inv.WarriorMenu;
import dev.crysscoder.skilltree.storage.MySqlStorage;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;


@AllArgsConstructor
public class AdminCommand implements CommandExecutor, TabCompleter {
    private final JavaPlugin plugin;
    private final MySqlStorage mySqlStorage;
    private final ChoiceMenu choiceMenu;
    private final WarriorMenu warriorMenu;
    private final FarmerMenu farmerMenu;
    private final AlchemistMenu alchemistMenu;

    private final String[] SUBCOMMAND = {"info", "addtask", "start"};

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player p = (Player) sender;

        if (args.length == 0) {
            choiceMenu.getInventory();
            p.sendMessage("§cИспользуйте: /skilltree <info|addtask>");
            return true;
        }

        String subcommand = args[0];
        switch (subcommand) {
            case "info":
                showInfoPlayer(p);
                break;
            case "addtask":
                if (args.length != 3) {
                    p.sendMessage("§cИспользуйте: /skilltree addtask <игрок> <номер задания>");
                    return true;
                }
                String targetName = args[1];
                int taskNumber = Integer.parseInt(args[2]);
                if (taskNumber > 9) {
                    p.sendMessage("§cНомер задания не может быть больше 9.");
                    return true;
                }
            case "start":
                menuCommandExecutor(p);
                return true;

        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if(args.length == 1){
           String current = args[0].toLowerCase();

           return Arrays.stream(SUBCOMMAND)
                   .filter(k -> k.startsWith(current))
                   .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    public void showInfoPlayer(Player player) {

    }

    private void menuCommandExecutor(Player p) {
        mySqlStorage.getPlayer(p.getName()).thenAccept(playerData -> {
            if (playerData != null && playerData.getSkill() != null) {
                p.getScheduler().run(plugin, task -> {
                    Skill skill = playerData.getSkill();
                    switch (skill) {
                        case WARRIOR -> warriorMenu.openInventory(p);
                        case FARMER -> farmerMenu.openInventory(p);
                        case ALCHEMIST -> alchemistMenu.openInventory(p);
                        case SOME_DEFAULT -> choiceMenu.openInventory(p);
                        default -> p.sendMessage("Некорректно выбран класс.");
                    }
                }, null);
            } else {
                p.getScheduler().run(plugin, task -> {
                    p.openInventory(choiceMenu.getInventory());
                    p.sendMessage("§aВыберите свой класс!");
                }, null);
            }
        });
    }
}
