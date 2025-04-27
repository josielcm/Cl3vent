package me.josielcm.event.manager.games.cakefever;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import me.josielcm.event.Cl3vent;
import me.josielcm.event.manager.games.GameType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CakeFeverEvent implements Listener {

    private final Map<UUID, Long> interactCooldowns = new HashMap<>();
    private static final long INTERACT_COOLDOWN = 200;

    @EventHandler(priority = EventPriority.HIGH)
    public void onCake(PlayerInteractEvent ev) {
        if (ev.getClickedBlock() == null || ev.getClickedBlock().getType() != Material.CAKE)
            return;

        if (!Cl3vent.getInstance().getEventManager().isInGame() ||
            Cl3vent.getInstance().getEventManager().getActualGame() != GameType.CAKEFEVER)
            return;

        Player player = ev.getPlayer();
        UUID playerId = player.getUniqueId();

        long now = System.currentTimeMillis();
        if (interactCooldowns.containsKey(playerId) && 
            now - interactCooldowns.get(playerId) < INTERACT_COOLDOWN) {
            ev.setCancelled(true);
            return;
        }
        interactCooldowns.put(playerId, now);

        CakeFever cakeFever = Cl3vent.getInstance().getEventManager().getCakeFever();
        if (cakeFever.isCakeLocation(ev.getClickedBlock().getLocation())) {
            ev.setCancelled(true);
            cakeFever.randomPoint(player);
            ev.getClickedBlock().setType(Material.AIR);
        }
    }

}
