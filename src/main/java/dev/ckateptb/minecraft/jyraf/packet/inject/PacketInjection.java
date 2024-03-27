package dev.ckateptb.minecraft.jyraf.packet.inject;

import com.github.retrooper.packetevents.event.PacketListenerCommon;
import dev.ckateptb.minecraft.jyraf.Jyraf;
import dev.ckateptb.minecraft.jyraf.container.handler.ComponentRegisterHandler;
import lombok.SneakyThrows;
import org.bukkit.plugin.Plugin;

public class PacketInjection implements ComponentRegisterHandler {

    @SneakyThrows
    @Override
    public void handle(Object component, String qualifier, Plugin owner) {
        if (!(component instanceof PacketListenerCommon listener)) return;
        Jyraf.getPlugin().getPacketApi().getEventManager().registerListener(listener);
    }

}