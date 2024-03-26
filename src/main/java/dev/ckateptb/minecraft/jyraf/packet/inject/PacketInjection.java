package dev.ckateptb.minecraft.jyraf.packet.inject;

import com.github.retrooper.packetevents.event.PacketListenerCommon;
import dev.ckateptb.minecraft.jyraf.Jyraf;
import dev.ckateptb.minecraft.jyraf.container.api.Container;
import dev.ckateptb.minecraft.jyraf.container.handler.ComponentRegisterHandler;
import dev.ckateptb.minecraft.jyraf.container.handler.ContainerInitializeHandler;
import lombok.SneakyThrows;
import org.bukkit.plugin.Plugin;

public class PacketInjection implements ComponentRegisterHandler, ContainerInitializeHandler {

    @SneakyThrows
    @Override
    public void handle(Object component, String qualifier, Plugin owner) {
        if (!(component instanceof PacketListenerCommon listener)) return;
        Jyraf.getPlugin().getPacketApi().getEventManager().registerListener(listener);
    }

    @Override
    public void handle(Container container, Long count) {
    }
}