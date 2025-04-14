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

	public BalloonArmorModel(Location location, Location pos1, Location pos2) {
        this.location = location;
	}

    public void startTask() {
        task = Bukkit.getScheduler().runTaskTimer(Cl3vent.getInstance(), new Runnable() {
            private Location targetLocation = getRandomLocationInside();
            private double step = 0.1; // Movimiento suave (ajustar para mayor o menor suavidad)
            private double progress = 0.0;

            @Override
            public void run() {
                if (armorStand == null || armorStand.isDead()) {
                    removeArmorStand();
                    return;
                }

                if (progress >= 1.0) {
                    targetLocation = getRandomLocationInside();
                    progress = 0.0;
                }

                Location currentLocation = armorStand.getLocation();
                double newX = currentLocation.getX() + (targetLocation.getX() - currentLocation.getX()) * step;
                double newY = currentLocation.getY() + (targetLocation.getY() - currentLocation.getY()) * step;
                double newZ = currentLocation.getZ() + (targetLocation.getZ() - currentLocation.getZ()) * step;

                double randomYOffset = ThreadLocalRandom.current().nextDouble(-0.1, 0.1);
                newY += randomYOffset;

                armorStand.teleport(new Location(currentLocation.getWorld(), newX, newY, newZ));
                progress += step;
            }
        }, 0, 1);
    }

    private Location getRandomLocationInside() {
        double x = ThreadLocalRandom.current().nextDouble(pos1.getX(), pos2.getX());
        double y = ThreadLocalRandom.current().nextDouble(pos1.getY(), pos2.getY());
        double z = ThreadLocalRandom.current().nextDouble(pos1.getZ(), pos2.getZ());

        if (location.getWorld() == null) {
            return null;
        }

        if (location.getWorld().getBlockAt((int) x, (int) y, (int) z).getType() != Material.AIR) {
            return getRandomLocationInside();
        }

        return new Location(location.getWorld(), x, y, z);
    }

    public void removeArmorStand() {
        if (task != null) {
            task.cancel();
            task = null;
        }

        if (armorStand != null) {
            armorStand.remove();
        }

    }

    public void buildArmorStand() {
        removeArmorStand();

        armorStand = (ArmorStand) location.getWorld().spawn(location, ArmorStand.class);
        armorStand.setVisible(false);
        armorStand.setGravity(false);
        armorStand.setCustomNameVisible(false);
        armorStand.setMarker(true);
        armorStand.setSmall(true);
        armorStand.setBasePlate(false);
        armorStand.setCollidable(false);

        ItemStack balloon = ItemBuilder.builder()
                .material(Material.LEATHER_CHESTPLATE)
                .displayName("<gold>BALLOON")
                .customModelData(1)
                .build();

        armorStand.getEquipment().setHelmet(balloon);

    }

}

