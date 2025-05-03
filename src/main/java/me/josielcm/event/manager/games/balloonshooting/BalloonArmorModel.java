package me.josielcm.event.manager.games.balloonshooting;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scheduler.BukkitTask;

import lombok.Getter;
import lombok.Setter;
import me.josielcm.event.Cl3vent;

public class BalloonArmorModel {

    @Getter
    private ArmorStand armorStand;

    @Getter
    private Location location;

    @Getter
    private Location pos1;

    @Getter
    private Location pos2;

    @Getter
    private BukkitTask task;

    @Getter
    private boolean isGold = false;

    @Getter
    @Setter
    private double step = 0.02;

    public BalloonArmorModel(Location pos1, Location pos2) {
        if (pos1 == null || pos2 == null) {
            throw new IllegalArgumentException("Positions cannot be null");
        }
        if (pos1.getWorld() == null || pos2.getWorld() == null || !pos1.getWorld().equals(pos2.getWorld())) {
            throw new IllegalArgumentException("Positions must be in the same world");
        }

        this.pos1 = pos1.clone();
        this.pos2 = pos2.clone();
        this.location = getRandomLocationInside();
        buildArmorStand();
    }

    public void startTask() {
        if (task != null) {
            task.cancel();
            task = null;
        }

        if (armorStand == null || armorStand.isDead()) {
            removeArmorStand();
            return;
        }

        if (isGold) {
            step = 0.05;
        }

        task = Bukkit.getScheduler().runTaskTimer(Cl3vent.getInstance(), new Runnable() {
            private Location targetLocation = getRandomLocationInside();
            private double progress = 0.0;
            private int movementCounter = 0;

            @Override
            public void run() {
                if (armorStand == null || armorStand.isDead()) {
                    removeArmorStand();
                    return;
                }

                Location currentLocation = armorStand.getLocation();

                if (progress >= 1.0 || movementCounter >= 100) {
                    targetLocation = getRandomLocationInside();
                    progress = 0.0;
                    movementCounter = 0;
                    
                }

                double newX = currentLocation.getX() + (targetLocation.getX() - currentLocation.getX()) * step;
                double newY = currentLocation.getY() + (targetLocation.getY() - currentLocation.getY()) * step;
                double newZ = currentLocation.getZ() + (targetLocation.getZ() - currentLocation.getZ()) * step;

                double amplitude = 0.2; 
                double frequency = 0.1;
                double randomYOffset = Math.sin(progress * Math.PI * 2 * frequency) * amplitude;
                newY += randomYOffset;

                double randomXOffset = ThreadLocalRandom.current().nextDouble(-0.05, 0.05);
                double randomZOffset = ThreadLocalRandom.current().nextDouble(-0.05, 0.05);
                newX += randomXOffset;
                newZ += randomZOffset;

                newX = Math.min(Math.max(newX, Math.min(pos1.getX(), pos2.getX())), Math.max(pos1.getX(), pos2.getX()));
                newY = Math.min(Math.max(newY, Math.min(pos1.getY(), pos2.getY())), Math.max(pos1.getY(), pos2.getY()));
                newZ = Math.min(Math.max(newZ, Math.min(pos1.getZ(), pos2.getZ())), Math.max(pos1.getZ(), pos2.getZ()));

                armorStand.teleport(new Location(currentLocation.getWorld(), newX, newY, newZ));

                progress += step;
                movementCounter++;
            }
        }, 0L, 1L);
    }

    private Location getRandomLocationInside() {
        if (pos1 == null || pos2 == null) {
            throw new IllegalStateException("Invalid positions or world is null.");
        }

        int maxAttempts = 100;
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            double minX = Math.min(pos1.getX(), pos2.getX());
            double maxX = Math.max(pos1.getX(), pos2.getX());
            double minY = Math.min(pos1.getY(), pos2.getY());
            double maxY = Math.max(pos1.getY(), pos2.getY());
            double minZ = Math.min(pos1.getZ(), pos2.getZ());
            double maxZ = Math.max(pos1.getZ(), pos2.getZ());

            double x = ThreadLocalRandom.current().nextDouble(minX, maxX);
            double y = ThreadLocalRandom.current().nextDouble(minY, maxY);
            double z = ThreadLocalRandom.current().nextDouble(minZ, maxZ);

            Location randomLocation = new Location(pos1.getWorld(), x, y, z);

            if (randomLocation.getBlock().getType() == Material.AIR) {
                return randomLocation;
            }
        }

        throw new IllegalStateException(
                "Could not find a valid air block within the specified range after " + maxAttempts + " attempts.");
    }

    public void removeArmorStand() {
        if (task != null) {
            task.cancel();
            task = null;
        }

        if (armorStand != null && !armorStand.isDead()) {
            armorStand.remove();
            armorStand = null;
        }
    }

    public void buildArmorStand() {
        removeArmorStand();

        armorStand = (ArmorStand) location.getWorld().spawn(location, ArmorStand.class);
        armorStand.setVisible(false);
        armorStand.setGravity(false);
        armorStand.setCustomNameVisible(false);
        armorStand.setBasePlate(false);
        armorStand.setGlowing(true);
        
        ItemStack balloonTextured = new ItemStack(Material.LEATHER_HORSE_ARMOR);
        LeatherArmorMeta meta = (LeatherArmorMeta) balloonTextured.getItemMeta();

        if (meta != null) {
            meta.setCustomModelData(10002);

            List<Color> colors = new ArrayList<>();

            colors.add(Color.fromRGB(93, 226, 231));
            colors.add(Color.fromRGB(125, 218, 88));
            colors.add(Color.fromRGB(228, 8, 10));
            colors.add(Color.fromRGB(255, 1, 149));
            colors.add(Color.fromRGB(92, 246, 200));
            colors.add(Color.fromRGB(239, 195, 202));
            colors.add(Color.fromRGB(60, 80, 252));
            colors.add(Color.fromRGB(255, 215, 0)); // GOLD

            Color randomColor = colors.get(ThreadLocalRandom.current().nextInt(colors.size()));

            if (randomColor.asRGB() == Color.fromRGB(255, 215, 0).asRGB()) {
                isGold = true;
            }

            meta.setColor(randomColor);

            balloonTextured.setItemMeta(meta);
        }

        armorStand.getEquipment().setHelmet(balloonTextured);

        startTask();
    }

}
