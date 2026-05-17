package dev.crysscoder.skilltree.event;

import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;
import dev.crysscoder.skilltree.data.Task;
import dev.crysscoder.skilltree.enums.Status;
import dev.crysscoder.skilltree.manager.ChallengeManager;
import dev.crysscoder.skilltree.manager.ConfigManager;
import dev.crysscoder.skilltree.manager.EventManager;
import dev.crysscoder.skilltree.storage.MySqlStorage;
import dev.crysscoder.skilltree.util.MaterialUtils;
import dev.crysscoder.skilltree.util.YLevelUtils;

import java.util.*;

@AllArgsConstructor
public class BlocksBreakEvent implements Listener {
    private final ChallengeManager challengeManager;
    private final MySqlStorage mySqlStorage;
    private final JavaPlugin plugin;
    private final EventManager eventManager;
    private final ItemBreakEvent itemBreakEvent;


    @EventHandler (priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        final Block block = event.getBlock();
        final Material brokenType = event.getBlock().getType();
        final Player player = event.getPlayer();
        player.sendMessage("Сломанный блок: " + brokenType);

        if (isWood(brokenType)) {
            itemBreakEvent.addLastBrokenBlock(player, brokenType);
        }

        mySqlStorage.getPlayer(player.getName()).thenAccept(playerData -> {
            if (playerData == null) return;

            mySqlStorage.getTasksByPlayer(playerData.getId()).thenAccept(tasks -> {

                for (Task task : tasks) {

                    if (!eventManager.isApplicableTask(task, "find-item")) continue;;

                    final ConfigManager.Challenge challenge = challengeManager.getChallengeById(task.getChallengeId());
                    if (challenge == null) continue;


                    List<Material> validBlocks = MaterialUtils.getMaterialList(challenge, "block");
                    if (validBlocks == null || !validBlocks.contains(brokenType)) continue;

                    boolean yValid = YLevelUtils.isValidYLevel(block, challenge);
                    if (!yValid)  continue;

                    Bukkit.getScheduler().runTask(plugin, () -> {
                        eventManager.handleProgress(task, challenge, player);

                        if(task.getStatus() == Status.COMPLETED){
                           challengeManager.setNextChallenge(challenge, task);
                        }
                    });
                }
            });
        });
    }

     private boolean isWood(Material mat) {
        return switch (mat) {
            case OAK_LOG, SPRUCE_LOG, BIRCH_LOG, JUNGLE_LOG, DARK_OAK_LOG, ACACIA_LOG, CHERRY_LOG -> true;
            default -> false;
        };
    }

}