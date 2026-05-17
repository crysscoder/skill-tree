package dev.crysscoder.skilltree.util;

import org.bukkit.Material;
import dev.crysscoder.skilltree.manager.ConfigManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MaterialUtils {

    public static List<Material> getMaterialList(ConfigManager.Challenge challenge, String key) {
        Object raw = challenge.getSettings().get(key);
        if (!(raw instanceof List<?> list)) return null;

        List<Material> result = new ArrayList<>();
        for (Object obj : list) {
            if (obj instanceof String name) {
                try {
                    result.add(Material.valueOf(name.toUpperCase()));
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        return result.isEmpty() ? Collections.emptyList() : result;
    }

}

