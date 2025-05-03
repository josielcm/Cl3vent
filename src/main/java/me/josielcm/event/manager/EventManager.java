package me.josielcm.event.manager;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import lombok.Getter;
import lombok.Setter;
import me.josielcm.event.FileManager;
import me.josielcm.event.api.formats.Color;
import me.josielcm.event.api.logs.Log;
import me.josielcm.event.api.logs.Log.LogLevel;
import me.josielcm.event.api.regions.Container;
import me.josielcm.event.manager.games.GameType;
import me.josielcm.event.manager.games.balloonparkour.BalloonParkour;
import me.josielcm.event.manager.games.balloonshooting.BalloonShooting;
import me.josielcm.event.manager.games.cakefever.CakeFever;
import me.josielcm.event.manager.games.giantgift.GiantGift;
import me.josielcm.event.manager.games.giantgift.Gift;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.Title.Times;

public class EventManager {

    @Getter
    @Setter
    Set<UUID> allPlayers = new HashSet<>();

    @Getter
    @Setter
    Set<UUID> players = new HashSet<>();

    @Getter
    @Setter
    private CakeFever cakeFever;

    @Getter
    @Setter
    private BalloonParkour balloonParkour;

    @Getter
    @Setter
    private BalloonShooting balloonShooting;

    @Getter
    @Setter
    private GiantGift giantGift;

    @Getter
    @Setter
    private GameType actualGame = GameType.NONE;

    @Getter
    @Setter
    private boolean inGame = false;

    @Getter
    @Setter
    private Location spawn;

    public void instanceSpawn() {
        String worldS = FileManager.getSettings().getString("spawn.world");
        int xSpawn = FileManager.getSettings().getInt("spawn.x");
        int ySpawn = FileManager.getSettings().getInt("spawn.y");
        int zSpawn = FileManager.getSettings().getInt("spawn.z");

        if (Bukkit.getWorld(worldS) == null) {
            Log.log(LogLevel.ERROR, "World " + worldS + " not found for spawn");
            return;
        }

        spawn = new Location(Bukkit.getWorld(worldS), xSpawn, ySpawn, zSpawn);
        Log.log(LogLevel.INFO,
                "Spawn loaded at " + spawn.getBlockX() + ", " + spawn.getBlockY() + ", " + spawn.getBlockZ());
    }

