package dev.crysscoder.skilltree.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import dev.crysscoder.skilltree.enums.Status;
import dev.crysscoder.skilltree.manager.ConfigManager;


@Getter
@Setter
@AllArgsConstructor
public class Task {
    private int id;
    private int playerId;
    private String taskName;
    private String challengeId;
    private Status status;
    private int progress;
}
