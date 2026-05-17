package dev.crysscoder.skilltree.event;

import lombok.AllArgsConstructor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionType;
import dev.crysscoder.skilltree.data.Task;
import dev.crysscoder.skilltree.enums.Status;
import dev.crysscoder.skilltree.manager.ChallengeManager;
import dev.crysscoder.skilltree.manager.ConfigManager;
import dev.crysscoder.skilltree.manager.EventManager;
import dev.crysscoder.skilltree.storage.MySqlStorage;

import java.util.List;


@AllArgsConstructor
public class PotionDamageEntityEvent implements Listener {
    private final MySqlStorage mySqlStorage;
    private final EventManager eventManager;
    private final ChallengeManager challengeManager;

    @EventHandler
    public void onEntityPotionDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof ThrownPotion potion)) return;
        if (!(potion.getShooter() instanceof Player player)) return;




        if (!(event.getEntity() instanceof LivingEntity target)) return;
        if (target instanceof Player) return;

            mySqlStorage.getPlayer(player.getName()).thenAccept(playerData -> {
                mySqlStorage.getTasksByPlayer(playerData.getId()).thenAccept(tasks -> {
                    for (Task task : tasks) {

                        if (!(eventManager.isApplicableTask(task, "brew-throw-harming"))) continue;
                        ConfigManager.Challenge challenge = challengeManager.getChallengeById(task.getChallengeId());
                        if (challenge == null) continue;

                        final Object potionObj = challenge.getSettings().get("potionEffect");
                        if (!(potionObj instanceof List<?> targetPotion)) continue;

                        for (Object typePotion : targetPotion) {
                            final boolean harmful = potion.getEffects().stream()
                                    .anyMatch(effect -> effect.getType().getName().equalsIgnoreCase(typePotion.toString()));

                            if (!harmful) continue;

                            if (harmful) {
                                eventManager.handleProgress(task, challenge, player);
                                if (task.getStatus() == Status.COMPLETED) {
                                    challengeManager.setNextChallenge(challenge, task);
                                }
                            }
                        }

                    }
                });
            });
    }

}