    public void instanceGames() {
        cakeFever = new CakeFever();
        String title = FileManager.getCakefever().getString("settings.title");
        int limitElimination = FileManager.getCakefever().getInt("settings.limit-elimination");
        String worldS = FileManager.getCakefever().getString("settings.world");

        int xSpawn = FileManager.getCakefever().getInt("settings.spawn.x");
        int ySpawn = FileManager.getCakefever().getInt("settings.spawn.y");
        int zSpawn = FileManager.getCakefever().getInt("settings.spawn.z");

        if (Bukkit.getWorld(worldS) == null) {
            Log.log(LogLevel.ERROR, "World " + worldS + " not found for CakeFever");
            return;
        }

        Location spawn = new Location(Bukkit.getWorld(worldS), xSpawn, ySpawn, zSpawn);

        List<Location> cakes = new ArrayList<>();

        for (String key : FileManager.getCakefever().getConfigurationSection("cakes-spawns").getKeys(false)) {
            int xCake = FileManager.getCakefever().getInt("cakes-spawns." + key + ".x");
            int yCake = FileManager.getCakefever().getInt("cakes-spawns." + key + ".y");
            int zCake = FileManager.getCakefever().getInt("cakes-spawns." + key + ".z");
            Location cake = new Location(Bukkit.getWorld(worldS), xCake, yCake, zCake);

            cakes.add(cake);
        }

        cakeFever.setTitle(title);
        cakeFever.setLimitElimination(limitElimination);
        cakeFever.setSpawn(spawn);
        cakeFever.setWorld(Bukkit.getWorld(worldS));
        cakeFever.setCakes(cakes);

        title = FileManager.getBalloonparkour().getString("settings.title");
        worldS = FileManager.getBalloonparkour().getString("settings.world");
        xSpawn = FileManager.getBalloonparkour().getInt("settings.spawn.x");
        ySpawn = FileManager.getBalloonparkour().getInt("settings.spawn.y");
        zSpawn = FileManager.getBalloonparkour().getInt("settings.spawn.z");
        if (Bukkit.getWorld(worldS) == null) {
            Log.log(LogLevel.ERROR, "World " + worldS + " not found for BalloonParkour");
            return;
        }

        spawn = new Location(Bukkit.getWorld(worldS), xSpawn, ySpawn, zSpawn);
        ConcurrentHashMap<Integer, Location> checkpoints = new ConcurrentHashMap<>();
        for (String key : FileManager.getBalloonparkour().getConfigurationSection("checkpoints").getKeys(false)) {
            int xCheckpoint = FileManager.getBalloonparkour().getInt("checkpoints." + key + ".x");
            int yCheckpoint = FileManager.getBalloonparkour().getInt("checkpoints." + key + ".y");
            int zCheckpoint = FileManager.getBalloonparkour().getInt("checkpoints." + key + ".z");
            Location checkpoint = new Location(Bukkit.getWorld(worldS), xCheckpoint, yCheckpoint, zCheckpoint);

            checkpoints.put(Integer.parseInt(key), checkpoint);
        }

        int xSafeContainerPos1 = FileManager.getBalloonparkour().getInt("safe.pos1.x");
        int ySafeContainerPos1 = FileManager.getBalloonparkour().getInt("safe.pos1.y");
        int zSafeContainerPos1 = FileManager.getBalloonparkour().getInt("safe.pos1.z");

        int xSafeContainerPos2 = FileManager.getBalloonparkour().getInt("safe.pos2.x");
        int ySafeContainerPos2 = FileManager.getBalloonparkour().getInt("safe.pos2.y");
        int zSafeContainerPos2 = FileManager.getBalloonparkour().getInt("safe.pos2.z");

        Container safeContainer = new Container(
                new Location(Bukkit.getWorld(worldS), xSafeContainerPos1, ySafeContainerPos1, zSafeContainerPos1),
                new Location(Bukkit.getWorld(worldS), xSafeContainerPos2, ySafeContainerPos2, zSafeContainerPos2));

        balloonParkour = new BalloonParkour();
        balloonParkour.setTitle(title);
        balloonParkour.setSpawn(spawn);
        balloonParkour.setSafeContainer(safeContainer);
        balloonParkour.setWorld(Bukkit.getWorld(worldS));
        balloonParkour.setCheckpoints(checkpoints);

        int xSpawnBalloonShooting = FileManager.getBalloonshooting().getInt("settings.spawn.x");
        int ySpawnBalloonShooting = FileManager.getBalloonshooting().getInt("settings.spawn.y");
        int zSpawnBalloonShooting = FileManager.getBalloonshooting().getInt("settings.spawn.z");
        String worldSBalloonShooting = FileManager.getBalloonshooting().getString("settings.world");

        if (Bukkit.getWorld(worldSBalloonShooting) == null) {
            Log.log(LogLevel.ERROR, "World " + worldSBalloonShooting + " not found for BalloonShooting");
            return;
        }

        Location spawnBalloonShooting = new Location(Bukkit.getWorld(worldSBalloonShooting), xSpawnBalloonShooting,
                ySpawnBalloonShooting, zSpawnBalloonShooting);

        String titleBalloonShooting = FileManager.getBalloonshooting().getString("settings.title");

        int xRPos1 = FileManager.getBalloonshooting().getInt("region.pos1.x");
        int yRPos1 = FileManager.getBalloonshooting().getInt("region.pos1.y");
        int zRPos1 = FileManager.getBalloonshooting().getInt("region.pos1.z");

        int xRPos2 = FileManager.getBalloonshooting().getInt("region.pos2.x");
        int yRPos2 = FileManager.getBalloonshooting().getInt("region.pos2.y");
        int zRPos2 = FileManager.getBalloonshooting().getInt("region.pos2.z");

        Location pos1 = new Location(Bukkit.getWorld(worldSBalloonShooting), xRPos1, yRPos1, zRPos1);
        Location pos2 = new Location(Bukkit.getWorld(worldSBalloonShooting), xRPos2, yRPos2, zRPos2);

        balloonShooting = new BalloonShooting();
        balloonShooting.setTitle(titleBalloonShooting);
        balloonShooting.setSpawn(spawnBalloonShooting);
        balloonShooting.setPos1(pos1);
        balloonShooting.setPos2(pos2);
        balloonShooting.setWorld(Bukkit.getWorld(worldSBalloonShooting));

        String titleGiantGift = FileManager.getGiantgift().getString("settings.title");
        worldS = FileManager.getGiantgift().getString("settings.world");

        xSpawn = FileManager.getGiantgift().getInt("settings.spawn.x");
        ySpawn = FileManager.getGiantgift().getInt("settings.spawn.y");
        zSpawn = FileManager.getGiantgift().getInt("settings.spawn.z");

        if (Bukkit.getWorld(worldS) == null) {
            Log.log(LogLevel.ERROR, "World " + worldS + " not found for GiantGift");
            return;
        }

        spawn = new Location(Bukkit.getWorld(worldS), xSpawn, ySpawn, zSpawn);

        Set<Gift> gifts = new HashSet<>();

        for (String key : FileManager.getGiantgift().getConfigurationSection("gifts").getKeys(false)) {
            int xGift = FileManager.getGiantgift().getInt("gifts." + key + ".pos1.x");
            int yGift = FileManager.getGiantgift().getInt("gifts." + key + ".pos1.y");
            int zGift = FileManager.getGiantgift().getInt("gifts." + key + ".pos1.z");

            int xGift2 = FileManager.getGiantgift().getInt("gifts." + key + ".pos2.x");
            int yGift2 = FileManager.getGiantgift().getInt("gifts." + key + ".pos2.y");
            int zGift2 = FileManager.getGiantgift().getInt("gifts." + key + ".pos2.z");

            Location gift = new Location(Bukkit.getWorld(worldS), xGift, yGift, zGift);
            Location gift2 = new Location(Bukkit.getWorld(worldS), xGift2, yGift2, zGift2);

            Gift giftObj = new Gift(gift, gift2);
            giftObj.setId(new Random().nextInt(1000));

            gifts.add(giftObj);
        }

        giantGift = new GiantGift();
        giantGift.setTitle(titleGiantGift);
        giantGift.setSpawn(spawn);
        giantGift.setGifts(gifts);
        giantGift.setWorld(Bukkit.getWorld(worldS));
        giantGift.setLimitElimination(FileManager.getGiantgift().getInt("settings.limit-elimination"));

        Log.log(LogLevel.INFO, "CakeFever loaded with " + cakes.size() + " cakes");
        Log.log(LogLevel.INFO, "BalloonParkour loaded with " + checkpoints.size() + " checkpoints");
        Log.log(LogLevel.INFO,
                "BalloonShooting loaded with region: " + pos1.getBlockX() + ", " + pos1.getBlockY() + ", "
                        + pos1.getBlockZ() + " and " + pos2.getBlockX() + ", " + pos2.getBlockY() + ", "
                        + pos2.getBlockZ());
        Log.log(LogLevel.INFO, "GiantGift loaded with " + gifts.size() + " gifts");

    }

