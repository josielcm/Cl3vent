package me.josielcm.event.manager.games.balloonparkour;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import me.josielcm.event.Cl3vent;
import me.josielcm.event.api.Key;
import me.josielcm.event.manager.games.GameType;

public class BalloonParkourEvents implements Listener {

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent ev) {
        Player player = ev.getPlayer();
        if (!Cl3vent.getInstance().getEventManager().isInGame()) return;
        if (Cl3vent.getInstance().getEventManager().getActualGame() != GameType.BALLOONPARKOUR) return;
        if (player.hasPermission("cl3vent.bypass")) return;
        if (player.getWorld() != Cl3vent.getInstance().getEventManager().getBalloonParkour().getWorld()) return;
        if (!hasPlayerMoved(player, ev.getFrom(), ev.getTo())) return;

        if (isCheckpoint(ev.getTo())) {
            if (isNewCheckpoint(player, ev.getTo())) {
                Cl3vent.getInstance().getEventManager().getBalloonParkour().reachCheckpoint(player, getCheckpoint(ev.getTo()));
            }
        }

        if (Cl3vent.getInstance().getEventManager().getBalloonParkour().getSafeContainer() != null) {
            if (Cl3vent.getInstance().getEventManager().getBalloonParkour().getSafeContainer().isInside(player.getLocation())) {
                if (!Cl3vent.getInstance().getEventManager().getBalloonParkour().getNoElimination().contains(player.getUniqueId())) {
                    Cl3vent.getInstance().getEventManager().getBalloonParkour().getNoElimination().add(player.getUniqueId());
                    player.sendRichMessage("<green>Estas en la zona segura.");
                }
            }
        }

    }

    @EventHandler
    public void onInteract(PlayerInteractEvent ev) {
        Player player = ev.getPlayer();

        if (!Cl3vent.getInstance().getEventManager().isInGame()) return;
        if (Cl3vent.getInstance().getEventManager().getActualGame() != GameType.BALLOONPARKOUR) return;
        if (player.hasPermission("cl3vent.bypass")) return;
        if (player.getWorld() != Cl3vent.getInstance().getEventManager().getBalloonParkour().getWorld()) return;
        if (ev.getAction() != Action.RIGHT_CLICK_BLOCK || ev.getAction() != Action.RIGHT_CLICK_AIR) return;
        if (ev.getItem() == null) return;
        if (!ev.getItem().hasItemMeta()) return;

        ItemStack item = ev.getItem();
        PersistentDataContainer data = item.getItemMeta().getPersistentDataContainer();
        if (!data.has(Key.getParkourItemsKey())) return;

        switch (data.get(Key.getParkourItemsKey(), PersistentDataType.STRING)) {
            case "checkpoint":
                int checkpoint = getPlayerCheckpoint(player);
                if (checkpoint == -1) {
                    player.teleport(Cl3vent.getInstance().getEventManager().getBalloonParkour().getSpawn());
                    return;
                }
                
                Location checkLocation = getCheckpointLocation(checkpoint);
                if (checkLocation == null) {
                    player.teleport(Cl3vent.getInstance().getEventManager().getBalloonParkour().getSpawn());
                    return;
                }

                player.teleport(checkLocation);
                player.sendRichMessage("<grey>Regresando al checkpoint <green>" + checkpoint + "<grey>.");
                
                break;
            case "toggle-visibility":
                boolean visible = Cl3vent.getInstance().getEventManager().getBalloonParkour().getVisibility().get(player.getUniqueId());

                if (visible) {
                    Cl3vent.getInstance().getEventManager().getBalloonParkour().getVisibility().put(player.getUniqueId(), false);

                    for (UUID uuid : Cl3vent.getInstance().getEventManager().getPlayers()) {
                        Player p = Bukkit.getPlayer(uuid);
                        if (p != null && p != player && !p.hasPermission("cl3vent.bypass")) {
                            player.hidePlayer(Cl3vent.getInstance(), player);
                        }
                    }

                    player.sendRichMessage("<grey>Visibilidad de los jugadores fue " + "<red>desactivada<grey>.");
                } else {
                    Cl3vent.getInstance().getEventManager().getBalloonParkour().getVisibility().put(player.getUniqueId(), true);
                    for (UUID uuid : Cl3vent.getInstance().getEventManager().getPlayers()) {
                        Player p = Bukkit.getPlayer(uuid);
                        if (p != null && p != player && !p.hasPermission("cl3vent.bypass")) {
                            player.showPlayer(Cl3vent.getInstance(), player);
                        }
                    }
                    player.sendRichMessage("<grey>Visibilidad de los jugadores fue " + "<green>activada<grey>.");
                }  
                break;
            case "impulse":
                if (Cl3vent.getInstance().getEventManager().getBalloonParkour().getPlayers().containsKey(player.getUniqueId())) {
                    if (!player.hasCooldown(Material.FEATHER)) {
                        player.setVelocity(player.getLocation().getDirection().multiply(2).setY(1));
                        player.setCooldown(Material.FEATHER, 20 * 60);
                        player.sendRichMessage("<green>Impulso activado.");
                    } else {
                        player.sendRichMessage("<red>Debes esperar para usar el impulso.");
                    }
                }
                break;
            default:
                break;
        }



    }

    private boolean hasPlayerMoved(Player player, Location from, Location to) {
        return from.getBlockX() != to.getBlockX() || from.getBlockY() != to.getBlockY() || from.getBlockZ() != to.getBlockZ();
    }

    public boolean isCheckpoint(Location Location) {
        return Cl3vent.getInstance().getEventManager().getBalloonParkour().getCheckpoints().containsValue(Location);
    }

    public int getCheckpoint(Location location) {
        for (int i : Cl3vent.getInstance().getEventManager().getBalloonParkour().getCheckpoints().keySet()) {
            if (Cl3vent.getInstance().getEventManager().getBalloonParkour().getCheckpoints().get(i).equals(location)) {
                return i;
            }
        }
        return -1;
    }

    public Location getCheckpointLocation(int checkpoint) {
        return Cl3vent.getInstance().getEventManager().getBalloonParkour().getCheckpoints().get(checkpoint);
    }

    public int getPlayerCheckpoint(Player player) {
        if (Cl3vent.getInstance().getEventManager().getBalloonParkour().getPlayers().containsKey(player.getUniqueId())) {
            return Cl3vent.getInstance().getEventManager().getBalloonParkour().getPlayers().get(player.getUniqueId());
        }
        return -1;
    }

    public boolean isNewCheckpoint(Player player, Location location) {
        return isCheckpoint(location) && getPlayerCheckpoint(player) < getCheckpoint(location);
    }
    
}
