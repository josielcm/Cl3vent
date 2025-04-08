package me.josielcm.jcm.api.files.configs;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import me.josielcm.jcm.api.logs.Log;

/**
 * Extended YAML configuration handler for Bukkit plugins.
 * <p>
 * This class extends YamlConfiguration to provide simplified file operations
 * for plugin configuration files. It handles creation, loading, saving,
 * and deletion of configuration files with appropriate error handling.
 * <p>
 * Features:
 * <ul>
 *   <li>Automatic file creation if not exists</li>
 *   <li>Support for nested folder structures</li>
 *   <li>Resource copying from plugin jar if available</li>
 *   <li>Simple reload and save operations</li>
 * </ul>
 * 
 * @author JosielCM
 * @since 1.0
 */
public class Config extends YamlConfiguration {

    /** The plugin instance that owns this config */
    protected final @NotNull JavaPlugin plugin;
    
    /** The filename of this config */
    protected final @NotNull String name;
    
    /** The file object representing this config */
    protected final @NotNull File file;
    
    /** The directory containing this config file */
    protected final @NotNull File folder;

    /**
     * Creates a new config file in the plugin's data folder.
     *
     * @param plugin The plugin that owns this config
     * @param name The filename of the config (including extension)
     */
    public Config(@NotNull JavaPlugin plugin, @NotNull String name) {
        this.plugin = plugin;
        this.name = name;
        this.folder = plugin.getDataFolder();
        this.file = new File(folder, name);
        this.createFile(false);
        this.reload();
    }

    /**
     * Creates a new config file in a subdirectory of the plugin's data folder.
     *
     * @param plugin The plugin that owns this config
     * @param name The filename of the config (including extension)
     * @param folder The subdirectory path relative to the plugin's data folder
     */
    public Config(@NotNull JavaPlugin plugin, @NotNull String name, @NotNull String folder) {
        this.plugin = plugin;
        this.name = name;
        this.folder = new File(plugin.getDataFolder(), folder);
        this.file = new File(this.folder, name);
        this.createFile(true);
        this.reload();
    }

    /**
     * Creates the config file if it doesn't exist.
     * <p>
     * If the file exists in the plugin's resources, it will be copied from there.
     * Otherwise, an empty file will be created.
     *
     * @param useFolder Whether the file should be in a subdirectory
     */
    public void createFile(boolean useFolder) {
        if (file.exists()) {
            return;
        }
        
        // Ensure plugin data folder exists
        plugin.getDataFolder().mkdirs();

        // Handle file in subfolder
        if (useFolder) {
            folder.mkdirs();
            String resourcePath = folder.getName() + "/" + name;
            
            if (plugin.getResource(resourcePath) != null) {
                plugin.saveResource(resourcePath, false);
            } else {
                createEmptyFile();
            }
            return;
        }
        
        // Handle file in root data folder
        if (plugin.getResource(name) != null) {
            plugin.saveResource(name, false);
        } else {
            createEmptyFile();
        }
    }

    /**
     * Creates an empty file for this configuration.
     */
    private void createEmptyFile() {
        try {
            file.createNewFile();
        } catch (IOException e) {
            Log.logError("Failed to create config file: " + file.getPath(), e);
        }
    }

    /**
     * Saves the configuration to disk.
     * 
     * @return true if the save was successful, false otherwise
     */
    public boolean saveData() {
        try {
            save(file);
            return true;
        } catch (IOException e) {
            Log.logError("Failed to save config file: " + file.getPath(), e);
            return false;
        }
    }

    /**
     * Reloads the configuration from disk.
     * 
     * @return true if the reload was successful, false otherwise
     */
    public boolean reload() {
        try {
            load(file);
            return true;
        } catch (Exception e) {
            Log.logError("Failed to load config file: " + file.getPath(), e);
            return false;
        }
    }

    /**
     * Deletes the configuration file from disk.
     * 
     * @return true if deletion was successful, false otherwise
     */
    public boolean delete() {
        if (!file.exists()) {
            return true;
        }
        
        if (file.delete()) {
            return true;
        } else {
            Log.logError("Failed to delete file: " + file.getPath(), null);
            return false;
        }
    }
    
    /**
     * Gets the File object representing this config.
     * 
     * @return the file object
     */
    public @NotNull File getFile() {
        return file;
    }
    
    /**
     * Gets the name of this config file.
     * 
     * @return the filename
     */
    public @NotNull String getFileName() {
        return name;
    }
    
    /**
     * Gets the directory containing this config file.
     * 
     * @return the folder
     */
    public @NotNull File getFolder() {
        return folder;
    }
    
    /**
     * Checks if this config file exists on disk.
     * 
     * @return true if the file exists, false otherwise
     */
    public boolean exists() {
        return file.exists();
    }
}