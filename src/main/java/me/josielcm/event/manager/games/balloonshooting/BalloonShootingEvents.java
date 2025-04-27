package me.josielcm.event.manager.games.balloonshooting;

import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

import me.josielcm.event.Cl3vent;
import me.josielcm.event.manager.games.GameType;

public class BalloonShootingEvents implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onBalloonShoot(ProjectileHitEvent ev) {
        if (!(ev.getEntity() instanceof Arrow))
            return;
        if (!Cl3vent.getInstance().getEventManager().isInGame() ||
                Cl3vent.getInstance().getEventManager().getActualGame() != GameType.BALLONSHOOTING)
            return;
        if (ev.getHitEntity() == null || !(ev.getHitEntity() instanceof ArmorStand)
                || !(ev.getEntity().getShooter() instanceof Player p))
            return;

        BalloonShooting balloonShooting = Cl3vent.getInstance().getEventManager().getBalloonShooting();
        ArmorStand armorStand = (ArmorStand) ev.getHitEntity();

        if (balloonShooting.isBalloon(armorStand)) {
            balloonShooting.addPoint(p.getUniqueId());
            balloonShooting.removeBalloon(armorStand);
            p.sendRichMessage("<green>Has disparado un globo! +1 punto");
        } else {
            p.sendRichMessage("<red>Este no es un globo.");
        }

    }

}
