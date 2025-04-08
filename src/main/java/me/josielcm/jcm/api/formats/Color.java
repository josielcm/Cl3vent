package me.josielcm.jcm.api.formats;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import org.jetbrains.annotations.NotNull;
import java.util.List;
import java.util.Map;

/**
 * Utility class for color formatting and text manipulation in Minecraft.
 * Provides methods to parse and convert between different text formats,
 * handle placeholders, and manipulate colors.
 * <p>
 * Supports:
 * <ul>
 *   <li>MiniMessage format parsing</li>
 *   <li>Placeholder replacement</li>
 *   <li>RGB hex color handling</li>
 *   <li>Legacy color code conversion</li>
 *   <li>Gradient text generation</li>
 * </ul>
 * 
 * @author JosielCM
 * @since 1.0
 */
public class Color {
    /** The MiniMessage instance for parsing and serializing text */
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    
    /**
     * Private constructor to prevent instantiation of this utility class
     */
    private Color() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Parses a string with MiniMessage format and replaces placeholders with components.
     * 
     * @param message The message to parse
     * @param placeholders Map of placeholder strings to their component replacements
     * @return The parsed component with placeholders replaced
     */
    public static Component parse(@NotNull String message, @NotNull Map<String, Component> placeholders) {
        // First parse the message with MiniMessage
        Component finalMessage = MINI_MESSAGE.deserialize(message);

        // Replace each placeholder with its component
        for (Map.Entry<String, Component> entry : placeholders.entrySet()) {
            finalMessage = finalMessage.replaceText(TextReplacementConfig.builder()
                    .matchLiteral(entry.getKey())
                    .replacement(entry.getValue())
                    .build());
        }

        return finalMessage;
    }

    /**
     * Parses a string with MiniMessage format.
     * 
     * @param message The message to parse
     * @return The parsed component
     */
    public static Component parse(@NotNull String message) {
        return MINI_MESSAGE.deserialize(message);
    }

    /**
     * Serializes a component to MiniMessage format.
     * 
     * @param message The component to serialize
     * @return The serialized string in MiniMessage format
     */
    public static @NotNull String parse(@NotNull Component message) {
        return MINI_MESSAGE.serialize(message);
    }
    
    /**
     * Parses a string with MiniMessage format and replaces string placeholders.
     * 
     * @param message The message to parse
     * @param placeholders Map of placeholder strings to their string replacements
     * @return The parsed component with placeholders replaced
     */
    public static Component parseWithStringPlaceholders(@NotNull String message, @NotNull Map<String, String> placeholders) {
        String processed = message;
        
        // Replace each placeholder with its string value before parsing
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            processed = processed.replace(entry.getKey(), entry.getValue());
        }
        
