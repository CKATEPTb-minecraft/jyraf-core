package dev.ckateptb.minecraft.jyraf.packet.entity.property.glow;

import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import dev.ckateptb.minecraft.jyraf.packet.entity.PacketEntity;
import dev.ckateptb.minecraft.jyraf.packet.entity.enums.team.TeamColor;
import dev.ckateptb.minecraft.jyraf.packet.entity.property.PacketEntityProperty;
import org.bukkit.entity.Player;

import java.util.Map;

public class GlowProperty extends PacketEntityProperty<TeamColor> {
    public GlowProperty() {
        super("glow", null, TeamColor.class);
    }

    @Override
    public void apply(Player player, PacketEntity entity, boolean isSpawned, Map<Integer, EntityData> properties) {
        TeamColor value = entity.getProperty(this);
        EntityData oldData = properties.get(0);
        byte oldValue = oldData == null ? 0 : (byte) oldData.getValue();
        properties.put(0, newEntityData(0, EntityDataTypes.BYTE, (byte) (oldValue | (value == null ? 0 : 0x40))));
        if (isSpawned) entity.removeTeam(player);
        entity.setTeam(player, value);
    }
}
