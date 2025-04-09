package me.josielcm.event.api;

import org.bukkit.plugin.java.JavaPlugin;

import lombok.Getter;
import me.josielcm.event.api.logs.Log;

public class API {

    @Getter
    private static API instance;

    @SuppressWarnings("unused")
    private JavaPlugin plugin;

    public API(JavaPlugin plugin) {
        this.plugin = plugin;
        instance = this;
    }
    
    public void onLoad() {
        Log.onLoad();
    }

    public void onEnable(boolean isEnabled, boolean isDebug) {
        if (isEnabled) {
            Key.instanceKeys();
        }
    }

}
