package me.josielcm.event.manager;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import lombok.Getter;
import lombok.Setter;
import me.josielcm.event.Cl3vent;
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
