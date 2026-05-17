package dev.crysscoder.skilltree.manager;

import lombok.AllArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import dev.crysscoder.skilltree.data.Task;
import dev.crysscoder.skilltree.enums.Skill;
import dev.crysscoder.skilltree.enums.Status;
import dev.crysscoder.skilltree.storage.MySqlStorage;
import dev.crysscoder.skilltree.util.ItemMetaUtils;

import java.util.*;

@AllArgsConstructor
public class PanelManager {
    private final ConfigManager configManager;
    private final MySqlStorage mySqlStorage;
    private final ChallengeManager challengeManager;

    private final int[] SLOTS_PANEL = {36, 37, 38, 39, 40, 41, 42, 43, 44};
    public static final int[] SLOTS_GRAY_PANEL = {
            27, 28, 29, 30, 31, 32, 33, 34, 35,
            45, 46, 47, 48, 49, 50, 51, 52, 53,
    };

    private final Map<Skill, List<String>> skillTasks = new HashMap<>();

    public void setPanels(Player player, Skill skill, Inventory inventory) {

        mySqlStorage.getPlayer(player.getName()).thenAccept(playerData -> {
            if (playerData == null) {
                fillEmptyPanels(inventory);
                return;
            }

            List<String> challengeIds = skillTasks.get(skill);
            if (challengeIds == null || challengeIds.isEmpty()) {
                fillEmptyPanels(inventory);
                return;
            }

            mySqlStorage.getTasksByPlayer(playerData.getId()).thenAccept(playerTasks -> {
                for (int i = 0; i < SLOTS_PANEL.length; i++) {
                    int slotNumber = SLOTS_PANEL[i];

                    if (i >= challengeIds.size()) {
                        inventory.setItem(slotNumber, createEmptyPanel());
                        continue;
                    }

                    String challengeId = challengeIds.get(i);
                    ConfigManager.Challenge challenge = configManager.getById(challengeId);

                    if (challenge == null) {
                        inventory.setItem(slotNumber, createEmptyPanel());
                        continue;
                    }

                    Task playerTask = playerTasks.stream()
                            .filter(t -> t.getTaskName().equalsIgnoreCase(challenge.getDisplayName()))
                            .findFirst()
                            .orElse(null);

                    Status status = (playerTask != null) ? playerTask.getStatus() : Status.NOT_STARTED;
                    ItemStack panel = createChallengePanel(challenge, status);

                    inventory.setItem(slotNumber, panel);
                }
            }).exceptionally(e -> {
                fillEmptyPanels(inventory);
                return null;
            });
        }).exceptionally(e -> {
            fillEmptyPanels(inventory);
            return null;
        });
    }

    private ItemStack createChallengePanel(ConfigManager.Challenge challenge, Status status) {
        Material material;
        String statusText;

        switch (status) {
            case COMPLETED -> {
                material = Material.LIME_STAINED_GLASS_PANE;
                statusText = "§aВыполнено";
            }
            case IN_PROGRESS -> {
                material = Material.YELLOW_STAINED_GLASS_PANE;
                statusText = "§eВ процессе";
            }
            default -> {
                material = Material.RED_STAINED_GLASS_PANE;
                statusText = "§cНе начато";
            }
        }

        ItemStack item = new ItemStack(material);
        List<String> lore = new ArrayList<>(challenge.getGoal());
        lore.add(" ");
        lore.add(statusText);
        lore.add("§7Требуется: §f" + challenge.getData().getRequired());
        lore.add("§7ID: §8" + challenge.getId());

        ItemMetaUtils.applyItemMeta(item, "§e" + challenge.getDisplayName(), lore);
        return item;
    }

    private void fillEmptyPanels(Inventory inventory) {
        for (int slotNumber : SLOTS_PANEL) {
            inventory.setItem(slotNumber, createEmptyPanel());
        }
    }

    private ItemStack createEmptyPanel() {
        ItemStack emptyPanel = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMetaUtils.applyItemMeta(emptyPanel, "§7Пусто", List.of("§8Нет задания"));
        return emptyPanel;
    }

    public void fillPanelSlots(Inventory inventory, ItemStack panel) {
        if (inventory == null || panel == null) return;
        for (int slot : SLOTS_GRAY_PANEL) {
            inventory.setItem(slot, panel);
        }
    }

    public void initializeSkillTasks() {
        List<String> allIds = challengeManager.getAllId();

        if (allIds == null || allIds.isEmpty()) {
            return;
        }

        for (String id : allIds) {
            if (getStartNameTask(id, "warrior")) addTaskToSkill(Skill.WARRIOR, id);
            if (getStartNameTask(id, "alchemist")) addTaskToSkill(Skill.ALCHEMIST, id);
            if (getStartNameTask(id, "farmer")) addTaskToSkill(Skill.FARMER, id);
        }
    }

    private boolean getStartNameTask(String id, String prefix) {
        return id.toLowerCase().startsWith(prefix.toLowerCase());
    }

    private void addTaskToSkill(Skill skill, String id) {
        skillTasks.computeIfAbsent(skill, k -> new ArrayList<>()).add(id);
    }
}
