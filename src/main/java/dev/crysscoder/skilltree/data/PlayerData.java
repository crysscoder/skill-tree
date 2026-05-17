package dev.crysscoder.skilltree.data;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bukkit.attribute.Attribute;
import dev.crysscoder.skilltree.enums.Skill;

@Getter
@Setter
@AllArgsConstructor
public class PlayerData {
    private int id;
    private final String playerName;
    private Skill skill;
    private int progress;


    public PlayerData(String playerName, Skill skill, int progress) {
        this.playerName = playerName;
        this.skill = skill;
        this.progress = progress;

    }

}
