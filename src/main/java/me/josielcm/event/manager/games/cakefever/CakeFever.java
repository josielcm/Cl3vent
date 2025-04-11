package me.josielcm.event.manager.games.cakefever;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.title.Title.Times;
import me.josielcm.event.Cl3vent;
import me.josielcm.event.api.formats.Color;
import me.josielcm.event.api.utils.RandomUtils;
import net.kyori.adventure.title.Title;

public class CakeFever {

    @Getter
    @Setter
    private List<Location> cakes = new ArrayList<>();

    @Getter
    @Setter
    private HashMap<UUID, Integer> points = new HashMap<>();

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

    public void prepare() {
        listener = new CakeFeverEvent();
        Cl3vent.getInstance().getServer().getPluginManager().registerEvents(listener, Cl3vent.getInstance());

        removeCakes();
        setCakesBlock();

        // showTitle();

        Cl3vent.getInstance().getEventManager().getPlayers().forEach(player -> {
            Player p = Bukkit.getPlayer(player);

            if (p != null) {
                p.teleport(spawn);
            } else {
                Cl3vent.getInstance().getEventManager().getPlayers().remove(player);
            }
        });

        start();
    }

    private void start() {

        task = Bukkit.getScheduler().runTaskTimer(Cl3vent.getInstance(), new Runnable() {
            int time = 300;

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
        removeCakes();
        eliminatePlayers();

        HandlerList.unregisterAll(listener);
        points.clear();
    }

    private void eliminatePlayers() {
        List<UUID> players = get20MenusPoints();

        for (UUID player : players) {
            Player p = Bukkit.getPlayer(player);

            if (p != null) {
                Cl3vent.getInstance().getEventManager().eliminatePlayer(player);;
            }
        }
    }

    private List<UUID> get20MenusPoints() {
        List<UUID> players = new ArrayList<>();

        List<HashMap.Entry<UUID, Integer>> sortedEntries = new ArrayList<>(points.entrySet());
        sortedEntries.sort(Map.Entry.comparingByValue());

        int limit = Math.min(20, sortedEntries.size());
        for (int i = 0; i < limit; i++) {
            players.add(sortedEntries.get(i).getKey());
        }

        return players;
    }

    public void randomPoint(Player player) {
        boolean isCake = RandomUtils.randomBool();

        if (isCake) {
            int actualPoints = points.getOrDefault(player.getUniqueId(), 0);
            points.put(player.getUniqueId(), actualPoints + 1);
            player.sendRichMessage("<aqua>+1</aqua>");
        } else {
            int actualPoints = points.getOrDefault(player.getUniqueId(), 0);
            int reducedPoints = RandomUtils.randomInt(1, 3);
            int newPoints = Math.max(0, actualPoints - reducedPoints);
            points.put(player.getUniqueId(), newPoints);
            player.sendRichMessage("<red>-" + reducedPoints + "</red>");
        }

    }

    public void regenerateCakes() {
        removeCakes();
        setCakesBlock();
    }

    private void showTitle() {
        Times times = Times.times(
                Duration.ofSeconds(1),
                Duration.ofSeconds(3),
                Duration.ofSeconds(1)

        );

        Title titleC = Title.title(
                Color.parse(title),
                Color.parse(""),
                times);

        Bukkit.getOnlinePlayers().forEach(player -> {
            player.showTitle(titleC);
        });
    }

    private void setCakesBlock() {
        for (Location cake : cakes) {
            cake.getBlock().setType(org.bukkit.Material.CAKE);
        }
    }

    private void removeCakes() {
        for (Location cake : cakes) {
            cake.getBlock().setType(org.bukkit.Material.AIR);
        }
    }
}
