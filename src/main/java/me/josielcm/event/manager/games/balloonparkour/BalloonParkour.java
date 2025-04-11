package me.josielcm.event.manager.games.balloonparkour;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import lombok.Getter;
import lombok.Setter;
import me.josielcm.event.Cl3vent;
import me.josielcm.event.api.Key;
import me.josielcm.event.api.items.ItemBuilder;
import me.josielcm.event.api.regions.Container;

public class BalloonParkour {
    
    @Getter
    @Setter
    private HashMap<Integer, Location> checkpoints = new HashMap<>();

    @Getter
    @Setter
    private HashMap<UUID, Integer> players = new HashMap<>();

    @Getter
    @Setter
    private Location spawn;

    @Getter
    @Setter
    private World world;

    @Getter
    @Setter
    Container safeContainer;

    @Getter
    @Setter
    private String title = "Balloon Parkour";

    @Getter
    @Setter
    private Set<UUID> noElimination = new HashSet<>();

    @Getter
    @Setter
    private HashMap<UUID, Boolean> visibility = new HashMap<>();

    @Getter
    @Setter
    private BukkitTask task;

    @Getter
    @Setter
    private Listener listener;

    public void prepare() {
        listener = new BalloonParkourEvents();
        Bukkit.getPluginManager().registerEvents(listener, Cl3vent.getInstance());

        // Cl3vent.getInstance().getEventManager().showTitle(title, "", 1, 3, 1);

        players.clear();
        noElimination.clear();
        visibility.clear();

        Cl3vent.getInstance().getEventManager().getPlayers().forEach(player -> {
            Player p = Bukkit.getPlayer(player);

            players.put(player, -1);
            visibility.put(player, true);


            if (p != null) {
                p.teleport(spawn);
            } else {
                Cl3vent.getInstance().getEventManager().getPlayers().remove(player);
            }
        });

        start();
    }

    public void start() {
        giveItems();

        task = Bukkit.getScheduler().runTaskTimer(Cl3vent.getInstance(), new Runnable() {
            int time = 30;

            @Override
            public void run() {
                if (time <= 0) {
                    stop();
                    return;
                }

                Cl3vent.getInstance().getEventManager().sendActionBar("Time: " + time);
                time--;
            }
        }, 0L, 20L);

    }

    public void stop() {
        if (task != null) {
            task.cancel();
        }

        Cl3vent.getInstance().getEventManager().getPlayers().forEach(player -> {
            Player p = Bukkit.getPlayer(player);

            if (p != null) {
                if (!p.hasPermission("cl3vent.bypass")) {
                    p.getInventory().clear();
                    p.teleport(spawn);
                }
            } else {
                Cl3vent.getInstance().getEventManager().getPlayers().remove(player);
            }
        });

        eliminatePlayers();

        HandlerList.unregisterAll(listener);

        players.clear();
        noElimination.clear();
        visibility.clear();
    }

    public void reachCheckpoint(Player player, int checkpoint) {
        if (players.containsKey(player.getUniqueId())) {
            players.put(player.getUniqueId(), checkpoint);
        }
    }

    private void eliminatePlayers() {
        for (UUID player : players.keySet()) {
            Player p = Bukkit.getPlayer(player);

            if (p != null) {
                if (!noElimination.contains(player)) {
                    Cl3vent.getInstance().getEventManager().eliminatePlayer(player);
                }
            }
        }
    }

    public void giveItems() {
        ItemStack checkpointItem = ItemBuilder.builder()
                .material(Material.NETHER_STAR)
                .displayName("<aqua>Regresar al ultimo checkpoint")
                .pdc(Key.getParkourItemsKey(), "checkpoint")
                .build();

        ItemStack toggleVisibilityItem = ItemBuilder.builder()
                .material(Material.ENDER_EYE)
                .displayName("<aqua>Cambiar visibilidad de los jugadores")
                .pdc(Key.getParkourItemsKey(), "toggle-visibility")
                .build();

        ItemStack impulse = ItemBuilder.builder()
                .material(Material.FEATHER)
                .displayName("<aqua>Impulso")
                .pdc(Key.getParkourItemsKey(), "impulse")
                .build();

        Cl3vent.getInstance().getEventManager().getPlayers().forEach(player -> {
            Player p = Bukkit.getPlayer(player);

            if (p != null && !p.hasPermission("cl3vent.bypass")) {
                p.getInventory().clear();
                p.getInventory().setItem(0, checkpointItem);
                p.getInventory().setItem(4, impulse);
                p.getInventory().setItem(8, toggleVisibilityItem);
            } else {
                Cl3vent.getInstance().getEventManager().getPlayers().remove(player);
            }
        });
    }

}
