package dev.crysscoder.skilltree.event;

import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import dev.crysscoder.skilltree.data.Task;
import dev.crysscoder.skilltree.enums.Status;
import dev.crysscoder.skilltree.manager.ChallengeManager;
import dev.crysscoder.skilltree.manager.ConfigManager;
import dev.crysscoder.skilltree.manager.EventManager;
import dev.crysscoder.skilltree.storage.MySqlStorage;

import java.lang.reflect.Method;


@AllArgsConstructor
public class BlockHitEvent implements Listener {
    private final ChallengeManager challengeManager;
    private final MySqlStorage mySqlStorage;
    private final JavaPlugin plugin;
    private final EventManager eventManager;

    @EventHandler
    public void onBlockHitEvent(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Arrow arrow)) return;
        if (!(arrow.getShooter() instanceof Player player)) return;

        final Block hitBlock = event.getHitBlock();

        if (hitBlock.getType() != Material.TARGET) return;
        mySqlStorage.getPlayer(player.getName()).thenAccept(playerData -> {
            if (playerData == null) return;

            mySqlStorage.getTasksByPlayer(playerData.getId()).thenAccept(tasks -> {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    for (Task task : tasks) {
                        if (!eventManager.isApplicableTask(task, "bow-hit")) continue;

                        final ConfigManager.Challenge challenge = challengeManager.getChallengeById(task.getChallengeId());
                        if (challenge == null) continue;

                        final int distance = getDistance(challenge);
                        final double playerDistance = player.getEyeLocation().distance(
                                hitBlock.getLocation().add(0.5, 0.5, 0.5)
                        );

                        if (playerDistance >= distance) {
                            eventManager.handleProgress(task, challenge, player);
                            if (task.getStatus() == Status.COMPLETED) {
                                challengeManager.setNextChallenge(challenge, task);
                                player.sendMessage("§aТы попал!");
                            }
                        } else {
                            player.sendMessage("§cНе попал!");
                        }
                    }
                });
            });

        });
    }


    public int getDistance(ConfigManager.Challenge challenge) {
        Object value = challenge.getSettings().get("distance");
        if (value instanceof Number num) {
            return num.intValue();
        }
        if (value instanceof String str) {
            try {
                return Integer.parseInt(str);
            } catch (NumberFormatException ignored) {
            }
        }
        Bukkit.getLogger().warning("§cНеверное значение distance в челлендже " + challenge.getId());
        return 0;
    }


}
