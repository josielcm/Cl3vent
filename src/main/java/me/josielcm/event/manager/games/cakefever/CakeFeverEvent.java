package me.josielcm.event.manager.games.cakefever;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import me.josielcm.event.Cl3vent;
import me.josielcm.event.manager.games.GameType;

public class CakeFeverEvent implements Listener {

    @EventHandler
    public void onCake(PlayerInteractEvent ev) {
        if (ev.getClickedBlock() == null) return;
        if (ev.getClickedBlock().getType() != Material.CAKE);
        if (!Cl3vent.getInstance().getEventManager().isInGame()) return;
        if (Cl3vent.getInstance().getEventManager().getActualGame() != GameType.CAKEFEVER) return;

        if (Cl3vent.getInstance().getEventManager().getCakeFever().getCakes().contains(ev.getClickedBlock().getLocation())) {
            Cl3vent.getInstance().getEventManager().getCakeFever().randomPoint(ev.getPlayer());
        }

    }
    
}
