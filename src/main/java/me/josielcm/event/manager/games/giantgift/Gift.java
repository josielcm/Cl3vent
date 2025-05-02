package me.josielcm.event.manager.games.giantgift;

import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Location;

import lombok.Getter;
import lombok.Setter;

public class Gift {

    @Getter
    @Setter
    private Location pos1;

    @Getter
    @Setter
    private Location pos2;

    @Getter
    @Setter
    private int capacity = 0;

    @Getter
    @Setter
    private int current = 0;

    @Getter
    @Setter
    private int id = 0;

    @Getter
    @Setter
    private HashSet<UUID> players;

    public Gift(Location pos1, Location pos2) {
        this.players = new HashSet<>();
        this.pos1 = pos1;
        this.pos2 = pos2;
    }

    public boolean isFull() {
        return current >= capacity;
    }

    public boolean add(UUID uuid, Location loc) {
        if (isFull() || contains(uuid)) {
            return false;
        }

        players.add(uuid);
        current++;
        return true;
    }

    public void remove(UUID uuid) {
        if (contains(uuid)) {
            players.remove(uuid);
            current--;
        }
    }

    public boolean contains(UUID uuid) {
        return players.contains(uuid);
    }

    public void clear() {
        players.clear();
        current = 0;
    }

    public boolean isInArea(Location loc) {
        if (loc.getWorld() != pos1.getWorld()) {
            return false;
        }

        return loc.getX() >= Math.min(pos1.getX(), pos2.getX()) && loc.getX() <= Math.max(pos1.getX(), pos2.getX())
                && loc.getY() >= Math.min(pos1.getY(), pos2.getY()) && loc.getY() <= Math.max(pos1.getY(), pos2.getY())
                && loc.getZ() >= Math.min(pos1.getZ(), pos2.getZ()) && loc.getZ() <= Math.max(pos1.getZ(), pos2.getZ());
    }

}
