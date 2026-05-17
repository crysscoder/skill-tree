package dev.crysscoder.skilltree.event;

import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;
import dev.crysscoder.skilltree.data.Task;
import dev.crysscoder.skilltree.enums.Status;
import dev.crysscoder.skilltree.manager.ChallengeManager;
import dev.crysscoder.skilltree.manager.ConfigManager;
import dev.crysscoder.skilltree.manager.EventManager;
import dev.crysscoder.skilltree.storage.MySqlStorage;
import dev.crysscoder.skilltree.util.MaterialUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@AllArgsConstructor
public class ItemBreakEvent implements Listener {

    private final ChallengeManager challengeManager;
    private final MySqlStorage mySqlStorage;
    private final JavaPlugin plugin;
    private final EventManager eventManager;

    private final Map<Player, Material> lastBreakBlock = new ConcurrentHashMap<>();

    public void addLastBrokenBlock(Player player, Material blockType) {
        lastBreakBlock.put(player, blockType);
    }

    @EventHandler(priority =  EventPriority.LOW)
    public void onPlayerItemBreak(PlayerItemBreakEvent event) {
        final Player player = event.getPlayer();
        final Material item = event.getBrokenItem().getType();

        final Material lastBlock = lastBreakBlock.get(player);
        if (lastBlock == null) player.sendMessage("блок null");

         lastBreakBlock.remove(player);

        mySqlStorage.getPlayer(player.getName()).thenAccept(playerData -> {
            if (playerData == null) return;

            mySqlStorage.getTasksByPlayer(playerData.getId()).thenAccept(tasks -> {
                for (Task task : tasks) {
                    if (!eventManager.isApplicableTask(task, "break-sword-on-wood")) continue;

                    final ConfigManager.Challenge challenge = challengeManager.getChallengeById(task.getChallengeId());
                    if (challenge == null) continue;

                    final List<Material> materialBlocks = MaterialUtils.getMaterialList(challenge, "block");
                    final List<Material> materialItems = MaterialUtils.getMaterialList(challenge, "item");

                    if (materialBlocks == null || materialItems == null) continue;

                    if (materialBlocks.contains(lastBlock) && materialItems.contains(item)) {
                        eventManager.handleProgress(task, challenge, player);

                        if (task.getStatus() == Status.COMPLETED) {
                            challengeManager.setNextChallenge(challenge, task);
                        }

                        Bukkit.getScheduler().runTask(plugin, () ->
                            player.sendMessage("§aТы сломал меч об дерево. Задание выполнено!")
                        );
                    }
                }
            });
        });
    }
}
