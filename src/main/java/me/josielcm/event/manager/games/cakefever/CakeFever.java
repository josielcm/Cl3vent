package me.josielcm.event.manager.games.cakefever;

import java.util.ArrayList;
import java.util.List;
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
        CakeFeverEvent eventListener = new CakeFeverEvent();
        this.listener = eventListener;

        Cl3vent.getInstance().getServer().getPluginManager().registerEvents(listener, Cl3vent.getInstance());

        cakeLocationCache.clear();
        for (Location cake : cakes) {
            String key = cake.getBlockX() + ":" + cake.getBlockY() + ":" + cake.getBlockZ();
            cakeLocationCache.put(key, true);
        }

        points.clear();

        removeCakes();
        setCakesBlock();

        final Cl3vent plugin = Cl3vent.getInstance();
        final Set<UUID> eventPlayers = plugin.getEventManager().getPlayers();

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

        if (!validPlayers.isEmpty()) {
            final int BATCH_SIZE = 20;
            for (int i = 0; i < validPlayers.size(); i += BATCH_SIZE) {
                final int batchIndex = i;
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    int end = Math.min(batchIndex + BATCH_SIZE, validPlayers.size());
                    for (int j = batchIndex; j < end; j++) {
                        validPlayers.get(j).teleport(spawn);
                    }
                }, i / BATCH_SIZE);
            }
        }

        if (!invalidPlayers.isEmpty()) {
            eventPlayers.removeAll(invalidPlayers);
        }

        start();
    }

    private void start() {
        final Cl3vent plugin = Cl3vent.getInstance();
        final AtomicInteger time = new AtomicInteger(60);

        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            int currentTime = time.getAndDecrement();
            if (currentTime <= 0) {
                stop();
                return;
            }

            String message = "Time: " + currentTime;
            plugin.getEventManager().sendActionBar(message);

            if (currentTime % 30 == 0) {
                Bukkit.getScheduler().runTaskLater(plugin, this::regenerateCakes, 1L);
            }
        }, 0L, 20L);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
        }

        removeCakes();

        List<UUID> playersToEliminate = get20MenusPoints();

        Bukkit.getScheduler().runTask(Cl3vent.getInstance(), () -> {
            for (UUID player : playersToEliminate) {
                Player p = Bukkit.getPlayer(player);
                if (p != null) {
                    Cl3vent.getInstance().getEventManager().eliminatePlayer(player);
                }
            }
        });

        HandlerList.unregisterAll(listener);
        points.clear();
        cakeLocationCache.clear();

        Cl3vent.getInstance().getEventManager().stop();
    }

    private List<UUID> get20MenusPoints() {
        return points.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e1.getValue(), e2.getValue()))
                .limit(20)
                .map(entry -> entry.getKey())
                .toList();
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
                if (v == null)
                    return 0;
                int reducedPoints = RandomUtils.randomInt(1, 3);
                return Math.max(0, v - reducedPoints);
            });
            player.sendRichMessage("<red>-" + RandomUtils.randomInt(1, 3) + "</red>");
        }

        player.sendRichMessage("<gray>Points: " + points.get(playerId) + "</gray>");
    }

    public void regenerateCakes() {
        Bukkit.getScheduler().runTaskLater(Cl3vent.getInstance(), () -> {
            removeCakes();
            setCakesBlock();
        }, 1L);
    }

    private void setCakesBlock() {
        final int BATCH_SIZE = 50;
        for (int i = 0; i < cakes.size(); i += BATCH_SIZE) {
            final int startIdx = i;
            Bukkit.getScheduler().runTaskLater(Cl3vent.getInstance(), () -> {
                int endIdx = Math.min(startIdx + BATCH_SIZE, cakes.size());
                for (int j = startIdx; j < endIdx; j++) {
                    cakes.get(j).getBlock().setType(org.bukkit.Material.CAKE);
                }
            }, i / BATCH_SIZE);
        }
    }

    private void removeCakes() {
        final int BATCH_SIZE = 50;
        for (int i = 0; i < cakes.size(); i += BATCH_SIZE) {
            final int startIdx = i;
            Bukkit.getScheduler().runTaskLater(Cl3vent.getInstance(), () -> {
                int endIdx = Math.min(startIdx + BATCH_SIZE, cakes.size());
                for (int j = startIdx; j < endIdx; j++) {
                    cakes.get(j).getBlock().setType(org.bukkit.Material.AIR);
                }
            }, i / BATCH_SIZE);
        }
    }

    public boolean isCakeLocation(Location location) {
        String key = location.getBlockX() + ":" + location.getBlockY() + ":" + location.getBlockZ();
        return cakeLocationCache.containsKey(key);
    }
}
