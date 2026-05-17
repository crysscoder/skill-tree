package dev.crysscoder.skilltree.manager;

import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;
import dev.crysscoder.skilltree.data.Task;
import dev.crysscoder.skilltree.enums.Status;
import dev.crysscoder.skilltree.storage.MySqlStorage;

import java.util.List;

@AllArgsConstructor
public class EventManager {
    private final ChallengeManager challengeManager;
    private final MySqlStorage mySqlStorage;

    public boolean isApplicableTask(Task task, String typeConfig) {
        if (task.getStatus() == Status.COMPLETED) return false;
        ConfigManager.Challenge challenge = challengeManager.getChallengeById(task.getChallengeId());
        if (challenge == null) return false;

        return typeConfig.equalsIgnoreCase(challenge.getType());
    }

    public void handleProgress(Task task, ConfigManager.Challenge challenge, Player player) {
        int required = challenge.getData().getRequired();
        int newProgress = Math.min(task.getProgress() + 1, required);

        task.setProgress(newProgress);
        if (newProgress >= required) {
            task.setStatus(Status.COMPLETED);
            player.sendMessage("Задание выполнено! Чтобы посмотреть следующее задание используй /skilltree start");
        }

        mySqlStorage.updateTask(task);
    }




}
