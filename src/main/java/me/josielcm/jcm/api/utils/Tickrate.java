package me.josielcm.jcm.api.utils;

import java.lang.reflect.Method;
import java.util.logging.Level;

import org.bukkit.entity.Player;

import me.josielcm.jcm.Base;

/*
 * Change player tick rate but only works in 1.21.1
 * 
 * @version 1.21.1
 */
public class Tickrate {
    
    /**
     * Set player tick rate
     * require v1.21.1 (PAPER)
     * 
     * @param player
     * @param tickRate
     * 
     * @version 1.21.1
     * @author JosielCM
     */
    public static void setPlayerTickRate(Player player, float tickRate) {
        try {
            Method getHandleMethod = player.getClass().getMethod("getHandle");
            Object serverPlayer = getHandleMethod.invoke(player);
            Object connection = serverPlayer.getClass().getField("connection").get(serverPlayer);

            Class<?> ClientBoundTickingStatePacket = Class.forName("net.minecraft.network.protocol.game.ClientboundTickingStatePacket");
            Object packet = ClientBoundTickingStatePacket.getConstructor(float.class, boolean.class).newInstance(tickRate, false);

            Method sendPacket = connection.getClass().getMethod("send", Class.forName("net.minecraft.network.protocol.Packet"));
            sendPacket.invoke(connection, packet);

        } catch (Exception e) {
            Base.getInstance().getLogger().log(Level.SEVERE, "Error setting tick rate", e);
        }

    }
}
