package me.josielcm.jcm.api.regions.selector;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.World;

import lombok.Getter;
import lombok.Setter;
import me.josielcm.jcm.api.regions.Cuboid;

public class SelectorData {

    @Getter
    @Setter
    private int id = 0;

    @Getter
    @Setter
    private String name = "NONE";

    @Getter
    @Setter
    private World world;

    @Getter
    @Setter
    private Location pos1;

    @Getter
    @Setter
    private Location pos2;

    @Getter
    @Setter
    private Location pos3;

    @Getter
    @Setter
    private Location pos4;

    @Getter
    @Setter
    private Cuboid cuboid;

    @Getter
    @Setter
    private UUID owner;

    public SelectorData(UUID owner) {
        this.owner = owner;
    }

    public boolean createCuboid() {
        if (pos1 == null || pos2 == null) return false;

        this.cuboid = new Cuboid(pos1, pos2);
        return true;
    }

    public boolean hasCuboid() {
        return cuboid != null;
    }

    public void updateCuboid() {
        this.cuboid = new Cuboid(pos1, pos2);
    }

}
