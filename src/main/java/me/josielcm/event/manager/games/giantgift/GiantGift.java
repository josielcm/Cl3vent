package me.josielcm.event.manager.games.giantgift;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.title.Title;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;

import lombok.Getter;
import lombok.Setter;
import me.josielcm.event.Cl3vent;
import me.josielcm.event.api.formats.Color;
import me.josielcm.event.api.formats.Format;
import me.josielcm.event.manager.EventManager;

public class GiantGift {

    @Getter
    @Setter
    private int round = 0;

    @Getter
    @Setter
    private Set<Gift> gifts = new HashSet<>();

    @Getter
    @Setter
    private Set<UUID> players = new HashSet<>();

    @Getter
    @Setter
    private Location spawn;

    @Getter
    @Setter
    private World world;

    @Getter
    @Setter
    private int limitElimination = 0;

    @Getter
    @Setter
    private int elimination = 0;

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
    private String title = "Giant Gift";

    @Getter
    @Setter
    private boolean available = false;

    @Getter
    @Setter
    private Title titleMsg;

    //<gradient:#14ffr8:96ffbd>¡

    public void prepare() {
        GiantGiftEvents listener = new GiantGiftEvents();
        this.listener = listener;

        Cl3vent plugin = Cl3vent.getInstance();
        EventManager eventManager = plugin.getEventManager();

        titleMsg = Title.title(Color.parse(title), Color.parse("<gradient:#14ffr8:96ffbd>¡No te quedes afuera!"));

        plugin.getServer().getPluginManager().registerEvents(listener, plugin);

        round = 1;
        players = new HashSet<>(eventManager.getPlayers());

        players.forEach(player -> {
            Player p = Bukkit.getPlayer(player);

            if (p != null) {
                p.setGameMode(org.bukkit.GameMode.ADVENTURE);
                p.getInventory().clear();
            } else {
                players.remove(player);
                eventManager.getPlayers().remove(player);
            }
        });

        eventManager.getAllPlayers().forEach(player -> {
            Player p = Bukkit.getPlayer(player);
            if (p != null) {
                p.teleport(spawn);
                p.showTitle(titleMsg);
            }
        });

        AtomicInteger time = new AtomicInteger(15);

        bossBar = BossBar.bossBar(
                Color.parse("<gradient:#14ffr8:96ffbd><b>¡Iniciando en <gold>" + Format.formatTime(time.get()) + "</gold>!"),
                0.0f,
                BossBar.Color.YELLOW,
                BossBar.Overlay.PROGRESS);

        for (UUID playerId : eventManager.getAllPlayers()) {
            Player p = Bukkit.getPlayer(playerId);
            if (p != null) {
                p.hideBossBar(bossBar);
                p.showBossBar(bossBar);
            }
        }

        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            int currentTime = time.getAndDecrement();
            if (currentTime <= 0) {
                start();
                return;
            }

            bossBar.name(Color.parse("<gradient:#14ffr8:96ffbd><b>¡Iniciando en <gold>" + Format.formatTime(currentTime) + "</gold>!"));

        }, 0L, 20L);
    
    }

    public void start() {
        if (task != null) {
            task.cancel();
        }

        generateCapacities();

        AtomicInteger time = new AtomicInteger(60);

        bossBar = BossBar.bossBar(
                Color.parse("<gradient:#14ffr8:96ffbd><b>" + Format.formatTime(time.get())),
                0.0f,
                BossBar.Color.YELLOW,
                BossBar.Overlay.PROGRESS);

        for (UUID playerId : Cl3vent.getInstance().getEventManager().getAllPlayers()) {
            Player p = Bukkit.getPlayer(playerId);
            if (p != null) {
                p.hideBossBar(bossBar);
                p.showBossBar(bossBar);
            }
        }

        Cl3vent.getInstance().getEventManager().showTitle("<aqua><b>Ronda " + round, "<gradient:#14ffr8:96ffbd><b>¡No te quedes fuera!", 1, 2, 1);
        Cl3vent.getInstance().getEventManager().sendActionBar("<gradient:#ff510d:ffc800><b>¡Muevete!");
        Cl3vent.getInstance().getEventManager().playSound(Sound.ENTITY_PLAYER_LEVELUP);

        task = Bukkit.getScheduler().runTaskTimer(Cl3vent.getInstance(), () -> {
            int currentTime = time.getAndDecrement();
            if (currentTime <= 0) {
                startNextRound();
                return;
            }

            if (checkIfAllGiftsAreFull()) {
                Cl3vent.getInstance().getEventManager().showTitle("<gradient:#14ffr8:96ffbd>¡Opps!", "<gradient:#14ffr8:96ffbd>Se han llenado todos los regalos", 1, 2, 1);
                startNextRound();
                return;
            }

            if (elimination >= limitElimination) {
                end();
                return;
            }

            if (currentTime == 40) {
                Cl3vent.getInstance().getEventManager().showTitle("<gradient:#14ffr8:96ffbd><b>¡Corre!", "<gradient:#14ffr8:96ffbd><b>Encuentra espacio en un regalo", 1, 2, 1);
                available = true;
            }

            bossBar.name(Color.parse("<gradient:#14ffr8:96ffbd><b>" + Format.formatTime(currentTime)));

        }, 0L, 20L);

    }

    private void startNextRound() {
        if (task != null) {
            task.cancel();
        }

        task = null;

        eliminatePlayers();
        clearGiftsPlayers();

        if (round >= 6) {
            end();
            return;
        }

        round++;

        if (elimination >= limitElimination) {
            end();
            return;
        }

        if (Cl3vent.getInstance().getEventManager().getPlayers().size() <= 1) {
            end();
            return;
        }

        for (UUID playerId : players) {
            Player p = Bukkit.getPlayer(playerId);
            if (p != null) {
                p.teleport(spawn);
                p.setGameMode(org.bukkit.GameMode.ADVENTURE);
            }
        }

        AtomicInteger coundown = new AtomicInteger(15);
        bossBar.name(Color.parse("<gradient:#ff510d:ffc800><b>¡Preparando la próxima ronda!"));

        task = Bukkit.getScheduler().runTaskTimer(Cl3vent.getInstance(), () -> {
            int currentTime = coundown.getAndDecrement();
            if (currentTime <= 0) {
                startNextTask();
                return;
            }

            Cl3vent.getInstance().getEventManager().sendActionBar("<gold><b>" + Format.formatTime(currentTime));
        }, 0L, 20L);

    }

    private void startNextTask() {
        if (task != null) {
            task.cancel();
        }
        task = null;

        AtomicInteger time = new AtomicInteger(60);
        generateCapacities();

        Cl3vent.getInstance().getEventManager().showTitle("<aqua><b>Ronda " + round, "<gradient:#14ffr8:96ffbd><b>¡No te quedes fuera!", 1, 2, 1);
        Cl3vent.getInstance().getEventManager().sendActionBar("<gradient:#ff510d:ffc800><b>¡Muevete!");
        Cl3vent.getInstance().getEventManager().playSound(Sound.ENTITY_PLAYER_LEVELUP);

        task = Bukkit.getScheduler().runTaskTimer(Cl3vent.getInstance(), () -> {
            int currentTime = time.getAndDecrement();
            if (currentTime <= 0) {
                startNextRound();
                return;
            }

            if (checkIfAllGiftsAreFull()) {
                Cl3vent.getInstance().getEventManager().showTitle("<gradient:#14ffr8:96ffbd>¡Opps!", "<gradient:#14ffr8:96ffbd>Se han llenado todos los regalos", 1, 2, 1);
                startNextRound();
                return;
            }

            if (elimination >= limitElimination) {
                end();
                return;
            }

            if (currentTime == 40) {
                Cl3vent.getInstance().getEventManager().showTitle("<gradient:#14ffr8:96ffbd><b>¡Corre!", "<gradient:#14ffr8:96ffbd><b>Encuentra espacio en un regalo", 1, 2, 1);
                available = true;
            }

            bossBar.name(Color.parse("<gold><b>" + Format.formatTime(currentTime)));

        }, 0L, 20L);
    }

    public void end() {
        if (task != null) {
            task.cancel();
        }

        for (UUID playerId : Cl3vent.getInstance().getEventManager().getAllPlayers()) {
            Player p = Bukkit.getPlayer(playerId);
            if (p != null) {
                p.hideBossBar(bossBar);
                p.teleport(Cl3vent.getInstance().getEventManager().getSpawn());
            }
        }

        players.clear();
        clearGiftsPlayers();

        HandlerList.unregisterAll(listener);
        Cl3vent.getInstance().getEventManager().stop();
    }

    private void eliminatePlayers() {
        if (elimination >= limitElimination) {
            return;
        }

        Set<UUID> playersToSurvive = new HashSet<>();

        for (Gift gift : gifts) {
            Set<UUID> playersInGift = gift.getPlayers();
            if (playersInGift.size() > 0) {
                playersToSurvive.addAll(playersInGift);
            }
        }

        clearGiftsPlayers();

        Set<UUID> playersToEliminate = new HashSet<>(players);

        playersToEliminate.forEach(player -> {
            Player p = Bukkit.getPlayer(player);
            if (p != null) {
                if (!playersToSurvive.contains(player)) {
                    elimination++;
                    Cl3vent.getInstance().getEventManager().eliminatePlayer(player);
                }
            }
        });
    }

    private void clearGiftsPlayers() {
        for (Gift gift : gifts) {
            gift.clear();
        }
    }

    public boolean isGift(Location loc) {
        for (Gift gift : gifts) {
            if (gift.isInArea(loc)) {
                return true;
            }
        }
        return false;
    }

    public Gift getGift(Location loc) {
        if (!isGift(loc)) {
            return null;
        }

        for (Gift gift : gifts) {
            if (gift.isInArea(loc)) {
                return gift;
            }
        }
        return null;
    }

    private void generateCapacities() {
        if (gifts.isEmpty() || gifts.size() != 5) {
            return;
        }

        switch (round) {
            case 1, 2, 3, 4, 5, 6:
                GiftCapacities roundCapacity = switch (round) {
                    case 1 -> GiftCapacities.RD_1;
                    case 2 -> GiftCapacities.RD_2;
                    case 3 -> GiftCapacities.RD_3;
                    case 4 -> GiftCapacities.RD_4;
                    case 5 -> GiftCapacities.RD_5;
                    case 6 -> GiftCapacities.RD_6;
                    default -> null;
                };

                if (roundCapacity == null)
                    break;

                int totalPass = roundCapacity.getPass();
                int remainingCapacity = totalPass;

                Gift[] giftArray = gifts.toArray(new Gift[0]);

                for (int i = 0; i < 4; i++) {
                    int maxForThisGift = Math.max(1, remainingCapacity / (5 - i) + 2);
                    int minForThisGift = Math.max(1, maxForThisGift / 2);

                    int giftCapacity = minForThisGift;
                    if (maxForThisGift > minForThisGift) {
                        giftCapacity += (int) (Math.random() * (maxForThisGift - minForThisGift));
                    }

                    giftCapacity = Math.min(giftCapacity, remainingCapacity - (4 - i));

                    giftArray[i].setCapacity(giftCapacity);
                    remainingCapacity -= giftCapacity;
                }

                giftArray[4].setCapacity(remainingCapacity);

                Bukkit.getLogger().info("Round " + round + " capacities:");
                for (int i = 0; i < 5; i++) {
                    Bukkit.getLogger().info("Gift " + (i + 1) + ": " + giftArray[i].getCapacity());
                }

                break;

            default:
                break;
        }
    }

    private boolean checkIfAllGiftsAreFull() {
        for (Gift gift : gifts) {
            if (!gift.isFull()) {
                return false;
            }
        }
        return true;
    }

    public void safePlayer(UUID uuid, Location loc) {
        if (isGift(loc)) {
            Gift gift = getGift(loc);
            if (gift != null) {
                if (gift.isFull()) {
                    Player p = Bukkit.getPlayer(uuid);
                    if (p != null) {
                        p.sendRichMessage("<red><b>¡El regalo está lleno busca otro!");
                    }

                    return;
                }
                

                if (gift.add(uuid, loc)) {
                    Player p = Bukkit.getPlayer(uuid);

                    if (p != null) {
                        p.setGameMode(org.bukkit.GameMode.SPECTATOR);
                        p.sendRichMessage("<gold><b>¡Entraste al regalo a tiempo!");
                        p.playSound(p.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1, 1);

                        Cl3vent.getInstance().getEventManager().sendActionBar("<green><b>¡" + p.getName() + " se salvo!");
                    }
                } else {
                    if (gift.isFull()) {
                        Player p = Bukkit.getPlayer(uuid);
                        if (p != null) {
                            p.sendRichMessage("<red><b>¡El regalo está lleno busca otro!");
                        }
                    }
                }
            }
        }
    }

    public boolean isSafePlayer(UUID uuid) {
        for (Gift gift : gifts) {
            if (gift.getPlayers().contains(uuid)) {
                return true;
            }
        }
        return false;
    }

}
