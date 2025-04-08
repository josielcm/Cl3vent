package me.josielcm.jcm;

import org.bukkit.plugin.java.JavaPlugin;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.PaperCommandManager;

import lombok.Getter;
import lombok.Setter;
import me.josielcm.jcm.api.Key;
import me.josielcm.jcm.api.logs.Log;
import me.josielcm.jcm.commands.DickMain;
import me.josielcm.jcm.commands.subscommands.Reload;

import java.util.Arrays;
import java.util.List;

public final class Base extends JavaPlugin {

    @Getter
    private static Base instance;

    private PaperCommandManager commandManager;

    @Getter
    @Setter
    private boolean debug = false;

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

        setupCommands();

        Log.onEnable(
                isEnabled(), // Plugin enabled status
                false, // Events loaded
                true, // Commands loaded
                false, // Other features
                true, // Dependencies required
                false // Development mode
        );
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
                new DickMain(),
                new Reload());

        for (BaseCommand command : commands) {
            commandManager.registerCommand(command);
        }
    }
}