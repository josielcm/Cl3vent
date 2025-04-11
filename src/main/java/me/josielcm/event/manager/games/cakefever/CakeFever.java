package me.josielcm.event.manager.games.cakefever;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;

import lombok.Getter;
import lombok.Setter;
import me.josielcm.event.Cl3vent;
import me.josielcm.event.api.utils.RandomUtils;

public class CakeFever {

    @Getter
    @Setter
    private List<Location> cakes = new ArrayList<>();

    @Getter
    @Setter
    private ConcurrentHashMap<UUID, Integer> points = new ConcurrentHashMap<>();

    @Getter
    @Setter
    private Location spawn;

    @Getter
    @Setter
    private World world;

    @Getter
    @Setter
    private String title = "Cake Fever";

    @Getter
    @Setter
    private int limitElimination = 0;

    @Getter
    @Setter
    private BukkitTask task;

    @Getter
    @Setter
    private Listener listener;

    private final ConcurrentHashMap<String, Boolean> cakeLocationCache = new ConcurrentHashMap<>();

    public void prepare() {
        // Crear el listener
        CakeFeverEvent eventListener = new CakeFeverEvent();
        this.listener = eventListener;
        
        // Registrar eventos
        Cl3vent.getInstance().getServer().getPluginManager().registerEvents(listener, Cl3vent.getInstance());

        // Limpiar y repoblar la caché
        cakeLocationCache.clear();
        for (Location cake : cakes) {
            String key = cake.getBlockX() + ":" + cake.getBlockY() + ":" + cake.getBlockZ();
            cakeLocationCache.put(key, true);
        }

        // Limpiar puntos anteriores
        points.clear();
        
        // Remover y colocar pasteles
        removeCakes();
        setCakesBlock();

        // Cache plugin instance to reduce method calls
        final Cl3vent plugin = Cl3vent.getInstance();
        final Set<UUID> eventPlayers = plugin.getEventManager().getPlayers();
        
        // Process players in batches
        List<Player> validPlayers = new ArrayList<>();
        List<UUID> invalidPlayers = new ArrayList<>();
        
        for (UUID playerId : eventPlayers) {
            Player p = Bukkit.getPlayer(playerId);
            if (p != null) {
                points.put(playerId, 0);
                validPlayers.add(p);
            } else {
                invalidPlayers.add(playerId);
            }
        }
        
        // Teleport players in batches with a slight delay to spread load
        if (!validPlayers.isEmpty()) {
            final int BATCH_SIZE = 20;
            for (int i = 0; i < validPlayers.size(); i += BATCH_SIZE) {
                final int batchIndex = i;
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    int end = Math.min(batchIndex + BATCH_SIZE, validPlayers.size());
                    for (int j = batchIndex; j < end; j++) {
                        validPlayers.get(j).teleport(spawn);
                    }
                }, i/BATCH_SIZE);
            }
        }
        
        // Clean up invalid players
        if (!invalidPlayers.isEmpty()) {
            eventPlayers.removeAll(invalidPlayers);
        }

        start();
    }

    private void start() {
        // More efficient timer implementation
        final Cl3vent plugin = Cl3vent.getInstance();
        final AtomicInteger time = new AtomicInteger(300);
        
        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            int currentTime = time.getAndDecrement();
            if (currentTime <= 0) {
                stop();
                return;
            }
            
            // Pre-construct action bar message once per tick
            String message = "Time: " + currentTime;
            plugin.getEventManager().sendActionBar(message);
            
            // Regenerate cakes every 30 seconds
            if (currentTime % 30 == 0) {
                Bukkit.getScheduler().runTaskLater(plugin, this::regenerateCakes, 1L);
            }
            
            // Every 15 seconds, run garbage collection hint
            if (currentTime % 15 == 0) {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> System.gc());
            }
        }, 0L, 20L);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
        }
        
        removeCakes();
        
        // Eliminar jugadores en un hilo separado para no bloquear el servidor
        Bukkit.getScheduler().runTaskAsynchronously(Cl3vent.getInstance(), () -> {
            List<UUID> playersToEliminate = get20MenusPoints();
            
            // Volver al hilo principal para eliminar jugadores
            Bukkit.getScheduler().runTask(Cl3vent.getInstance(), () -> {
                for (UUID player : playersToEliminate) {
                    Player p = Bukkit.getPlayer(player);
                    if (p != null) {
                        Cl3vent.getInstance().getEventManager().eliminatePlayer(player);
                    }
                }
            });
        });

        HandlerList.unregisterAll(listener);
        points.clear();
        cakeLocationCache.clear();
    }

    private List<UUID> get20MenusPoints() {
        List<UUID> players = new ArrayList<>();

        // Usar CopyOnWriteArrayList para evitar excepciones de concurrencia
        List<Map.Entry<UUID, Integer>> sortedEntries = new ArrayList<>(points.entrySet());
        
        // Ordenar por puntos (menor a mayor)
        sortedEntries.sort(Map.Entry.comparingByValue());

        int limit = Math.min(20, sortedEntries.size());
        for (int i = 0; i < limit; i++) {
            players.add(sortedEntries.get(i).getKey());
        }

        return players;
    }

    public void randomPoint(Player player) {
        boolean isCake = RandomUtils.randomBool();
        UUID playerId = player.getUniqueId();
        
        // Usar computeIfPresent para operaciones atómicas
        if (isCake) {
            points.compute(playerId, (k, v) -> v == null ? 1 : v + 1);
            player.sendRichMessage("<aqua>+1</aqua>");
        } else {
            points.compute(playerId, (k, v) -> {
                if (v == null) return 0;
                int reducedPoints = RandomUtils.randomInt(1, 3);
                return Math.max(0, v - reducedPoints);
            });
            player.sendRichMessage("<red>-" + RandomUtils.randomInt(1, 3) + "</red>");
        }
    }

    public void regenerateCakes() {
        // Ejecutar en el hilo principal con un pequeño retraso
        Bukkit.getScheduler().runTaskLater(Cl3vent.getInstance(), () -> {
            removeCakes();
            setCakesBlock();
        }, 1L);
    }

    private void setCakesBlock() {
        // Procesar en lotes para no sobrecargar el servidor
        final int BATCH_SIZE = 50;
        for (int i = 0; i < cakes.size(); i += BATCH_SIZE) {
            final int startIdx = i;
            Bukkit.getScheduler().runTaskLater(Cl3vent.getInstance(), () -> {
                int endIdx = Math.min(startIdx + BATCH_SIZE, cakes.size());
                for (int j = startIdx; j < endIdx; j++) {
                    cakes.get(j).getBlock().setType(org.bukkit.Material.CAKE);
                }
            }, i/BATCH_SIZE);
        }
    }

    private void removeCakes() {
        // Procesar en lotes para no sobrecargar el servidor
        final int BATCH_SIZE = 50;
        for (int i = 0; i < cakes.size(); i += BATCH_SIZE) {
            final int startIdx = i;
            Bukkit.getScheduler().runTaskLater(Cl3vent.getInstance(), () -> {
                int endIdx = Math.min(startIdx + BATCH_SIZE, cakes.size());
                for (int j = startIdx; j < endIdx; j++) {
                    cakes.get(j).getBlock().setType(org.bukkit.Material.AIR);
                }
            }, i/BATCH_SIZE);
        }
    }

    // Método para verificar si una ubicación es un cake válido
    public boolean isCakeLocation(Location location) {
        String key = location.getBlockX() + ":" + location.getBlockY() + ":" + location.getBlockZ();
        return cakeLocationCache.containsKey(key);
    }
}
