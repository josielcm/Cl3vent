package me.josielcm.jcm.api.papi;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class PAPIExtension extends PlaceholderExpansion {

    @Override
    public @NotNull String getIdentifier() {
        return "base";
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
            return "";
        }

        if (params.equals("test")) {
            return getIdentifier() + " by " + getAuthor() + " v" + getVersion();
        }

        return null;
    }
    
}
