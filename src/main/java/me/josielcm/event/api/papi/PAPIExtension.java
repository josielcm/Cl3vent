package me.josielcm.event.api.papi;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
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

        return null;
    }
    
}
