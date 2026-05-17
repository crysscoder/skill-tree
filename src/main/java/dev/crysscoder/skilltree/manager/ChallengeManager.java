package dev.crysscoder.skilltree.manager;


import org.bukkit.Bukkit;
import dev.crysscoder.skilltree.data.Task;
import dev.crysscoder.skilltree.enums.Status;
import dev.crysscoder.skilltree.storage.MySqlStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ChallengeManager {

    private final ConfigManager configManager;
    private final MySqlStorage mySqlStorage;

    public ChallengeManager(ConfigManager configManager, MySqlStorage mySqlStorage) {
        this.configManager = configManager;
        this.mySqlStorage = mySqlStorage;
    }


    public List<ConfigManager.Challenge> getAllChallenges() {
        return configManager.getChallenges();
    }


    public ConfigManager.Challenge getChallengeById(String id) {
        return configManager.getById(id);
    }

    public ConfigManager.Challenge getFirstChallengeForClass(String classPrefix) {
        for (ConfigManager.Challenge challenge : getAllChallenges()) {
            if (challenge.getId().startsWith(classPrefix)) {
                return challenge;
            }
        }
        return null;
    }


    public List<String> getAllId() {
        List<String> ids = new ArrayList<>();
        List<ConfigManager.Challenge> challenges = configManager.getChallenges();

        if (challenges == null || challenges.isEmpty()) {
            return ids;
        }

        for (ConfigManager.Challenge challenge : challenges) {
            if (challenge.getId() != null && !challenge.getId().isEmpty()) {
                ids.add(challenge.getId());
            }
        }

        return ids;
    }

    public void setNextChallenge(ConfigManager.Challenge challenge, Task task) {
        final String nextId = challenge.getNextChallengeId();

        if (nextId != null) {
            final ConfigManager.Challenge next = getChallengeById(nextId);
            if (next != null) {
                Task nextTask = new Task(
                        0,
                        task.getPlayerId(),
                        next.getDisplayName(),
                        nextId,
                        Status.IN_PROGRESS,
                        0
                );
                mySqlStorage.addTask(nextTask);
            }
        }
    }
}
