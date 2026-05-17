package dev.crysscoder.skilltree.event;

import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.plugin.java.JavaPlugin;
import dev.crysscoder.skilltree.data.Task;
import dev.crysscoder.skilltree.enums.Status;
import dev.crysscoder.skilltree.manager.ChallengeManager;
import dev.crysscoder.skilltree.manager.ConfigManager;
import dev.crysscoder.skilltree.manager.EventManager;
import dev.crysscoder.skilltree.storage.MySqlStorage;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
public class PotionDamageEvent implements Listener {
    private final EventManager eventManager;
    private final JavaPlugin plugin;
    private final  MySqlStorage mySqlStorage;
    private final ChallengeManager challengeManager;

    private final Set<Player> potionPlayer = new HashSet<>();

    @EventHandler
    public void onEntityPotionDamage(EntityPotionEffectEvent event) {
         if (!(event.getEntity() instanceof Player player)) return;
        if (event.getNewEffect() == null) return;

        String effectType = event.getNewEffect().getType().getName().toUpperCase();
        if (!effectType.equals("POISON")) return;

        potionPlayer.add(player);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!potionPlayer.contains(player)) return;
            if (player.isDead()) return;

            potionPlayer.remove(player);
            updateChallengeProgress(player, "survive", "condition", "POISON");
        },20L * 5);

    }

    private void updateChallengeProgress(Player player, String typeConfig, String type, String typeSettings) {
         mySqlStorage.getPlayer(player.getName()).thenAccept(playerData -> {
             mySqlStorage.getTasksByPlayer(playerData.getId()).thenAccept(tasks -> {
                 for (Task task : tasks) {
                     if (!(eventManager.isApplicableTask(task, typeConfig))) continue;

                     final ConfigManager.Challenge challenge = challengeManager.getChallengeById(task.getChallengeId());
                     if (challenge == null) continue;
                     final Object condition = challenge.getSettings().get(type);
                     boolean matches = false;

                     if (condition instanceof List<?> list) {
                         matches = list.stream().anyMatch(o ->
                                 o != null && o.toString().equalsIgnoreCase(typeSettings));
                     } else if (condition instanceof String str) {
                         matches = str.equalsIgnoreCase(typeSettings);
                     }

                     if (!matches) continue;

                     eventManager.handleProgress(task, challenge, player);
                     if (task.getStatus() == Status.COMPLETED) {
                         challengeManager.setNextChallenge(challenge, task);
                     }

                 }
             });
         });
     }
}
