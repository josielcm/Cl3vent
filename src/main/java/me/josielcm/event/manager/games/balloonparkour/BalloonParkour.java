package me.josielcm.event.manager.games.balloonparkour;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
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
import me.josielcm.event.api.formats.Color;
import me.josielcm.event.api.formats.Format;
import me.josielcm.event.api.items.ItemBuilder;
import me.josielcm.event.api.regions.Container;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.title.Title;

public class BalloonParkour {

    @Getter
    @Setter
    private ConcurrentHashMap<Integer, Location> checkpoints = new ConcurrentHashMap<>();

    @Getter
    @Setter
    private ConcurrentHashMap<UUID, Integer> players = new ConcurrentHashMap<>();

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
    private Title titleMsg;

    @Getter
    @Setter
    private Set<UUID> noElimination = ConcurrentHashMap.newKeySet();

    @Getter
    @Setter
    private ConcurrentHashMap<UUID, Boolean> visibility = new ConcurrentHashMap<>();

    @Getter
    @Setter
    private BukkitTask task;

    @Getter
    @Setter
    private Listener listener;

    @Getter
    @Setter
    private BossBar bossBar;

    public void prepare() {
        BalloonParkourEvents eventListener = new BalloonParkourEvents();
        this.listener = eventListener;

        titleMsg = Title.title(Color.parse(title), Color.parse("<gold>¡Completa el parkour!"));

        Bukkit.getPluginManager().registerEvents(listener, Cl3vent.getInstance());

        players.clear();
        noElimination.clear();
        visibility.clear();

        final Cl3vent plugin = Cl3vent.getInstance();
        final Set<UUID> eventPlayers = plugin.getEventManager().getPlayers();

        for (UUID playerId : eventPlayers) {
            Player p = Bukkit.getPlayer(playerId);
            if (p != null) {
                players.put(playerId, -1);
                visibility.put(playerId, true);
                p.setGameMode(org.bukkit.GameMode.ADVENTURE);
                p.showTitle(titleMsg);
                p.getInventory().clear();
                p.teleport(spawn);
            } else {
                players.remove(playerId);
            }
        }

        start();
    }

    public void start() {
        if (task != null) {
            task.cancel();
            task = null;
        }

        giveItemsOptimized();

        final Cl3vent plugin = Cl3vent.getInstance();
        final AtomicInteger time = new AtomicInteger(60);

        bossBar = BossBar.bossBar(
                Color.parse("<gold><b>" + Format.formatTime(time.get())),
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
                stop();
                return;
            }

            bossBar.name(Color.parse("<gold><b>" + Format.formatTime(currentTime)));

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

        Cl3vent.getInstance().getEventManager().stop();
    }

    public void reachCheckpoint(Player player, int checkpoint) {
        if (!players.containsKey(player.getUniqueId())) {
            Cl3vent.getInstance().getLogger().warning("Player not in game: " + player.getName());
            return;
        }

        int currentCheckpoint = players.get(player.getUniqueId());
        Cl3vent.getInstance().getLogger().info("Player " + player.getName() + " attempting checkpoint " + checkpoint +
                " (current: " + currentCheckpoint + ")");

        // Solo actualizar si es el siguiente checkpoint
        if (checkpoint == currentCheckpoint + 1) {
            players.put(player.getUniqueId(), checkpoint);

            // Efectos y mensajes
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

            if (checkpoint == checkpoints.size() - 1) {
                player.sendRichMessage("<green>¡Último checkpoint alcanzado! <gray>Busca la <gold>zona final<gray>.");
            } else {
                player.sendRichMessage("<green>Checkpoint " + checkpoint + " alcanzado! <gray>(" +
                        checkpoint + "/" + (checkpoints.size() - 1) + ")");
            }

            Cl3vent.getInstance().getLogger().info("Player " + player.getName() + " reached checkpoint " + checkpoint);
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

    public void giveItemsOptimized() {
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

        final Cl3vent plugin = Cl3vent.getInstance();
        Set<UUID> eventPlayers = plugin.getEventManager().getPlayers();

        eventPlayers.forEach(playerId -> {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                player.getInventory().clear();
                player.getInventory().setItem(0, checkpointItem.clone());
                player.getInventory().setItem(4, impulse.clone());
                player.getInventory().setItem(8, toggleVisibilityItem.clone());
            }
        });

    }

    public void updatePlayerVisibility(Player player, boolean visible) {
        visibility.put(player.getUniqueId(), visible);

        // Schedule this as a delayed task to not block the main thread
        Bukkit.getScheduler().runTaskLater(Cl3vent.getInstance(), () -> {
            // Process visibility changes in chunks to reduce lag spikes
            List<Player> activePlayers = new ArrayList<>();
            for (UUID uuid : Cl3vent.getInstance().getEventManager().getPlayers()) {
                Player target = Bukkit.getPlayer(uuid);
                if (target != null && target != player && !target.hasPermission("cl3vent.bypass")) {
                    activePlayers.add(target);
                }
            }

            // Process in batches
            final int BATCH_SIZE = 20;
            for (int i = 0; i < activePlayers.size(); i += BATCH_SIZE) {
                final int startIdx = i;
                Bukkit.getScheduler().runTaskLater(Cl3vent.getInstance(), () -> {
                    int endIdx = Math.min(startIdx + BATCH_SIZE, activePlayers.size());
                    for (int j = startIdx; j < endIdx; j++) {
                        if (visible) {
                            player.showPlayer(Cl3vent.getInstance(), activePlayers.get(j));
                        } else {
                            player.hidePlayer(Cl3vent.getInstance(), activePlayers.get(j));
                        }
                    }
                }, i / BATCH_SIZE);
            }

            // Show message to player
            String message = visible ? "<grey>Visibilidad de los jugadores fue <green>activada<grey>."
                    : "<grey>Visibilidad de los jugadores fue <red>desactivada<grey>.";
            player.sendRichMessage(message);
        }, 1L);
    }

}
