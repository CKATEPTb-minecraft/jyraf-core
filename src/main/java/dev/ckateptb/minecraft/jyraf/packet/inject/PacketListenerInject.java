package dev.ckateptb.minecraft.jyraf.packet.inject;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.EventManager;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketListenerCommon;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import dev.ckateptb.minecraft.jyraf.container.handler.ComponentRegisterHandler;
import org.bukkit.plugin.Plugin;

public class PacketListenerInject implements ComponentRegisterHandler {
    @Override
    public void handle(Object component, String qualifier, Plugin owner) {
        EventManager eventManager = PacketEvents.getAPI().getEventManager();
        if (component instanceof PacketListenerCommon listener) {
            eventManager.registerListener(listener);
        } else if (component instanceof PacketListener listener) {
            eventManager.registerListener(listener, PacketListenerPriority.NORMAL);
        }
    }
}
