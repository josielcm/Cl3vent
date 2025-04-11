package me.josielcm.event;

import org.bukkit.plugin.java.JavaPlugin;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.PaperCommandManager;

import lombok.Getter;
import lombok.Setter;
import me.josielcm.event.api.Key;
import me.josielcm.event.api.logs.Log;
import me.josielcm.event.commands.EventCommand;
import me.josielcm.event.commands.subscommands.Reload;
import me.josielcm.event.manager.EventManager;
import me.josielcm.event.manager.events.PlayerEvents;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public final class Cl3vent extends JavaPlugin {

    @Getter
    private static Cl3vent instance;

    private PaperCommandManager commandManager;

    @Getter
    @Setter
    private boolean debug = false;

    @Getter
    @Setter
    private EventManager eventManager;

    @Override
    public void onLoad() {
        instance = this;
        Log.onLoad();
    }

    @Override
    public void onEnable() {
        Key.instanceKeys();
        FileManager.loadFiles();
        FileManager.debug();

        eventManager = new EventManager();

        setupCommands();
        registerEvents();

        HashMap<String, Boolean> options = new HashMap<>();
        options.put("Debug", isDebug());
        options.put("Events", true);
        options.put("Commands", true);

        Log.onEnable(
                isEnabled(), options);
    }

    @Override
    public void onDisable() {
        if (commandManager != null) {
            commandManager.unregisterCommands();
        }

        FileManager.saveFiles();
        Log.onDisable();
    }

    public void reload() {
        FileManager.reload();
        FileManager.debug();
        if (commandManager != null) {
            commandManager.unregisterCommands();
        }
        setupCommands();
        Log.onReload();
    }

    private void setupCommands() {
        commandManager = new PaperCommandManager(this);

        // Register all commands at once
        List<BaseCommand> commands = Arrays.asList(
                new EventCommand(),
                new Reload());

        for (BaseCommand command : commands) {
            commandManager.registerCommand(command);
        }
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new PlayerEvents(), this);
    }

}