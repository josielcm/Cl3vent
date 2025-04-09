package me.josielcm.event.manager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import lombok.Getter;
import lombok.Setter;
import me.josielcm.event.Cl3vent;
import me.josielcm.event.FileManager;
import me.josielcm.event.api.formats.Color;
import me.josielcm.event.api.logs.Log;
import me.josielcm.event.api.logs.Log.LogLevel;
import me.josielcm.event.manager.games.GameType;
import me.josielcm.event.manager.games.cakefever.CakeFever;
import me.josielcm.event.manager.games.cakefever.CakeFeverEvent;

public class EventManager {

    @Getter
    @Setter
    Set<UUID> players = new HashSet<>();

    @Getter
    @Setter
    private CakeFever cakeFever;

    @Getter
    @Setter
    private GameType actualGame = GameType.NONE;

    @Getter
    @Setter
    private boolean inGame = false;

    public void registerEvents() {
        Cl3vent.getInstance().getServer().getPluginManager().registerEvents(new CakeFeverEvent(), Cl3vent.getInstance());
    }

    public void instanceGames() {
        cakeFever = new CakeFever();
        String title = FileManager.getCakefever().getString("settigns.title");
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

        Log.log(LogLevel.INFO, "CakeFever loaded with " + cakes.size() + " cakes");

    }

    public void startGame(GameType gameType) {
        if (inGame) {
            Log.log(LogLevel.INFO, "Game already started");
            return;
        }

        switch (gameType) {
            case CAKEFEVER:
                cakeFever.prepare();
                break;
        
            default:
                break;
        }

    }

    public void sendActionBar(String message) {
        players.forEach(player -> {
            Player p = Bukkit.getPlayer(player);

            if (p != null) {
                p.sendActionBar(Color.parse(message));
            }

        });
    }

    public void sendMessage(String message) {
        players.forEach(player -> {
            Player p = Bukkit.getPlayer(player);

            if (p != null) {
                p.sendMessage(Color.parse(message));
            }

        });
    }

}
