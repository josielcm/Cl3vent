package me.josielcm.event.api.papi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.josielcm.event.Cl3vent;
import me.josielcm.event.manager.games.GameType;

public class PAPIExtension extends PlaceholderExpansion {

    @Override
    public @NotNull String getIdentifier() {
        return "Cl3vent";
    }

    @Override
    public @NotNull String getAuthor() {
        return "JosielCM";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (player == null) {
            return "NONE";
        }

        if (params.startsWith("player_checkpoint_")) {
            String playerName = params.substring(18);

            if (playerName == null || playerName.isEmpty()) {
                return "NONE";
            }
            
            UUID uuid = Bukkit.getPlayerUniqueId(playerName);

            if (uuid == null) {
                return "NONE";
            }

            if (Cl3vent.getInstance().getEventManager().getActualGame() != null && Cl3vent.getInstance().getEventManager().getActualGame() == GameType.BALLOONPARKOUR) {

                if (!Cl3vent.getInstance().getEventManager().getBalloonParkour().getPlayers().containsKey(uuid)) {
                    return "NONE";
                }

                int checkpoint = Cl3vent.getInstance().getEventManager().getBalloonParkour().getPlayers().get(uuid);
                return String.valueOf(checkpoint);
            }

            return "NONE";
        }

        if (params.startsWith("checkpoint_")) {
            String check = params.substring(11);
            int checkpoint = -1;

            try {
                checkpoint = Integer.parseInt(check);
                if (checkpoint < 0) {
                    return "NONE";
                }
            } catch (NumberFormatException e) {
                return "NONE";
            }
            
            var eventManager = Cl3vent.getInstance().getEventManager();
            if (eventManager.getActualGame() == GameType.BALLOONPARKOUR) {
                var balloonParkour = eventManager.getBalloonParkour();
                if (balloonParkour.getCheckpoints().containsKey(checkpoint)) {
                    Location loc = balloonParkour.getCheckpoints().get(checkpoint);
                    if (loc != null) {
                        return loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ();
                    }
                }
            }
            
            return "NONE";
        }

        if (params.equals("actual_game")) {
            return Cl3vent.getInstance().getEventManager().getActualGame() != null ? Cl3vent.getInstance().getEventManager().getActualGame().name().toUpperCase() : "NONE";
        }

        // %cl3vent_cakefever_topname_#%
        // %cl3vent_cakefever_top_#%

        // %cl3vent_balloon_topname_#%
        // %cl3vent_balloon_top_#%

        // %cl3vent_parkour_reached%
        // %cl3vent_parkour_max%

        if (params.startsWith("cakefever_topname_")) {
        try {
            int position = Integer.parseInt(params.substring(18));
            if (position < 1) return "NONE";
            
            var eventManager = Cl3vent.getInstance().getEventManager();
            if (eventManager.getActualGame() == GameType.CAKEFEVER) {
                List<UUID> topPlayers = new ArrayList<>(eventManager.getCakeFever().getPoints().entrySet().stream()
                        .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))
                        .map(Map.Entry::getKey)
                        .toList());
                
                if (position <= topPlayers.size()) {
                    UUID playerId = topPlayers.get(position - 1);
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerId);
                    return offlinePlayer.getName() != null ? offlinePlayer.getName() : "Unknown";
                }
            }
            return "NONE";
        } catch (NumberFormatException e) {
            return "NONE";
        }
    }
    
    // CakeFever top player points placeholders
    if (params.startsWith("cakefever_top_")) {
        try {
            int position = Integer.parseInt(params.substring(14));
            if (position < 1) return "NONE";
            
            var eventManager = Cl3vent.getInstance().getEventManager();
            if (eventManager.getActualGame() == GameType.CAKEFEVER) {
                List<Map.Entry<UUID, Integer>> topPoints = new ArrayList<>(eventManager.getCakeFever().getPoints().entrySet().stream()
                        .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))
                        .toList());
                
                if (position <= topPoints.size()) {
                    return String.valueOf(topPoints.get(position - 1).getValue());
                }
            }
            return "0";
        } catch (NumberFormatException e) {
            return "NONE";
        }
    }
    
    // BalloonShooting top player name placeholders
    if (params.startsWith("balloon_topname_")) {
        try {
            int position = Integer.parseInt(params.substring(16));
            if (position < 1) return "NONE";
            
            var eventManager = Cl3vent.getInstance().getEventManager();
            if (eventManager.getActualGame() == GameType.BALLONSHOOTING) {
                List<UUID> topPlayers = eventManager.getBalloonShooting().get5MaxPoints();
                
                if (position <= topPlayers.size()) {
                    UUID playerId = topPlayers.get(position - 1);
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerId);
                    return offlinePlayer.getName() != null ? offlinePlayer.getName() : "Unknown";
                }
            }
            return "NONE";
        } catch (NumberFormatException e) {
            return "NONE";
        }
    }
    
    if (params.startsWith("balloon_top_")) {
        try {
            int position = Integer.parseInt(params.substring(12));
            if (position < 1) return "NONE";
            
            var eventManager = Cl3vent.getInstance().getEventManager();
            if (eventManager.getActualGame() == GameType.BALLONSHOOTING) {
                List<UUID> topPlayers = eventManager.getBalloonShooting().get5MaxPoints();
                
                if (position <= topPlayers.size()) {
                    UUID playerId = topPlayers.get(position - 1);
                    int points = eventManager.getBalloonShooting().getPoints().getOrDefault(playerId, 0);
                    return String.valueOf(points);
                }
            }
            return "0";
        } catch (NumberFormatException e) {
            return "NONE";
        }
    }
    
    if (params.equals("parkour_reached")) {
        var eventManager = Cl3vent.getInstance().getEventManager();
        if (eventManager.getActualGame() == GameType.BALLOONPARKOUR) {
            int reached = eventManager.getBalloonParkour().getReachedPlayers();
            return String.valueOf(reached);
        }
        return "0";
    }
    
    if (params.equals("parkour_max")) {
        var eventManager = Cl3vent.getInstance().getEventManager();
        if (eventManager.getActualGame() == GameType.BALLOONPARKOUR) {
            return String.valueOf(eventManager.getBalloonParkour().getMaxPlayers());
        }
        return "0";
    }

    return null;
    }    
}
