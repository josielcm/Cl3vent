package me.josielcm.event.manager.games.balloonshooting;

import java.time.Duration;
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
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import lombok.Getter;
import lombok.Setter;
import me.josielcm.event.Cl3vent;
import me.josielcm.event.api.formats.Color;
import me.josielcm.event.api.formats.Format;
import me.josielcm.event.api.items.ItemBuilder;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.Title.Times;

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
    private Title titleMsg;

    @Getter
    @Setter
    private BukkitTask task;

    @Getter
    @Setter
    private Listener listener;

    @Getter
    @Setter
    private BossBar bossBar;

    @Getter
    private final Cl3vent plugin = Cl3vent.getInstance();

    public void prepare() {
        points.clear();

        titleMsg = Title.title(Color.parse(title), Color.parse("<gradient:#14ffr8:96ffbd>¡Dispara a los globos!"));

        listener = new BalloonShootingEvents();

        Cl3vent.getInstance().getServer().getPluginManager().registerEvents(listener, Cl3vent.getInstance());

        List<UUID> playersToRemove = new ArrayList<>();
        Set<UUID> eventPlayers = plugin.getEventManager().getPlayers();

        for (UUID playerId : eventPlayers) {
            Player p = Bukkit.getPlayer(playerId);
            if (p != null) {
                points.put(playerId, 0);
                p.teleport(spawn);
                p.showTitle(titleMsg);
                p.setGameMode(org.bukkit.GameMode.ADVENTURE);
            } else {
                playersToRemove.add(playerId);
            }
        }

        for (UUID playerId : playersToRemove) {
            eventPlayers.remove(playerId);
            points.remove(playerId);
            plugin.getEventManager().getPlayers().remove(playerId);
        }

        final AtomicInteger time = new AtomicInteger(15);

        bossBar = BossBar.bossBar(
                Color.parse("<gradient:#14ffr8:96ffbd><b>¡Iniciando en <gold>" + Format.formatTime(time.get())
                        + "</gold>!"),
                0.0f,
                BossBar.Color.YELLOW,
                BossBar.Overlay.PROGRESS);

        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            int currentTime = time.getAndDecrement();
            if (currentTime <= 0) {
                start();
                return;
            }

            bossBar.name(Color.parse(
                    "<gradient:#14ffr8:96ffbd><b>¡Iniciando en <gold>" + Format.formatTime(currentTime) + "</gold>!"));

        }, 0L, 20L);
    }

    public void start() {
        if (task != null) {
            task.cancel();
            task = null;
        }

        regenerateBalloons();

        final AtomicInteger time = new AtomicInteger(60);

        bossBar = BossBar.bossBar(
                Color.parse("<gradient:#14ffr8:96ffbd><b>" + Format.formatTime(time.get())),
                0.0f,
                BossBar.Color.YELLOW,
                BossBar.Overlay.PROGRESS);

        for (UUID playerId : plugin.getEventManager().getAllPlayers()) {
            Player p = Bukkit.getPlayer(playerId);
            if (p != null) {
                p.showBossBar(bossBar);
                p.showBossBar(bossBar);
            }
        }

        giveItems();

        Cl3vent.getInstance().getEventManager().showTitle("<gradient:#14ffr8:96ffbd><b>¡Dispara a los globos!", "", 1,
                2, 0);
        Cl3vent.getInstance().getEventManager().playSound(Sound.ENTITY_PLAYER_LEVELUP);

        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            int currentTime = time.getAndDecrement();
            if (currentTime <= 0) {
                stop();
                return;
            }

            bossBar.name(Color.parse("<gradient:#14ffr8:96ffbd><b>" + Format.formatTime(currentTime)));

            if (currentTime % 15 == 0) {
                Bukkit.getScheduler().runTaskLater(plugin, this::regenerateBalloons, 1L);
            }

            if (currentTime == 10) {
                Cl3vent.getInstance().getEventManager()
                        .sendActionBar("<gradient:#14ffr8:96ffbd><b>¡Quedan 10 segundos!");
                Cl3vent.getInstance().getEventManager().playSound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
            }

        }, 0L, 20L);

    }

    public void stop() {
        if (task != null) {
            task.cancel();
        }

        Cl3vent.getInstance().getEventManager().getAllPlayers().forEach(player -> {
            Player p = Bukkit.getPlayer(player);

            if (p != null) {
                p.hideBossBar(bossBar);
                if (!p.hasPermission("cl3vent.bypass")) {
                    p.getInventory().clear();
                }
                p.teleport(Cl3vent.getInstance().getEventManager().getSpawn());
            } else {
                plugin.getEventManager().eliminatePlayer(player);
            }
        });

        removeAllBalloons();

        List<UUID> playersToEliminate = get10MenusPoints();

        Cl3vent.getInstance().getEventManager().showTitle("<gradient:#14ffr8:96ffbd><b>¡Juego terminado!", "", 1, 3, 1);
        Cl3vent.getInstance().getEventManager().playSound(Sound.ENTITY_WARDEN_HEARTBEAT);

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
                .displayName("<gold>Arco")
                .enchant(Enchantment.INFINITY, 1)
                .unbreakable(true)
                .hideUnbreakable()
                .hideAttributes()
                .hideEnchants()
                .build();

        ItemStack arrow = ItemBuilder.builder()
                .material(org.bukkit.Material.ARROW)
                .displayName("<grey>Flecha")
                .amount(1)
                .build();

        plugin.getEventManager().getPlayers().forEach(playerId -> {
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

    public List<UUID> get5MaxPoints() {
        return points.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))
                .limit(5)
                .map(entry -> entry.getKey())
                .toList();
    }

    public List<UUID> getAllPoints() {
        return points.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))
                .map(entry -> entry.getKey())
                .toList();
    }

    public void addPoint(UUID player, ArmorStand armorStand) {
        if (player == null || armorStand == null) {
            return;
        }

        Player p = Bukkit.getPlayer(player);

        if (p == null) {
            return;
        }

        if (isGold(armorStand)) {
            points.merge(player, 2, Integer::sum);

            Times times = Times.times(
                    Duration.ofSeconds(1),
                    Duration.ofSeconds(1),
                    Duration.ofSeconds(1));

            Title title = Title.title(
                    Color.parse("<gold><b>¡Punto doble!"),
                    Color.parse("<gold>+2"),
                    times);

            p.showTitle(title);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 1, 1);

            Cl3vent.getInstance().getEventManager()
                    .sendActionBar("<gradient:#14ffr8:96ffbd><b>¡" + p.getName() + " ha conseguido un punto doble!");
        } else {
            points.merge(player, 1, Integer::sum);

            p.sendActionBar(Color.parse("<aqua><b>+1"));
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 1, 1);
        }
    }

    private void regenerateBalloons() {
        if (pos1 == null || pos2 == null) {
            Bukkit.getLogger().warning("Cannot regenerate balloons: positions not set");
            return;
        }

        removeAllBalloons();

        try {
            for (int i = 0; i < 50; i++) {
                BalloonArmorModel balloon = new BalloonArmorModel(pos1, pos2);
                balloons.add(balloon);
            }
        } catch (IllegalArgumentException e) {
            Bukkit.getLogger().severe("Error regenerating balloons: " + e.getMessage());
        }
    }

    public void removeAllBalloons() {
        balloons.forEach(BalloonArmorModel::removeArmorStand);
        balloons.clear();
    }

    public void removeBalloon(ArmorStand armorStand) {
        balloons.removeIf(balloon -> balloon.getArmorStand().equals(armorStand));
        armorStand.remove();
    }

    public boolean isBalloon(ArmorStand armorStand) {
        return balloons.stream()
                .anyMatch(balloon -> balloon.getArmorStand().equals(armorStand));
    }

    public boolean isGold(ArmorStand armorStand) {
        return balloons.stream()
                .filter(balloon -> balloon.getArmorStand().equals(armorStand))
                .findFirst()
                .map(BalloonArmorModel::isGold)
                .orElse(false);
    }

}
