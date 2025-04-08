package me.josielcm.jcm.api.logs;

import org.bukkit.Bukkit;
import java.util.Map;
import java.util.LinkedHashMap;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import me.josielcm.jcm.Base;

public class Log {
    
    private static Base plugin = Base.getInstance();
    private static String pluginName = "NONE";
    
    public enum LogLevel {
        INFO("<green>"),
        WARNING("<yellow>"),
        ERROR("<red>"),
        SUCCESS("<green>"),
        DEBUG("<aqua>");
        
        private final String color;
        
        LogLevel(String color) {
            this.color = color;
        }
        
        public String getColor() {
            return color;
        }
    }

    @SuppressWarnings("deprecation")
    public static void onLoad() {
        pluginName = plugin.getDescription().getName();
        
        logHeader(LogLevel.INFO, "Loading " + pluginName + "...");
    }

    public static void onEnable(boolean loaded, boolean events, boolean commands, boolean other, 
                               boolean requireDependencies, boolean requireDev) {
        
        Map<String, Boolean> statusDetails = new LinkedHashMap<>();
        statusDetails.put("Events", events);
        statusDetails.put("Commands", commands);
        statusDetails.put("Other features", other);
        statusDetails.put("Required dependencies", requireDependencies);
        statusDetails.put("Require support", requireDev);
        
        if (!loaded) {
            logPluginStatus(LogLevel.ERROR, "Error loading " + pluginName + "!", statusDetails);
            log(LogLevel.ERROR, "Please check the console for more information.");
        } else {
            logPluginStatus(LogLevel.SUCCESS, pluginName + " loaded successfully!", statusDetails);
            log(LogLevel.SUCCESS, pluginName + " is ready to use!");
            log(LogLevel.SUCCESS, "Enjoy!");
        }
        
        logFooter();
    }

    public static void onReload() {
        logHeader(LogLevel.INFO, "Reloading " + pluginName + "...");
        logFooter();
    }

    public static void onDisable() {
        logHeader(LogLevel.INFO, pluginName + " is shutting down...");
        logFooter();
    }
    
    /**
     * Envía un mensaje de log con el nivel especificado
     */
    public static void log(LogLevel level, String message) {
        Bukkit.getConsoleSender().sendRichMessage(level.getColor() + message);
    }
    
    /**
     * Crea un encabezado para mensajes importantes
     */
    public static void logHeader(LogLevel level, String title) {
        String separator = "═".repeat(50);
        
        Bukkit.getConsoleSender().sendRichMessage("");
        Bukkit.getConsoleSender().sendRichMessage(level.getColor() + separator);
        Bukkit.getConsoleSender().sendRichMessage(level.getColor() + "  " + title);
        Bukkit.getConsoleSender().sendRichMessage(level.getColor() + separator);
    }
    
    /**
     * Muestra los detalles de estado del plugin
     */
    public static void logPluginStatus(LogLevel level, String title, Map<String, Boolean> statusDetails) {
        logHeader(level, title);
        
        // Mostrar cada elemento del estado
        for (Map.Entry<String, Boolean> detail : statusDetails.entrySet()) {
            String status = detail.getValue() ? "✓" : "✗";
            String itemColor = detail.getValue() ? LogLevel.SUCCESS.getColor() : LogLevel.ERROR.getColor();
            Bukkit.getConsoleSender().sendRichMessage(itemColor + " • " + detail.getKey() + ": " + status);
        }
    }
    
    /**
     * Agrega un pie de página estándar a los mensajes de log
     */
    public static void logFooter() {
        String separator = "─".repeat(50);
        
        Bukkit.getConsoleSender().sendRichMessage("<gray>" + separator);
        Bukkit.getConsoleSender().sendRichMessage("<green>Made with <red>❤ <green>by JosielCM");
        Bukkit.getConsoleSender().sendRichMessage("<aqua>https://github.com/josielcm");
        Bukkit.getConsoleSender().sendRichMessage("");
    }
    
    /**
     * Registra errores con información de timestamp
     */
    public static void logError(String errorMessage, Exception e) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String timestamp = LocalDateTime.now().format(formatter);
        
        logHeader(LogLevel.ERROR, "Error Detected");
        log(LogLevel.ERROR, "Time: " + timestamp);
        log(LogLevel.ERROR, "Message: " + errorMessage);
        
        if (e != null) {
            log(LogLevel.ERROR, "Exception: " + e.getClass().getName());
            log(LogLevel.ERROR, "Cause: " + e.getMessage());
        }
        
        Bukkit.getConsoleSender().sendRichMessage("<gray>Check logs for full stack trace.");
    }
}