package me.josielcm.jcm.api.utils;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;

public class EntityUtils {

    public Set<Entity> getEntitiesAt(Location location, int range) {
        return getEntitiesAt(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ(), range);
    }

    public Set<Entity> getEntitiesAt(World world, double x, double y, double z, int range) {
        Set<Entity> entities = new HashSet<>();

        int chunkX = (int) x >> 4;
        int chunkZ = (int) z >> 4;
        int chunkRadius = (int) Math.ceil(range / 16.0);

        Location location = new Location(world, x, y, z);

        for (int cx = chunkX - chunkRadius; cx <= chunkX + chunkRadius; cx++) {
            for (int cz = chunkZ - chunkRadius; cz <= chunkZ + chunkRadius; cz++) {
                Chunk chunk = world.getChunkAt(cx, cz);
                for (Entity entity : chunk.getEntities()) {
                    if (entity.getLocation().distanceSquared(location) <= range * range) {
                        entities.add(entity);
                    }
                }
            }
        }
        return entities;
    }
    
}
