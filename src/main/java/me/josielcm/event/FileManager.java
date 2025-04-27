package me.josielcm.event;

import lombok.Getter;
import me.josielcm.event.api.files.configs.Config;

public class FileManager {

    final static Cl3vent plugin = Cl3vent.getInstance();

    @Getter
    static Config messages = null;

    @Getter
    static Config settings = null;

    @Getter
    static Config cakefever = null;

    @Getter
    static Config balloonparkour = null;

    @Getter
    static Config balloonshooting = null;

    public static void loadFiles() {
        settings = new Config(plugin, "settings.yml");
        messages = new Config(plugin, "messages.yml");
        cakefever = new Config(plugin, "CakeFever.yml", "games");
        balloonparkour = new Config(plugin, "BalloonParkour.yml", "games");
        balloonshooting = new Config(plugin, "BalloonShooting.yml", "games");
    }

    public static void saveFiles() {
        settings.saveData();
        messages.saveData();
        cakefever.saveData();
        balloonparkour.saveData();
    }

    public static void reload() {
        settings = new Config(plugin, "settings.yml");
        messages = new Config(plugin, "messages.yml");
        cakefever = new Config(plugin, "CakeFever.yml", "games");
        balloonparkour = new Config(plugin, "BalloonParkour.yml", "games");
        balloonshooting = new Config(plugin, "BalloonShooting.yml", "games");
    }

    public static void debug() {
        plugin.setDebug(settings.getBoolean("debug", false));
    }
    
}