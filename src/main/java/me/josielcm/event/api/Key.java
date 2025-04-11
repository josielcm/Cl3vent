package me.josielcm.event.api;

import org.bukkit.NamespacedKey;

import lombok.Getter;
import me.josielcm.event.Cl3vent;

public class Key {
    
    @Getter
    private static NamespacedKey selectorItemsKey = null;

    @Getter
    private static NamespacedKey parkourItemsKey = null;

    public static void instanceKeys() {
        selectorItemsKey = new NamespacedKey(Cl3vent.getInstance(), "selectorItemsKey");
        parkourItemsKey = new NamespacedKey(Cl3vent.getInstance(), "parkour");
    }

}
