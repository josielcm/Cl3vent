package me.josielcm.event.manager.games.giantgift;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import me.josielcm.event.Cl3vent;
import me.josielcm.event.manager.EventManager;

public class GiantGiftEvents implements Listener {

    @EventHandler
    private void onEnterGift(PlayerMoveEvent ev) {
        if (ev.getTo() == null || !hasMoved(ev)) {
            return;
        }

        if (ev.getPlayer().getGameMode() == org.bukkit.GameMode.SPECTATOR || ev.getPlayer().hasPermission("cl3vent.bypass")) {
            return;
        }

        Cl3vent plugin = Cl3vent.getInstance();
        EventManager eventManager = plugin.getEventManager();
        
        Location loc = ev.getTo().clone();

        if (eventManager.getGiantGift().isGift(loc)) {
            if (eventManager.getGiantGift().isSafePlayer(ev.getPlayer().getUniqueId())) {
                return;
            }

            if (!eventManager.getGiantGift().isAvailable()) {
                ev.getPlayer().sendRichMessage("<red>Aún no se puede entrar a los regalos.");
                ev.getPlayer().teleport(eventManager.getGiantGift().getSpawn());
                return;
            }

            eventManager.getGiantGift().safePlayer(ev.getPlayer().getUniqueId(), loc);
        }

    }

    private boolean hasMoved(PlayerMoveEvent ev) {
        return ev.getFrom().getBlockX() != ev.getTo().getBlockX()
                || ev.getFrom().getBlockY() != ev.getTo().getBlockY()
                || ev.getFrom().getBlockZ() != ev.getTo().getBlockZ();
    }
    
}
