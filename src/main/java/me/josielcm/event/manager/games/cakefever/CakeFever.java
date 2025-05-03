package me.josielcm.event.manager.games.cakefever;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;

import lombok.Getter;
import lombok.Setter;
import me.josielcm.event.Cl3vent;
import me.josielcm.event.api.formats.Color;
import me.josielcm.event.api.formats.Format;
import me.josielcm.event.api.utils.RandomUtils;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.title.Title;

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
    private Title titleMsg;

    @Getter
    @Setter
    private int limitElimination = 0;

    @Getter
    @Setter
    private BukkitTask task;

    @Getter
    @Setter
    private Listener listener;

    @Getter
    @Setter
    private BossBar bossBar;

    private final ConcurrentHashMap<String, Boolean> cakeLocationCache = new ConcurrentHashMap<>();

    public void prepare() {
        CakeFeverEvent eventListener = new CakeFeverEvent();
        this.listener = eventListener;

        titleMsg = Title.title(Color.parse(title), Color.parse("<gradient:#14ffr8:#96ffbd>¡Encuentra los pasteles!"));

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
        final Set<UUID> eventPlayers = plugin.getEventManager().getAllPlayers();

        eventPlayers.forEach(player -> {
            Player p = Bukkit.getPlayer(player);
            if (p != null) {
                points.put(player, 0);
                if (!p.hasPermission("cl3vent.bypass")) {
                    p.setGameMode(org.bukkit.GameMode.ADVENTURE);
                }
                p.showTitle(titleMsg);
                p.teleport(spawn);

            }
        });

        AtomicInteger time = new AtomicInteger(15);

        bossBar = BossBar.bossBar(
                Color.parse("<gradient:#14ffr8:#96ffbd><b>¡Iniciando en <gold>" + Format.formatTime(time.get()) + "</gold>!"),
                0.0f,
                BossBar.Color.YELLOW,
                BossBar.Overlay.PROGRESS);

        for (UUID playerId : plugin.getEventManager().getAllPlayers()) {
            Player p = Bukkit.getPlayer(playerId);
            if (p != null) {
                p.showBossBar(bossBar);
            }
        }

        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            int currentTime = time.getAndDecrement();
            if (currentTime <= 0) {
                start();
                return;
            }

            bossBar.name(Color.parse("<gradient:#14ffr8:#96ffbd><b>¡Iniciando en <gold>" + Format.formatTime(currentTime) + "</gold>!"));
        }, 0L, 20L);

    }

    private void start() {
        if (task != null) {
            task.cancel();
            task = null;
        }

        final Cl3vent plugin = Cl3vent.getInstance();
        final AtomicInteger time = new AtomicInteger(60);

        bossBar.name(Color.parse("<gradient:#14ffr8:#96ffbd><b>" + Format.formatTime(time.get())));

        for (UUID playerId : plugin.getEventManager().getAllPlayers()) {
            Player p = Bukkit.getPlayer(playerId);
            if (p != null) {
                p.teleport(spawn);
                p.playSound(p.getLocation(), "ambient.buscatorta", 0.5f, 1.0f);
            }
        }

        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            int currentTime = time.getAndDecrement();
            if (currentTime <= 0) {
                stop();
                return;
            }

            bossBar.name(Color.parse("<gradient:#14ffr8:#96ffbd><b>" + Format.formatTime(currentTime)));

            if (currentTime % 10 == 0) {
                Bukkit.getScheduler().runTaskLater(plugin, this::regenerateCakes, 1L);
                Cl3vent.getInstance().getEventManager().sendMessage("<gradient:#14ffr8:#96ffbd>¡Los pasteles han sido regenerados!");
                Cl3vent.getInstance().getEventManager().playSound(Sound.BLOCK_NOTE_BLOCK_BELL);
            }
        }, 0L, 20L);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
        }

        for (UUID playerId : Cl3vent.getInstance().getEventManager().getAllPlayers()) {
            Player p = Bukkit.getPlayer(playerId);
            if (p != null) {
                p.hideBossBar(bossBar);
                p.teleport(Cl3vent.getInstance().getEventManager().getSpawn());
                p.stopAllSounds();
            } else {
                Cl3vent.getInstance().getEventManager().eliminatePlayer(playerId);
            }
        }

        Cl3vent.getInstance().getEventManager().showTitle("<gradient:#14ffr8:#96ffbd>¡Juego terminado!", "", 1, 3, 1);
        Cl3vent.getInstance().getEventManager().playSound(Sound.BLOCK_NOTE_BLOCK_IMITATE_ENDER_DRAGON);

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

        if (isCake) {
            points.compute(playerId, (k, v) -> v == null ? 1 : v + 1);
            player.sendActionBar(Color.parse("<gradient:#14ffr8:#96ffbd>¡+1 punto!"));
        } else {
            final int reducedPoints = RandomUtils.randomInt(1, 2);
            points.compute(playerId, (k, v) -> {
                if (v == null)
                    return 0;
                return Math.max(0, v - reducedPoints);
            });
            if (reducedPoints == 1) {
                player.sendActionBar(Color.parse("<red>¡-1 punto!"));
            } else {
                player.sendActionBar(Color.parse("<red>¡-2 puntos!"));
            }
        }

        player.sendRichMessage("<gradient:#14ffr8:#96ffbd>Puntos: " + points.get(playerId));
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
