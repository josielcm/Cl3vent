package me.josielcm.jcm.api;

import org.bukkit.NamespacedKey;

import lombok.Getter;
import me.josielcm.jcm.Base;

public class Key {
    
    @Getter
    private static NamespacedKey selectorItemsKey = null;

    public static void instanceKeys() {
        selectorItemsKey = new NamespacedKey(Base.getInstance(), "selectorItemsKey");
    }

}
