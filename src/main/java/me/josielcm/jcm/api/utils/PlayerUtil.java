package me.josielcm.jcm.api.utils;

import java.time.Duration;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import me.josielcm.jcm.api.formats.Color;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.Title.Times;

public final class PlayerUtil {
    
    // Prevent instantiation of utility class
    private PlayerUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Send a message to multiple players identified by their UUIDs
     */
    public static void sendMessage(Collection<UUID> players, String message) {
        if (message == null || message.isEmpty()) return;
        
        Component parsed = Color.parse(message);
        players.forEach(uuid -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                player.sendMessage(parsed);
            }
        });
    }
    
    /**
     * Send a message to a single player
     */
    public static void sendMessage(Player player, String message) {
        if (player == null || !player.isOnline() || message == null || message.isEmpty()) return;
        player.sendMessage(Color.parse(message));
    }

    /**
     * Send a title to a player
     */
    public static void sendTitle(Player player, String title, String subtitle) {
        if (player == null || !player.isOnline()) return;
        
        final Title t = Title.title(
            title != null ? Color.parse(title) : Component.empty(),
            subtitle != null ? Color.parse(subtitle) : Component.empty()
        );
        player.showTitle(t);
    }

    /**
     * Send a title to a player with timing options
     */
    public static void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        if (player == null || !player.isOnline()) return;
        
        Times times = Times.times(
            Duration.ofMillis(fadeIn * 50), 
            Duration.ofMillis(stay * 50), 
            Duration.ofMillis(fadeOut * 50)
        );
        
        final Title t = Title.title(
            title != null ? Color.parse(title) : Component.empty(),
            subtitle != null ? Color.parse(subtitle) : Component.empty(),
            times
        );
        
        player.showTitle(t);
    }

    /**
     * Send an action bar message to multiple players
     */
    public static void sendActionBar(Collection<UUID> players, String message) {
        if (message == null || message.isEmpty()) return;
        
        Component parsed = Color.parse(message);
        players.forEach(uuid -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                player.sendActionBar(parsed);
            }
        });
    }
    
    /**
     * Send an action bar message to a single player
     */
    public static void sendActionBar(Player player, String message) {
        if (player == null || !player.isOnline() || message == null) return;
        player.sendActionBar(Color.parse(message));
    }

    /**
     * Check if a location is inside a region defined by two corners
     */
    public static boolean isInside(Location loc, Location pos1, Location pos2) {
        if (loc == null || pos1 == null || pos2 == null) return false;
        if (!loc.getWorld().equals(pos1.getWorld()) || !pos1.getWorld().equals(pos2.getWorld())) return false;
        
        // Create a bounding box from the two positions and check if location is within
        BoundingBox box = BoundingBox.of(pos1, pos2);
        return box.contains(loc.toVector());
    }

    /**
     * Apply a velocity impulse to a player
     */
    public static void impulse(Player player, boolean back, double power, double height, double forward) {
        if (player == null || !player.isOnline()) return;
        
        Vector direction = player.getLocation().getDirection();
        Vector impulse = new Vector();
        
        if (back) {
            impulse.setX(-direction.getX() * power);
            impulse.setZ(-direction.getZ() * power);
        } else {
            impulse.setX(direction.getX() * power);
            impulse.setZ(direction.getZ() * power);
        }
        
        impulse.setY(height);
        player.setVelocity(impulse.multiply(forward));
    }
    
    /**
     * Apply a velocity in a specific direction
     */
    public static void applyVelocity(Player player, double x, double y, double z) {
        if (player == null || !player.isOnline()) return;
        player.setVelocity(new Vector(x, y, z));
    }
    
    /**
     * Play a sound for a player
     */
    public static void playSound(Player player, Sound sound, float volume, float pitch) {
        if (player == null || !player.isOnline() || sound == null) return;
        player.playSound(player.getLocation(), sound, volume, pitch);
    }
    
    /**
     * Play a sound for multiple players
     */
    public static void playSound(Collection<UUID> players, Sound sound, float volume, float pitch) {
        if (sound == null) return;
        
        players.forEach(uuid -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                player.playSound(player.getLocation(), sound, volume, pitch);
            }
        });
    }
    
    /**
     * Apply a potion effect to a player
     */
    public static void applyEffect(Player player, PotionEffectType type, int duration, int amplifier) {
        if (player == null || !player.isOnline() || type == null) return;
        player.addPotionEffect(new PotionEffect(type, duration, amplifier));
    }
    
    /**
     * Get all online players within a radius of a location
     */
    public static Set<Player> getPlayersInRadius(Location center, double radius) {
        if (center == null) return new HashSet<>();
        
        World world = center.getWorld();
        double radiusSquared = radius * radius;
        
        return Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.getWorld().equals(world))
                .filter(p -> p.getLocation().distanceSquared(center) <= radiusSquared)
                .collect(Collectors.toSet());
    }
    
    /**
     * Reset a player's state (clear inventory, effects, etc.)
     */
    @SuppressWarnings("deprecation")
    public static void resetPlayerState(Player player) {
        if (player == null || !player.isOnline()) return;
        
        player.getInventory().clear();
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setSaturation(20);
        player.setFireTicks(0);
        player.setFallDistance(0);
        player.setExp(0);
        player.setLevel(0);
        
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
    }
    
    /**
     * Execute a task for each online player
     */
    public static void forEachPlayer(Consumer<Player> action) {
        if (action == null) return;
        Bukkit.getOnlinePlayers().forEach(action);
    }
    
    /**
     * Give items to a player safely
     */
    public static void giveItems(Player player, ItemStack... items) {
        if (player == null || !player.isOnline() || items == null) return;
        
        for (ItemStack item : items) {
            if (item == null) continue;
            
            // Try to add to inventory, drop on ground if full
            if (player.getInventory().firstEmpty() == -1) {
                player.getWorld().dropItemNaturally(player.getLocation(), item);
            } else {
                player.getInventory().addItem(item);
            }
        }
    }
    
    /**
     * Check if a player is in creative or spectator mode
     */
    public static boolean isInCreativeMode(Player player) {
        if (player == null || !player.isOnline()) return false;
        GameMode mode = player.getGameMode();
        return mode == GameMode.CREATIVE || mode == GameMode.SPECTATOR;
    }
}