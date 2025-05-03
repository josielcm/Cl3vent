package me.josielcm.event.manager.events;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import me.josielcm.event.Cl3vent;
import me.josielcm.event.manager.EventManager;

public class PlayerEvents implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent ev) {
        Cl3vent.getInstance().getEventManager().getAllPlayers().add(ev.getPlayer().getUniqueId());

        if (!ev.getPlayer().hasPermission("cl3vent.bypass")) {
            Cl3vent.getInstance().getEventManager().getPlayers().add(ev.getPlayer().getUniqueId());
        }

        if (Cl3vent.getInstance().getEventManager().getSpawn() != null) {
            ev.getPlayer().teleport(Cl3vent.getInstance().getEventManager().getSpawn());
            ev.getPlayer().setGameMode(GameMode.ADVENTURE);
            ev.getPlayer().getInventory().clear();
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent ev) {
        Player player = ev.getPlayer();
        EventManager eventManager = Cl3vent.getInstance().getEventManager();

        eventManager.getPlayers().remove(player.getUniqueId());
        eventManager.getAllPlayers().remove(player.getUniqueId());

        switch (Cl3vent.getInstance().getEventManager().getActualGame()) {
            case CAKEFEVER:
                eventManager.getCakeFever().getPoints().remove(player.getUniqueId());
                break;
            case BALLOONPARKOUR:
                eventManager.getBalloonParkour().getPlayers().remove(player.getUniqueId());
                break;
            case BALLONSHOOTING:
                eventManager.getBalloonShooting().getPoints().remove(player.getUniqueId());
                break;
            case GIANTGIFT:
                eventManager.getGiantGift().getPlayers().remove(player.getUniqueId());
                break;
            default:
                break;
        }
    }

}
