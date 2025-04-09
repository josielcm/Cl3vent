package me.josielcm.event.api.logs;

import org.bukkit.Bukkit;

import me.josielcm.event.Cl3vent;

import java.util.Map;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Log {
    
    private static Cl3vent plugin = Cl3vent.getInstance();
    private static String pluginName = "NONE";
    
    public enum LogLevel {
        INFO("<aqua>"),
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

    public static void onLoad() {
        pluginName = plugin.getPluginMeta().getName();
        
        logHeader(LogLevel.INFO, "Loading " + pluginName + "...");
    }

    public static void onEnable(boolean loaded, Map<String, Boolean> options) {
        
        if (!loaded) {
            logPluginStatus(LogLevel.ERROR, "Error loading " + pluginName + "!", options);
            log(LogLevel.ERROR, "Please check the console for more information.");
        } else {
            logPluginStatus(LogLevel.INFO, pluginName + " loaded successfully!", options);
            log(LogLevel.INFO, pluginName + " is ready to use!");
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
        String separator = "-".repeat(50);
        
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
            String itemColor = detail.getValue() ? LogLevel.INFO.getColor() : LogLevel.ERROR.getColor();
            Bukkit.getConsoleSender().sendRichMessage(itemColor + " • " + detail.getKey() + ": " + status);
        }
    }
    
    /**
     * Agrega un pie de página estándar a los mensajes de log
     */
    public static void logFooter() {
        String separator = "─".repeat(50);
        
        Bukkit.getConsoleSender().sendRichMessage("<gray>" + separator);
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
            log(LogLevel.ERROR, "Stack Trace:");
            for (StackTraceElement element : e.getStackTrace()) {
                log(LogLevel.ERROR, "  at " + element.toString());
            }
        }
        
        Bukkit.getConsoleSender().sendRichMessage("<gray>Check logs for full stack trace.");
    }
}