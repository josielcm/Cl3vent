package me.josielcm.event.manager.games.balloonshooting;

import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import lombok.Getter;
import me.josielcm.event.Cl3vent;
import me.josielcm.event.api.formats.Color;
import me.josielcm.event.api.items.ItemBuilder;

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

    public BalloonArmorModel(Location pos1, Location pos2) {
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.location = getRandomLocationInside();
        buildArmorStand();
    }

    public void startTask() {
        task = Bukkit.getScheduler().runTaskTimer(Cl3vent.getInstance(), new Runnable() {
            private Location targetLocation = getRandomLocationInside();
            private double step = 0.02;
            private double progress = 0.0;

            @Override
            public void run() {
                if (armorStand == null || armorStand.isDead()) {
                    removeArmorStand();
                    return;
                }

                Location currentLocation = armorStand.getLocation();

                double newX = currentLocation.getX() + (targetLocation.getX() - currentLocation.getX()) * step;
                double newY = currentLocation.getY() + (targetLocation.getY() - currentLocation.getY()) * step;
                double newZ = currentLocation.getZ() + (targetLocation.getZ() - currentLocation.getZ()) * step;

                double randomYOffset = ThreadLocalRandom.current().nextDouble(-0.1, 0.1);
                newY += randomYOffset;

                armorStand.teleport(new Location(currentLocation.getWorld(), newX, newY, newZ));

                progress += step;

                if (progress >= 1.0) {
                    targetLocation = getRandomLocationInside();
                    progress = 0.0;
                }
            }
        }, 0L, 1L);
    }

    private Location getRandomLocationInside() {
        if (pos1 == null || pos2 == null) {
            throw new IllegalStateException("Invalid positions or world is null.");
        }

        int maxAttempts = 100;
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            double x = ThreadLocalRandom.current().nextDouble(pos1.getX(), pos2.getX());
            double y = ThreadLocalRandom.current().nextDouble(pos1.getY(), pos2.getY());
            double z = ThreadLocalRandom.current().nextDouble(pos1.getZ(), pos2.getZ());

            Location randomLocation = new Location(pos1.getWorld(), x, y, z);

            if (randomLocation.getWorld().getBlockAt(randomLocation).getType() == Material.AIR) {
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
        armorStand.setVisible(true);
        armorStand.setGravity(false);
        armorStand.setCustomNameVisible(true);
        armorStand.setBasePlate(false);
        armorStand.customName(Color.parse("<gold>Balloon"));

        ItemStack balloon = ItemBuilder.builder()
                .material(Material.LEATHER_CHESTPLATE)
                .displayName("<gold>BALLOON")
                .customModelData(1)
                .build();

        armorStand.getEquipment().setHelmet(balloon);

        startTask();
    }

}