        return MINI_MESSAGE.deserialize(processed);
    }
    
    /**
     * Converts legacy color codes (§) to MiniMessage format.
     * 
     * @param legacyText The text with legacy color codes
     * @return Text in MiniMessage format
     */
    public static String legacyToMiniMessage(@NotNull String legacyText) {
        Component component = LegacyComponentSerializer.legacySection().deserialize(legacyText);
        return MINI_MESSAGE.serialize(component);
    }
    
    /**
     * Converts MiniMessage format to legacy color codes (§).
     * 
     * @param miniMessage The text in MiniMessage format
     * @return Text with legacy color codes
     */
    public static String miniMessageToLegacy(@NotNull String miniMessage) {
        Component component = MINI_MESSAGE.deserialize(miniMessage);
        return LegacyComponentSerializer.legacySection().serialize(component);
    }
    
    /**
     * Applies a gradient between two colors to a text.
     * 
     * @param text The text to apply gradient to
     * @param startColor The starting color in hex format (#RRGGBB)
     * @param endColor The ending color in hex format (#RRGGBB)
     * @return Component with gradient applied
     */
    public static Component gradient(@NotNull String text, @NotNull String startColor, @NotNull String endColor) {
        if (text.isEmpty()) {
            return Component.empty();
        }
        
        // Remove # if present
        startColor = startColor.startsWith("#") ? startColor.substring(1) : startColor;
        endColor = endColor.startsWith("#") ? endColor.substring(1) : endColor;
        
        // Parse hex colors
        java.awt.Color start = hexToRgb(startColor);
        java.awt.Color end = hexToRgb(endColor);
        
        if (text.length() == 1) {
            return Component.text(text).color(TextColor.color(start.getRed(), start.getGreen(), start.getBlue()));
        }
        
        // Build gradient string in MiniMessage format
        StringBuilder gradientText = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            float ratio = (float) i / (text.length() - 1);
            java.awt.Color currentColor = interpolateColor(start, end, ratio);
            gradientText.append("<#").append(rgbToHex(currentColor)).append(">").append(text.charAt(i));
        }
        
        return MINI_MESSAGE.deserialize(gradientText.toString());
    }
    
    /**
     * Applies a rainbow effect to text.
     * 
     * @param text The text to apply rainbow to
     * @param saturation The color saturation (0-1)
     * @param brightness The color brightness (0-1)
     * @return Component with rainbow colors applied
     */
    public static Component rainbow(@NotNull String text, float saturation, float brightness) {
        if (text.isEmpty()) {
            return Component.empty();
        }
        
        // Build rainbow string in MiniMessage format
        StringBuilder rainbowText = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            float hue = (float) i / text.length();
            java.awt.Color color = java.awt.Color.getHSBColor(hue, saturation, brightness);
            rainbowText.append("<#").append(rgbToHex(color)).append(">").append(text.charAt(i));
        }
        
        return MINI_MESSAGE.deserialize(rainbowText.toString());
    }
    
    /**
     * Simpler version of rainbow with default saturation and brightness.
     * 
     * @param text The text to apply rainbow to
     * @return Component with rainbow colors applied
     */
    public static Component rainbow(@NotNull String text) {
        return rainbow(text, 0.9f, 1.0f);
    }
    
    /**
     * Creates a component list from strings, parsing each with MiniMessage.
     * 
     * @param strings List of strings to parse
     * @return List of parsed components
     */
    public static List<Component> parseList(@NotNull List<String> strings) {
        return strings.stream()
                .map(Color::parse)
                .toList();
    }
    
    /**
     * Strips all formatting codes and tags from text.
     * 
     * @param text The text to strip formatting from
     * @return Plain text without formatting
     */
    public static String stripFormatting(@NotNull String text) {
        // First convert MiniMessage to Component then extract plain text
        Component component = MINI_MESSAGE.deserialize(text);
        return PlainTextComponentSerializer.plainText().serialize(component);
    }
    
    /**
     * Creates a centered text component that tries to center text in chat.
     * 
     * @param text The text to center
     * @param width The width to center within (default chat width is ~320 pixels)
     * @return Component with centered text
     */
    public static Component center(@NotNull String text, int width) {
        // Strip formatting to get true length
        String stripped = stripFormatting(text);
        int length = stripped.length();
        
        // Average character width in pixels (approximation)
        int charWidth = 6;
        int textWidth = length * charWidth;
        
        // Calculate spaces needed for centering
        int spaces = Math.max(0, (width - textWidth) / (2 * charWidth));
        String padding = " ".repeat(spaces);
        
        return parse(padding + text);
    }
    
    /**
     * Creates a centered text with default chat width.
     * 
     * @param text The text to center
     * @return Component with centered text
     */
    public static Component center(@NotNull String text) {
        return center(text, 320);
    }
    
    /**
     * Helper method to interpolate between two colors.
     */
    private static java.awt.Color interpolateColor(java.awt.Color color1, java.awt.Color color2, float ratio) {
        int red = (int) (color1.getRed() * (1 - ratio) + color2.getRed() * ratio);
        int green = (int) (color1.getGreen() * (1 - ratio) + color2.getGreen() * ratio);
        int blue = (int) (color1.getBlue() * (1 - ratio) + color2.getBlue() * ratio);
        return new java.awt.Color(red, green, blue);
    }
    
    /**
     * Helper method to convert hex string to Color.
     */
    private static java.awt.Color hexToRgb(String hexColor) {
        try {
            return new java.awt.Color(
                Integer.parseInt(hexColor.substring(0, 2), 16),
                Integer.parseInt(hexColor.substring(2, 4), 16),
                Integer.parseInt(hexColor.substring(4, 6), 16)
            );
        } catch (Exception e) {
            // Default to white on error
            return java.awt.Color.WHITE;
        }
    }
    
    /**
     * Helper method to convert Color to hex string.
     */
    private static String rgbToHex(java.awt.Color color) {
        return String.format("%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }
    
    /**
     * Simple serializer for getting plain text from components.
     */
    private static class PlainTextComponentSerializer {
        public static PlainTextComponentSerializer plainText() {
            return new PlainTextComponentSerializer();
        }
        
        public String serialize(Component component) {
            StringBuilder builder = new StringBuilder();
            serialize(component, builder);
            return builder.toString();
        }
        
        private void serialize(Component component, StringBuilder builder) {
            if (component instanceof TextComponent textComponent) {
                builder.append(textComponent.content());
            }
            
            for (Component child : component.children()) {
                serialize(child, builder);
            }
        }
    }
}