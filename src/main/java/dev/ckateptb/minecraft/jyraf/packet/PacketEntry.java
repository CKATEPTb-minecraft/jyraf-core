package dev.ckateptb.minecraft.jyraf.packet;

import dev.ckateptb.minecraft.jyraf.packet.trait.PacketTrait;

import java.util.*;

public class PacketEntry {

    protected final Set<PacketTrait<? extends PacketEntry>> traits = Collections.synchronizedSet(new HashSet<>());

    public <T extends PacketEntry> void addTrait(PacketTrait<T> trait) {
        this.traits.add(trait);
    }

    public Collection<PacketTrait<?>> getTraits() {
        ArrayList<PacketTrait<?>> traits = new ArrayList<>(this.traits);
        traits.sort(Comparator.comparingInt((PacketTrait<?> trait) -> trait.getPriority().getSlot()).reversed());
        return traits;
    }

}