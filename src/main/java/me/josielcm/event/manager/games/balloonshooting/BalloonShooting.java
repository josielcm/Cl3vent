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
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import lombok.Getter;
import lombok.Setter;
import me.josielcm.event.Cl3vent;
import me.josielcm.event.api.items.ItemBuilder;

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
    private Location pos1;

    @Getter
    @Setter
    private Location pos2;

    @Getter
    @Setter
    private World world;

    @Getter
    @Setter
    private String title = "Balloon Shooting";

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

        regenerateBalloons();

        for (UUID playerId : eventPlayers) {
            Player p = Bukkit.getPlayer(playerId);
            if (p != null) {
                points.put(playerId, 0);
                p.teleport(spawn);
                p.setGameMode(org.bukkit.GameMode.ADVENTURE);
            } else {
                eventPlayers.remove(playerId);
            }

        }

        start();
    }

    public void start() {
        final AtomicInteger time = new AtomicInteger(60);

        giveItems();

        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            int currentTime = time.getAndDecrement();
            if (currentTime <= 0) {
                stop();
                return;
            }

            String message = "Time: " + currentTime;
            plugin.getEventManager().sendActionBar(message);

            if (currentTime % 30 == 0) {
                Bukkit.getScheduler().runTaskLater(plugin, this::regenerateBalloons, 1L);
            }

        }, 0L, 20L);

    }

    public void stop() {
        if (task != null) {
            task.cancel();
        }

        List<UUID> playersToEliminate = get10MenusPoints();
        
        Cl3vent.getInstance().getEventManager().sendActionBar("Juego terminado!");
        Cl3vent.getInstance().getEventManager().sendActionBar("Eliminando jugadores...");

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

        Cl3vent.getInstance().getEventManager().stop();
    }

    private void giveItems() {
        ItemStack bow = ItemBuilder.builder()
                .material(org.bukkit.Material.BOW)
                .displayName("Bow")
                .build();

        ItemStack arrow = ItemBuilder.builder()
                .material(org.bukkit.Material.ARROW)
                .displayName("Arrow")
                .amount(64)
                .build();

        eventPlayers.forEach(playerId -> {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                player.getInventory().clear();
                player.getInventory().setItem(0, bow);
                player.getInventory().setItem(8, arrow);
            }
        });
    }

    public List<UUID> get10MenusPoints() {
        return points.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e1.getValue(), e2.getValue()))
                .limit(10)
                .map(entry -> entry.getKey())
                .toList();
    }

    public void addPoint(UUID player) {
        points.merge(player, 1, Integer::sum);
    }

    private void regenerateBalloons() {
        List<BalloonArmorModel> balloonsCopy = new ArrayList<>(balloons);
        balloonsCopy.forEach(e -> {
            e.removeArmorStand();
            balloons.remove(e);
        });

        for (int i = 0; i < 20; i++) {
            BalloonArmorModel balloon = new BalloonArmorModel(pos1, pos2);
            balloon.buildArmorStand();
            balloons.add(balloon);
        }
    }

    public boolean isBalloon(ArmorStand armorStand) {
        return balloons.stream().anyMatch(balloon -> balloon.getArmorStand().equals(armorStand));
    }

    public void removeAllBalloons() {
        for (BalloonArmorModel balloon : balloons) {
            balloon.removeArmorStand();
        }
        balloons.clear();
    }

    public void removeBalloon(ArmorStand armorStand) {
        if (isBalloon(armorStand)) {
            BalloonArmorModel balloon = balloons.stream()
                    .filter(b -> b.getArmorStand().equals(armorStand))
                    .findFirst()
                    .orElse(null);

            if (balloon != null) {
                balloon.removeArmorStand();
                balloons.remove(balloon);
            }
        }
    }

}
