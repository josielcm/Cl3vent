package me.josielcm.event.api.regions;

import org.bukkit.Location;

import lombok.Getter;
import lombok.Setter;

public class Container {

    @Getter
    @Setter
    Location pos1;

    @Getter
    @Setter
    Location pos2;

    public Container(Location pos1, Location pos2) {
        this.pos1 = pos1;
        this.pos2 = pos2;
    }

    public boolean isInside(Location loc) {
        if (loc.getWorld() != pos1.getWorld()) return false;
        if (loc.getX() < Math.min(pos1.getX(), pos2.getX())) return false;
        if (loc.getX() > Math.max(pos1.getX(), pos2.getX())) return false;
        if (loc.getY() < Math.min(pos1.getY(), pos2.getY())) return false;
        if (loc.getY() > Math.max(pos1.getY(), pos2.getY())) return false;
        if (loc.getZ() < Math.min(pos1.getZ(), pos2.getZ())) return false;
        if (loc.getZ() > Math.max(pos1.getZ(), pos2.getZ())) return false;
        return true;
    }


}
