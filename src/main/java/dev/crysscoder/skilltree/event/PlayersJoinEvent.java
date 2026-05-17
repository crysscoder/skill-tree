package dev.crysscoder.skilltree.event;

import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import dev.crysscoder.skilltree.data.PlayerData;
import dev.crysscoder.skilltree.enums.Skill;
import dev.crysscoder.skilltree.storage.MySqlStorage;


@AllArgsConstructor
public class PlayersJoinEvent implements Listener {
    private final MySqlStorage mySqlStorage;

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        mySqlStorage.getPlayer(player.getName()).thenAccept(playerData -> {
            if (playerData == null) {
                PlayerData newPlayer = new PlayerData(player.getName(), Skill.SOME_DEFAULT, 0);
                mySqlStorage.addPlayer(newPlayer);
            }
        });
    }
}