    public void startGame(GameType gameType) {
        if (inGame) {
            Log.log(LogLevel.INFO, "Game already started");
            return;
        }

        this.actualGame = gameType;
        this.inGame = true;

        switch (gameType) {
            case CAKEFEVER:
                cakeFever.prepare();
                break;
            case BALLOONPARKOUR:
                balloonParkour.prepare();
                break;
            case BALLONSHOOTING:
                balloonShooting.prepare();
                break;
            case GIANTGIFT:
                giantGift.prepare();
                break;
            default:
                break;
        }
    }

    public void stopGame() {
        if (!inGame) {
            Log.log(LogLevel.INFO, "Game already stopped");
            return;
        }

        switch (actualGame) {
            case CAKEFEVER:
                cakeFever.stop();
                break;
            case BALLOONPARKOUR:
                balloonParkour.stop();
                break;
            case BALLONSHOOTING:
                balloonShooting.stop();
                break;
            case GIANTGIFT:
                giantGift.end();
                break;
            default:
                break;
        }

    }

    public void stop() {
        inGame = false;
        actualGame = GameType.NONE;
    }

    public void eliminatePlayer(UUID player) {
        Player p = Bukkit.getPlayer(player);
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(player);

        sendMessage("<gradient:#ff0004:#ffb730:#ff2200>¡" + offlinePlayer.getName() + " eliminado!</gradient>");
        playSound(Sound.ENTITY_WARDEN_STEP);

        players.remove(player);

        switch (actualGame) {
            case CAKEFEVER:
                cakeFever.getPoints().remove(player);
                break;
            case BALLOONPARKOUR:
                balloonParkour.getPlayers().remove(player);
                break;
            case BALLONSHOOTING:
                balloonShooting.getPoints().remove(player);
                break;
            case GIANTGIFT:
                giantGift.getPlayers().remove(player);
                break;
            default:
                break;
        }

        if (p != null) {
            if (p.hasPermission("cl3vent.twitch")) {
                p.getInventory().clear();
                p.setGameMode(org.bukkit.GameMode.SPECTATOR);
            } else {
                p.getInventory().clear();
                p.setGameMode(org.bukkit.GameMode.SPECTATOR);
                p.kick(Color.parse("<gold>¡Gracias por jugar!"));
            }
        }

    }

