package me.josielcm.event;

import lombok.Getter;
import me.josielcm.event.api.files.configs.Config;

public class FileManager {

    final static Cl3vent plugin = Cl3vent.getInstance();

    @Getter
    static Config messages = null;

    @Getter
    static Config settings = null;

    public static void loadFiles() {
        settings = new Config(plugin, "settings.yml");
        messages = new Config(plugin, "messages.yml");
    }

    public static void saveFiles() {
        settings.saveData();
        messages.saveData();
    }

    public static void reload() {
        settings = new Config(plugin, "settings.yml");
        messages = new Config(plugin, "messages.yml");
    }

    public static void debug() {
        plugin.setDebug(settings.getBoolean("debug", false));
    }
    
}