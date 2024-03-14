package dev.ckateptb.minecraft.jyraf.packet.entity.property.villager;


import com.github.retrooper.packetevents.protocol.entity.villager.VillagerData;
import dev.ckateptb.minecraft.jyraf.packet.entity.enums.villager.VillagerLevel;

public class VillagerLevelProperty extends VillagerDataProperty<VillagerLevel> {
    public VillagerLevelProperty(String name, int index, VillagerLevel def) {
        super(name, index, def);
    }

    @Override
    protected VillagerData apply(VillagerData data, VillagerLevel value) {
        data.setLevel(value.ordinal() + 1);
        return data;
    }
}