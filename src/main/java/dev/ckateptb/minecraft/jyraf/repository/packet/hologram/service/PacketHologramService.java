package dev.ckateptb.minecraft.jyraf.repository.packet.hologram.service;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import dev.ckateptb.minecraft.jyraf.container.annotation.Component;

@Component
public class PacketHologramService extends PacketListenerAbstract {

    public PacketHologramService() {
        super(PacketListenerPriority.HIGHEST);
    }

}