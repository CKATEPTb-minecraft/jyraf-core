package dev.ckateptb.minecraft.jyraf.packet.entity.property.villager;

import com.github.retrooper.packetevents.protocol.entity.villager.VillagerData;
import com.github.retrooper.packetevents.protocol.entity.villager.type.VillagerTypes;
import dev.ckateptb.minecraft.jyraf.packet.entity.enums.villager.VillagerType;

public class VillagerTypeProperty extends VillagerDataProperty<VillagerType> {
    public VillagerTypeProperty(String name, int index, VillagerType def) {
        super(name, index, def);
    }

    @Override
    protected VillagerData apply(VillagerData data, VillagerType value) {
        data.setType(VillagerTypes.getById(value.getId()));
        return data;
    }
}
