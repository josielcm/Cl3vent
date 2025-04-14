package me.josielcm.event.manager.games.balloonshooting;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;

import lombok.Getter;
import lombok.Setter;
import me.josielcm.event.Cl3vent;

public class BalloonShooting {

    @Getter
    @Setter
    private ConcurrentHashMap<UUID, Integer> points = new ConcurrentHashMap<>();

    @Getter
    @Setter
    private List<BalloonArmorModel> balloons = new ArrayList<>();

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
    private BukkitTask task;

    @Getter
    @Setter
    private Listener listener;

    @Getter
    private final Cl3vent plugin = Cl3vent.getInstance();

    @Getter
    final Set<UUID> eventPlayers = plugin.getEventManager().getPlayers();

    public void prepare() {
        points.clear();

        listener = new BalloonShootingEvents();

        Cl3vent.getInstance().getServer().getPluginManager().registerEvents(listener, Cl3vent.getInstance());

        balloons.forEach(e -> {
            e.removeArmorStand();
            e.buildArmorStand();
        });

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

    public void start() {
        final AtomicInteger time = new AtomicInteger(60);

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
                Bukkit.getScheduler().runTaskLater(plugin, this::regenerateBalloons, 1L);
            }
            
            // Every 15 seconds, run garbage collection hint
            if (currentTime % 15 == 0) {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> System.gc());
            }
        }, 0L, 20L);

    }

    public void stop() {
        // asd
    }

    private void regenerateBalloons() {
        balloons.forEach(e -> {
            e.removeArmorStand();
            e.buildArmorStand();
        });
    }

}
