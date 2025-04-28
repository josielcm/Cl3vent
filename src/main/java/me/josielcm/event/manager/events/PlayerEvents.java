package me.josielcm.event.manager.events;

import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import me.josielcm.event.Cl3vent;

public class PlayerEvents implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent ev) {
        Cl3vent.getInstance().getEventManager().getPlayers().add(ev.getPlayer().getUniqueId());
        Cl3vent.getInstance().getEventManager().getAllPlayers().add(ev.getPlayer().getUniqueId());

        if (Cl3vent.getInstance().getEventManager().getSpawn() != null) {
            ev.getPlayer().teleport(Cl3vent.getInstance().getEventManager().getSpawn());
            ev.getPlayer().setGameMode(GameMode.ADVENTURE);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent ev) {
        Cl3vent.getInstance().getEventManager().getPlayers().remove(ev.getPlayer().getUniqueId());
        Cl3vent.getInstance().getEventManager().getAllPlayers().remove(ev.getPlayer().getUniqueId());

        switch (Cl3vent.getInstance().getEventManager().getActualGame()) {
            case CAKEFEVER:
                Cl3vent.getInstance().getEventManager().getCakeFever().getPoints().remove(ev.getPlayer().getUniqueId());
                break;
        
            default:
                break;
        }
    }
    
}