    public void revivePlayer(UUID player) {
        players.add(player);
        Player p = Bukkit.getPlayer(player);
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(player);

        sendMessage("<gradient:#00e1ff:#a3a5ff>¡" + offlinePlayer.getName() + " ha sido revivido!");
        playSound(Sound.ENTITY_WARDEN_STEP);

        if (p != null) {
            p.setGameMode(org.bukkit.GameMode.ADVENTURE);
            p.getInventory().clear();
            p.teleport(spawn);
        }

    }

    public void sendActionBar(String message) {
        Component parsedMessage = Color.parse(message);

        for (UUID playerId : allPlayers) {
            Player p = Bukkit.getPlayer(playerId);
            if (p != null) {
                p.sendActionBar(parsedMessage);
            }
        }
    }

    public void sendMessage(String message) {
        Component parsedMessage = Color.parse(message);

        for (UUID playerId : allPlayers) {
            Player p = Bukkit.getPlayer(playerId);
            if (p != null) {
                p.sendMessage(parsedMessage);
            }
        }
    }

    public void showTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        Component parsedTitle = Color.parse(title);
        Component parsedSubtitle = Color.parse(subtitle);
        Times times = Times.times(
                Duration.ofSeconds(fadeIn),
                Duration.ofSeconds(stay),
                Duration.ofSeconds(fadeOut));
        Title titleObj = Title.title(parsedTitle, parsedSubtitle, times);

        for (UUID playerId : allPlayers) {
            Player p = Bukkit.getPlayer(playerId);
            if (p != null) {
                p.showTitle(titleObj);
            }
        }
    }

    public void playSound(Sound sound) {
        allPlayers.forEach(player -> {
            Player p = Bukkit.getPlayer(player);

            if (p != null) {
                p.playSound(p.getLocation(), sound, 1, 1);
            }

        });
    }

}
