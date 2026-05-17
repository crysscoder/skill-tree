package dev.crysscoder.skilltree.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;

public class TextUtil {
    public static String textMessage(String text){
        Component.text(text)
                .decoration(TextDecoration.ITALIC, false)
                .decoration(TextDecoration.BOLD);

        return text;
    }
}
