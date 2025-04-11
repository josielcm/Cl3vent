package me.josielcm.event.manager.games.balloonparkour;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import me.josielcm.event.Cl3vent;
import me.josielcm.event.api.Key;
import me.josielcm.event.manager.games.GameType;

public class BalloonParkourEvents implements Listener {

    private final Set<UUID> playersInSafeZone = new HashSet<>();
    private final Map<String, Integer> checkpointCache = new HashMap<>();
    private final Map<UUID, Long> playerCooldowns = new HashMap<>();

    private static final long MOVE_CHECK_INTERVAL = 500; // ms

    private BalloonParkour getBalloonParkour() {
        return Cl3vent.getInstance().getEventManager().getBalloonParkour();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMove(PlayerMoveEvent ev) {
        if (!Cl3vent.getInstance().getEventManager().isInGame() ||
            Cl3vent.getInstance().getEventManager().getActualGame() != GameType.BALLOONPARKOUR)
            return;

        Player player = ev.getPlayer();
        UUID playerId = player.getUniqueId();

        BalloonParkour balloonParkour = getBalloonParkour();

        if (player.hasPermission("cl3vent.bypass") || player.getWorld() != balloonParkour.getWorld())
            return;

        // Verificar si realmente se movió de bloque
        if (!hasPlayerMoved(ev.getFrom(), ev.getTo()))
            return;

        // Aplicar cooldown para reducir verificaciones
        long now = System.currentTimeMillis();
        if (playerCooldowns.containsKey(playerId) && 
            now - playerCooldowns.get(playerId) < MOVE_CHECK_INTERVAL) {
            return;
        }
        playerCooldowns.put(playerId, now);

        // Verificar checkpoints y zona segura con menor frecuencia
        Location loc = player.getLocation();

        // Verificar solo si está cerca de un checkpoint
        if (isInCheckpointArea(loc)) {
            int checkpoint = getCheckpoint(loc);
            if (checkpoint != -1 && getPlayerCheckpoint(player) < checkpoint) {
                balloonParkour.reachCheckpoint(player, checkpoint);
            }
        }

        // Optimizar verificación de zona segura
        if (balloonParkour.getSafeContainer() != null) {
            boolean isInSafe = balloonParkour.getSafeContainer().isInside(loc);
            boolean wasInSafe = playersInSafeZone.contains(playerId);

            if (isInSafe && !wasInSafe) {
                playersInSafeZone.add(playerId);
                balloonParkour.getNoElimination().add(playerId);
                player.sendRichMessage("<green>Estas en la zona segura.");
            } else if (!isInSafe && wasInSafe) {
                playersInSafeZone.remove(playerId);
                player.sendRichMessage("<red>Has salido de la zona segura.");
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent ev) {
        Player player = ev.getPlayer();

        if (!Cl3vent.getInstance().getEventManager().isInGame()) return;
        if (Cl3vent.getInstance().getEventManager().getActualGame() != GameType.BALLOONPARKOUR) return;
        if (player.hasPermission("cl3vent.bypass")) return;
        if (player.getWorld() != getBalloonParkour().getWorld()) return;
        
        // Corregir la condición de acción
        if (ev.getAction() != Action.RIGHT_CLICK_BLOCK && ev.getAction() != Action.RIGHT_CLICK_AIR) return;
        if (ev.getItem() == null || !ev.getItem().hasItemMeta()) return;

        ItemStack item = ev.getItem();
        PersistentDataContainer data = item.getItemMeta().getPersistentDataContainer();
        if (!data.has(Key.getParkourItemsKey())) return;

        switch (data.get(Key.getParkourItemsKey(), PersistentDataType.STRING)) {
            case "checkpoint":
                int checkpoint = getPlayerCheckpoint(player);
                if (checkpoint == -1) {
                    player.teleport(getBalloonParkour().getSpawn());
                    return;
                }

                Location checkLocation = getCheckpointLocation(checkpoint);
                if (checkLocation == null) {
                    player.teleport(getBalloonParkour().getSpawn());
                    return;
                }

                player.teleport(checkLocation);
                player.sendRichMessage("<grey>Regresando al checkpoint <green>" + checkpoint + "<grey>.");

                break;
            case "toggle-visibility":
                boolean visible = getBalloonParkour().getVisibility()
                        .get(player.getUniqueId());

                getBalloonParkour().updatePlayerVisibility(player, !visible);
                break;
            case "impulse":
                if (getBalloonParkour().getPlayers()
                        .containsKey(player.getUniqueId())) {
                    if (!player.hasCooldown(Material.FEATHER)) {
                        player.setVelocity(player.getLocation().getDirection().multiply(2).setY(1));
                        player.setCooldown(Material.FEATHER, 20 * 60);
                        player.sendRichMessage("<green>Impulso activado.");
                    } else {
                        player.sendRichMessage("<red>Debes esperar para usar el impulso.");
                    }
                }
                break;
            default:
                break;
        }

    }

    private boolean hasPlayerMoved(Location from, Location to) {
        return from.getBlockX() != to.getBlockX() || from.getBlockY() != to.getBlockY()
                || from.getBlockZ() != to.getBlockZ();
    }

    // Optimizar la detección de checkpoints con cache 
    public int getCheckpoint(Location location) {
        try {
            String key = location.getBlockX() + ":" + location.getBlockY() + ":" + location.getBlockZ();
            
            // Verificar si ya tenemos el checkpoint en cache
            if (checkpointCache.containsKey(key)) {
                return checkpointCache.get(key);
            }
            
            // Buscar el checkpoint que tenga la menor distancia dentro del rango aceptable
            int closestCheckpoint = -1;
            double minDistance = Double.MAX_VALUE;
            
            for (Map.Entry<Integer, Location> entry : getBalloonParkour().getCheckpoints().entrySet()) {
                Location checkpoint = entry.getValue();
                if (Math.abs(checkpoint.getBlockX() - location.getBlockX()) <= 1 &&
                    Math.abs(checkpoint.getBlockY() - location.getBlockY()) <= 1 &&
                    Math.abs(checkpoint.getBlockZ() - location.getBlockZ()) <= 1) {
                        
                    double distance = Math.abs(checkpoint.getX() - location.getX()) +
                                     Math.abs(checkpoint.getY() - location.getY()) +
                                     Math.abs(checkpoint.getZ() - location.getZ());
                                     
                    if (distance < minDistance) {
                        minDistance = distance;
                        closestCheckpoint = entry.getKey();
                    }
                }
            }
            
            // Guardar en cache el resultado
            checkpointCache.put(key, closestCheckpoint);
            return closestCheckpoint;
        } catch (Exception e) {
            // Log error pero no interrumpir el juego
            Bukkit.getLogger().warning("Error al verificar checkpoint: " + e.getMessage());
            return -1;
        }
    }

    public void cleanupCache() {
        // Si el caché crece demasiado, limpiarlo
        if (checkpointCache.size() > 10000) {
            initCheckpointCache(); // Reiniciar con valores conocidos
        }
    }

    public Location getCheckpointLocation(int checkpoint) {
        return getBalloonParkour().getCheckpoints().get(checkpoint);
    }

    public int getPlayerCheckpoint(Player player) {
        if (getBalloonParkour().getPlayers()
                .containsKey(player.getUniqueId())) {
            return getBalloonParkour().getPlayers().get(player.getUniqueId());
        }
        return -1;
    }

    // Método auxiliar para pre-verificar si está en un área potencial de checkpoint
    private boolean isInCheckpointArea(Location location) {
        // Esta verificación es más rápida que iterar por todos los checkpoints
        for (Location checkpoint : getBalloonParkour().getCheckpoints().values()) {
            if (checkpoint.getWorld() == location.getWorld() &&
                Math.abs(checkpoint.getBlockX() - location.getBlockX()) <= 2 &&
                Math.abs(checkpoint.getBlockY() - location.getBlockY()) <= 2 &&
                Math.abs(checkpoint.getBlockZ() - location.getBlockZ()) <= 2) {
                return true;
            }
        }
        return false;
    }

    // Añade este método para inicializar el caché de checkpoints al inicio
    public void initCheckpointCache() {
        checkpointCache.clear();
        
        // Pre-calcular algunas ubicaciones que son probablemente checkpoints
        BalloonParkour balloonParkour = getBalloonParkour();
        for (Map.Entry<Integer, Location> entry : balloonParkour.getCheckpoints().entrySet()) {
            Location checkpoint = entry.getValue();
            
            // Almacenar el bloque exacto
            String exactKey = checkpoint.getBlockX() + ":" + checkpoint.getBlockY() + ":" + checkpoint.getBlockZ();
            checkpointCache.put(exactKey, entry.getKey());
            
            // También almacenar bloques adyacentes que suelen disparar la detección
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) continue; // Saltar el bloque exacto
                        
                        String nearbyKey = (checkpoint.getBlockX() + dx) + ":" + 
                                          (checkpoint.getBlockY() + dy) + ":" + 
                                          (checkpoint.getBlockZ() + dz);
                        // Solo almacenar si no existe ya una entrada para esta ubicación
                        if (!checkpointCache.containsKey(nearbyKey)) {
                            checkpointCache.put(nearbyKey, entry.getKey());
                        }
                    }
                }
            }
        }
    }

}
