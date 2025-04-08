package me.josielcm.jcm.api.regions.selector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import lombok.Getter;
import me.josielcm.jcm.Base;
import me.josielcm.jcm.api.Key;
import me.josielcm.jcm.api.items.ItemBuilder;

public class Selector implements Listener {

    @Getter
    private SelectorData data;

    private Map<UUID, ItemStack[]> items;

    public Selector(Player player, boolean confirmItem) {
        this.items = new HashMap<>();
        this.data = new SelectorData(player.getUniqueId());
        Base.getInstance().getServer().getPluginManager().registerEvents(this, Base.getInstance());

        player.sendRichMessage("<green>Selector mode activated!");
        enable(confirmItem);
    }

    public void enable(boolean confirmItem) {
        Player player = Bukkit.getPlayer(data.getOwner());

        if (player != null) {
            saveInventory(player);
            giveItems(player, confirmItem);
        }
    }

    public void disable() {
        HandlerList.unregisterAll(this);
        Player player = Bukkit.getPlayer(data.getOwner());
        data = null;

        if (player != null) {
            player.sendRichMessage("<red>Selector mode deactivated!");
            returnInventory(player);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent ev) {
        if (data == null && !ev.getPlayer().getUniqueId().equals(data.getOwner())) return;
        if (ev.getItem() == null || !ev.getItem().hasItemMeta() || ev.getItem().getItemMeta().getPersistentDataContainer() == null) return;
        if (ev.getItem().getItemMeta().getPersistentDataContainer().get(Key.getSelectorItemsKey(), PersistentDataType.STRING) == null) return;

        Player player = ev.getPlayer();
        PersistentDataContainer pdc = ev.getItem().getItemMeta().getPersistentDataContainer();
        String pdcType = pdc.get(Key.getSelectorItemsKey(), PersistentDataType.STRING);

        Action action = ev.getAction();
        Block block = ev.getClickedBlock();
        SelectorData data = this.data;
        if (data == null) return;
        if (block == null) return;

        switch (pdcType) {
            case "selector":
                switch (action) {
                    case LEFT_CLICK_BLOCK:
                        data.setPos1(block.getLocation());
                        player.sendRichMessage("<green>Position 1 set at <gold>x:<aqua>" + block.getX() + " <gold>y:<aqua>" + block.getY() + " <gold>z:<aqua>" + block.getZ());
                        break;
                    case RIGHT_CLICK_BLOCK:
                        data.setPos2(block.getLocation());
                        player.sendRichMessage("<green>Position 2 set at <gold>x:<aqua>" + block.getX() + " <gold>y:<aqua>" + block.getY() + " <gold>z:<aqua>" + block.getZ());
                        break;
                    default:
                        break;
                }
                break;
            case "confirm":
                break;
            default:
                break;
        }

    }

    public void giveItems(Player player, boolean confirmItem) {
        ItemStack selector = ItemBuilder.builder()
                .displayName("<aqua>Selector")
                .lore("<gray>Use this to select regions.")
                .pdc(Key.getSelectorItemsKey(), "selector")
                .build();

        ItemStack confirm = ItemBuilder.builder()
                .displayName("<green>Confirm Selection")
                .lore("<gray>Use this to confirm your selection.")
                .pdc(Key.getSelectorItemsKey(), "confirm")
                .build();

        player.getInventory().addItem(selector);
        if (confirmItem) player.getInventory().addItem(confirm);
    }

    public void saveInventory(Player player) {
        ItemStack[] playerItems = player.getInventory().getContents();
        items.put(player.getUniqueId(), playerItems);
        player.getInventory().clear();
    }

    public void returnInventory(Player player) {
        ItemStack[] playerItems = items.get(player.getUniqueId());
        if (playerItems == null)
            return;

        player.getInventory().clear();
        player.getInventory().setContents(playerItems);
        items.remove(player.getUniqueId());
    }

}
