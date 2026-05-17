package dev.crysscoder.skilltree.manager;

import lombok.AllArgsConstructor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import dev.crysscoder.skilltree.data.Task;
import dev.crysscoder.skilltree.enums.Skill;
import dev.crysscoder.skilltree.enums.Status;
import dev.crysscoder.skilltree.storage.MySqlStorage;
import dev.crysscoder.skilltree.util.ItemMetaUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ItemManager {
    private final MySqlStorage mySqlStorage;
    private final ConfigManager configManager;
    private FileConfiguration config;
    private final JavaPlugin plugin;

    public ItemManager(MySqlStorage mySqlStorage, ConfigManager configManager, JavaPlugin plugin) {
        this.mySqlStorage = mySqlStorage;
        this.configManager = configManager;
        this.config = plugin.getConfig();
        this.plugin = plugin;
    }


    public CompletableFuture<ItemStack> parseHead(Player player) {
        final ConfigurationSection section = config.getConfigurationSection("head");

        if (section == null) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Section 'head' not found"));
        }

        return mySqlStorage.getPlayer(player.getName()).thenCompose(playerData -> {
            final Material material = Material.valueOf(section.getString("type").toUpperCase());
            final List<String> lore = section.getStringList("lore");
            final Skill skill = playerData.getSkill();

            final int max_health = 5;
            final int armor = 5;

            return mySqlStorage.getTasksByPlayer(playerData.getId()).thenApply(tasks -> {
                int num = 0;
                for (Task task : tasks) {
                    if (task.getStatus() == Status.COMPLETED) {
                        num++;
                    }
                }

                String display = section.getString("display");
                display = replacePlaceholders(display, player.getName(), skill, num, max_health, armor);

                List<String> finalLore = new ArrayList<>();
                for (String line : lore) {
                    finalLore.add(replacePlaceholders(line, player.getName(), skill, num, max_health, armor));
                }

                ItemStack head = new ItemStack(material);
                ItemMetaUtils.applyItemMeta(head, display, finalLore);
                return head;
            });
        });
    }

    private String replacePlaceholders(String text,
                                   String playerName,
                                   Skill skill,
                                   int num,
                                   int max_health,
                                   int armor) {
        return text
                .replace("{player_name}", playerName)
                .replace("{skill}", skill.name())
                .replace("{num}", String.valueOf(num))
                .replace("{max_health}", String.valueOf(max_health))
                .replace("{armor}", String.valueOf(armor));
    }
}
