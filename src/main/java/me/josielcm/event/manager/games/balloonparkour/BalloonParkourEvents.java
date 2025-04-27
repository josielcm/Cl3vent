package me.josielcm.event.manager.games.balloonparkour;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import me.josielcm.event.Cl3vent;
import me.josielcm.event.api.Key;

public class BalloonParkourEvents implements Listener {

    private final Set<UUID> playersInSafeZone = new HashSet<>();

    private BalloonParkour getBalloonParkour() {
        return Cl3vent.getInstance().getEventManager().getBalloonParkour();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMove(PlayerMoveEvent ev) {

        Player player = ev.getPlayer();
        UUID playerId = player.getUniqueId();
        BalloonParkour balloonParkour = getBalloonParkour();

        Bukkit.getLogger().info("§eProcesando movimiento para: " + player.getName());

        if (player.getWorld() != balloonParkour.getWorld()) // player.hasPermission("cl3vent.bypass") ||
            return;

        Location to = ev.getTo();
        Location from = ev.getFrom();
        if (to.getBlockX() == from.getBlockX() &&
                to.getBlockY() == from.getBlockY() &&
                to.getBlockZ() == from.getBlockZ())
            return;

        Location loc = to;

        if (balloonParkour.getSafeContainer() != null &&
                balloonParkour.getSafeContainer().isInside(loc)) {
            if (!playersInSafeZone.contains(playerId)) {
                playersInSafeZone.add(playerId);
                balloonParkour.getNoElimination().add(playerId);
                player.sendRichMessage("<green>¡Has llegado a la zona final! ¡Estás a salvo!");

                int lastCheckpoint = balloonParkour.getCheckpoints().size() - 1;
                int playerCheckpoint = getPlayerCheckpoint(player);
                if (playerCheckpoint == lastCheckpoint) {
                    player.sendRichMessage("<gold>¡Felicidades! ¡Has completado el parkour!");
                }

                player.getInventory().clear();
            }
            return;
        }

        if (isInCheckpointArea(loc)) {
            int checkpoint = getCheckpoint(loc);
            if (checkpoint != -1) {
                int currentCheckpoint = getPlayerCheckpoint(player);
                // Solo actualizar si es el siguiente checkpoint
                if (checkpoint == currentCheckpoint + 1) {
                    balloonParkour.reachCheckpoint(player, checkpoint);
                }
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent ev) {
        Player player = ev.getPlayer();

        // Corregir la condición de acción
        if (ev.getAction() != Action.RIGHT_CLICK_BLOCK && ev.getAction() != Action.RIGHT_CLICK_AIR) // player.hasPermission("cl3vent.bypass")
                                                                                                    // ||
            return;
        if (ev.getItem() == null || !ev.getItem().hasItemMeta())
            return;

        ItemStack item = ev.getItem();
        PersistentDataContainer data = item.getItemMeta().getPersistentDataContainer();
        if (!data.has(Key.getParkourItemsKey()))
            return;

        switch (data.get(Key.getParkourItemsKey(), PersistentDataType.STRING)) {
            case "checkpoint":
                if (!playersInSafeZone.contains(player.getUniqueId())) {
                    int checkpoint = getPlayerCheckpoint(player);
                    if (checkpoint == -1) {
                        player.teleport(getBalloonParkour().getSpawn());
                        return;
                    }

                    Location checkLocation = getCheckpointLocation(checkpoint);
                    if (checkLocation == null) {
                        player.teleport(getBalloonParkour().getSpawn());
                        return;
                    }

                    player.teleport(checkLocation);
                    player.sendRichMessage("<grey>Regresando al checkpoint <green>" + checkpoint + "<grey>.");

                    break;
                } else {
                    player.sendRichMessage("<grey>Ya has completado el parkour.");
                }
            case "toggle-visibility":
                boolean visible = getBalloonParkour().getVisibility()
                        .get(player.getUniqueId());

                getBalloonParkour().updatePlayerVisibility(player, !visible);
                break;
            case "impulse":
                if (getBalloonParkour().getPlayers()
                        .containsKey(player.getUniqueId())) {
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

    // Optimizar la detección de área de checkpoint
    private boolean isInCheckpointArea(Location location) {
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        for (Location checkpoint : getBalloonParkour().getCheckpoints().values()) {
            if (Math.abs(checkpoint.getBlockX() - x) <= 1 &&
                    Math.abs(checkpoint.getBlockY() - y) <= 1 &&
                    Math.abs(checkpoint.getBlockZ() - z) <= 1) {
                return true;
            }
        }
        return false;
    }

    // Optimizar la obtención del checkpoint exacto
    public int getCheckpoint(Location location) {
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        for (Map.Entry<Integer, Location> entry : getBalloonParkour().getCheckpoints().entrySet()) {
            Location checkpoint = entry.getValue();
            if (Math.abs(checkpoint.getBlockX() - x) <= 1 &&
                    Math.abs(checkpoint.getBlockY() - y) <= 1 &&
                    Math.abs(checkpoint.getBlockZ() - z) <= 1) {
                return entry.getKey();
            }
        }
        return -1;
    }

    public Location getCheckpointLocation(int checkpoint) {
        return getBalloonParkour().getCheckpoints().get(checkpoint);
    }

    public int getPlayerCheckpoint(Player player) {
        if (getBalloonParkour().getPlayers()
                .containsKey(player.getUniqueId())) {
            return getBalloonParkour().getPlayers().get(player.getUniqueId());
        }
        return -1;
    }

}
