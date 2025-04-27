package me.josielcm.event.manager.games.balloonshooting;

import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

import me.josielcm.event.Cl3vent;

public class BalloonShootingEvents implements Listener {

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.HIGH)
    public void onBalloonShoot(ProjectileHitEvent ev) {
        if (!(ev.getEntity() instanceof Arrow)) {
            Bukkit.broadcastMessage("§c¡No es una flecha válida por que: " + ev.getEntity().getType() + "!");
            return;
        }

        if (!(ev.getHitEntity() instanceof ArmorStand) ||
                !(ev.getEntity().getShooter() instanceof Player)) {
            Bukkit.broadcastMessage("§c¡No es un globo válido por que: " + ev.getHitEntity().getType() + " o "
                    + ev.getEntity().getShooter().toString() + "!");
            return;
        } else {
            Bukkit.broadcastMessage("§a¡Es un globo válido por que: " + ev.getHitEntity().getType() + " y "
                    + ev.getEntity().getShooter().toString() + "!");
        }

        Player player = (Player) ev.getEntity().getShooter();
        ArmorStand armorStand = (ArmorStand) ev.getHitEntity();
        BalloonShooting balloonShooting = Cl3vent.getInstance().getEventManager().getBalloonShooting();

        if (balloonShooting.isBalloon(armorStand)) {
            balloonShooting.addPoint(player.getUniqueId());
            balloonShooting.removeBalloon(armorStand);
            player.sendMessage("§a¡Has disparado un globo! +1 punto");
        } else {
            player.sendMessage("§c¡No es un globo válido!");
        }
    }

}
