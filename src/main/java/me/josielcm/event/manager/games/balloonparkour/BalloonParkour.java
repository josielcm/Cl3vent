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

    @Getter
    @Setter
    private int reachedPlayers = 0;

    @Getter
    @Setter
    private int maxPlayers = 30;

    public void prepare() {
        BalloonParkourEvents eventListener = new BalloonParkourEvents();
        this.listener = eventListener;

        titleMsg = Title.title(Color.parse(title), Color.parse("<gradient:#14ffe8:#96ffbd><b>¡Completa el parkour!"));

        Bukkit.getPluginManager().registerEvents(listener, Cl3vent.getInstance());

        players.clear();
        noElimination.clear();
        visibility.clear();
        reachedPlayers = 0;

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

        AtomicInteger time = new AtomicInteger(15);

        bossBar = BossBar.bossBar(
                Color.parse("<gradient:#14ffe8:#96ffbd><b>¡Iniciando en <gold>" + Format.formatTime(time.get()) + "</gold>!"),
                0.0f,
                BossBar.Color.YELLOW,
                BossBar.Overlay.PROGRESS);

        plugin.getEventManager().getAllPlayers().forEach(playerId -> {
            Player p = Bukkit.getPlayer(playerId);
            if (p != null) {
                p.showBossBar(bossBar);
            }
        });

        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            int currentTime = time.getAndDecrement();
            if (currentTime <= 0) {
                start();
                return;
            }

            bossBar.name(Color.parse("<gradient:#14ffe8:#96ffbd><b>¡Iniciando en <gold>" + Format.formatTime(currentTime) + "</gold>!"));

        }, 0L, 20L);
        
    }

    public void start() {
        if (task != null) {
            task.cancel();
            task = null;
        }

        giveItemsOptimized();

        final Cl3vent plugin = Cl3vent.getInstance();
        final AtomicInteger time = new AtomicInteger(330);

        Cl3vent.getInstance().getEventManager().showTitle("<gradient:#14ffe8:#96ffbd>¡El juego ha comenzado!", "", 1, 2, 1);
        Cl3vent.getInstance().getEventManager().playSound(Sound.ENTITY_PLAYER_LEVELUP);

        bossBar.name(Color.parse("<gradient:#14ffe8:#96ffbd><b>" + Format.formatTime(time.get())));

        for (UUID playerId : plugin.getEventManager().getAllPlayers()) {
            Player p = Bukkit.getPlayer(playerId);
            if (p != null) {
                p.teleport(spawn);
                p.playSound(p.getLocation(), "iaalchemy:ambient.parkour", 0.5f, 1.0f);
            }
        }

        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            int currentTime = time.getAndDecrement();
            if (currentTime <= 0) {
                stop();
                return;
            }

            bossBar.name(Color.parse("<gradient:#14ffe8:#96ffbd><b>" + Format.formatTime(currentTime)));

        }, 0L, 20L);

    }

    public void stop() {
        if (task != null) {
            task.cancel();
        }

        Cl3vent.getInstance().getEventManager().showTitle("<gradient:#14ffe8:#96ffbd>¡Juego terminado!", "", 1, 2, 1);
        Cl3vent.getInstance().getEventManager().playSound(Sound.ENTITY_PLAYER_LEVELUP);

        Cl3vent.getInstance().getEventManager().getAllPlayers().forEach(player -> {
            Player p = Bukkit.getPlayer(player);

            if (p != null) {
                p.hideBossBar(bossBar);
                if (!p.hasPermission("cl3vent.bypass")) {
                    p.getInventory().clear();
                }
                p.teleport(Cl3vent.getInstance().getEventManager().getSpawn());

                p.stopAllSounds();
            } else {
                Cl3vent.getInstance().getEventManager().eliminatePlayer(player);
            }
        });

        eliminatePlayers();

        HandlerList.unregisterAll(listener);

        players.clear();
        noElimination.clear();
        visibility.clear();
        reachedPlayers = 0;

        Cl3vent.getInstance().getEventManager().stop();
    }

    public void reachCheckpoint(Player player, int checkpoint) {
        if (!players.containsKey(player.getUniqueId())) {
            Cl3vent.getInstance().getLogger().warning("Player not in game: " + player.getName());
            return;
        }

        int currentCheckpoint = players.get(player.getUniqueId());

        if (checkpoint == currentCheckpoint + 1) {
            players.put(player.getUniqueId(), checkpoint);

            if (checkpoint == checkpoints.size() - 1) {
                player.sendRichMessage("<gradient:#14ffe8:#96ffbd>¡Último checkpoint alcanzado ve a final!");
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            } else { // <gradient:#14ffr8:96ffbd>¡
                if (checkpoint != 0) {
                    player.sendRichMessage("<gradient:#14ffe8:#96ffbd>¡Checkpoint " + checkpoint + " alcanzado!</gradient> <gray>(<aqua>" +
                    checkpoint + "</aqua>/<aqua>" + (checkpoints.size() - 1) + "</aqua>)");   
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                }
            }
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
                .displayName("<aqua><b>Regresar al ultimo checkpoint")
                .pdc(Key.getParkourItemsKey(), "checkpoint")
                .build();

        ItemStack toggleVisibilityItem = ItemBuilder.builder()
                .material(Material.ENDER_EYE)
                .displayName("<aqua><b>Cambiar visibilidad de los jugadores")
                .pdc(Key.getParkourItemsKey(), "toggle-visibility")
                .build();

        ItemStack impulse = ItemBuilder.builder()
                .material(Material.FEATHER)
                .displayName("<aqua><b>Impulso")
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
        if (player.hasCooldown(Material.ENDER_EYE)) {
            player.sendRichMessage("<red>Debes esperar para volver a usarlo.");
            return;
        }

        player.setCooldown(Material.ENDER_EYE, 20 * 3);

        visibility.put(player.getUniqueId(), visible);

        Bukkit.getScheduler().runTaskLater(Cl3vent.getInstance(), () -> {
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

            String message = visible ? "<grey>Visibilidad de los jugadores fue <green>activada<grey>."
                    : "<grey>Visibilidad de los jugadores fue <red>desactivada<grey>.";
            player.sendRichMessage(message);
        }, 1L);
    }

}
