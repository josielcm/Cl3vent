package me.josielcm.event.manager.games.balloonshooting;

import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

import me.josielcm.event.Cl3vent;

public class BalloonShootingEvents implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onBalloonShoot(ProjectileHitEvent ev) {
        if (!(ev.getEntity() instanceof Arrow)) return;
        if (!(ev.getHitEntity() instanceof ArmorStand) || !(ev.getEntity().getShooter() instanceof Player)) return;

        Player player = (Player) ev.getEntity().getShooter();
        ArmorStand armorStand = (ArmorStand) ev.getHitEntity();
        BalloonShooting balloonShooting = Cl3vent.getInstance().getEventManager().getBalloonShooting();

        if (balloonShooting.isBalloon(armorStand)) {
            ev.getEntity().remove();
            balloonShooting.addPoint(player.getUniqueId());
            balloonShooting.removeBalloon(armorStand);
            player.sendMessage("§a+1");
        }
    }

}
