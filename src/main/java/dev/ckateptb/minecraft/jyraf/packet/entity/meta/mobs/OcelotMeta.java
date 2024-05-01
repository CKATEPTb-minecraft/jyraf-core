package dev.ckateptb.minecraft.jyraf.packet.entity.meta.mobs;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import dev.ckateptb.minecraft.jyraf.packet.entity.meta.Metadata;
import dev.ckateptb.minecraft.jyraf.packet.entity.meta.types.AgeableMeta;

public class OcelotMeta extends AgeableMeta {

    public static final byte OFFSET = AgeableMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 1;

    public OcelotMeta(int entityId, Metadata metadata) {
        super(entityId, metadata);
    }

    public boolean isTrusting() {
        isVersionNewer(ServerVersion.V_1_14);
        return super.metadata.getIndex(OFFSET, false);
    }

    public void setTrusting(boolean value) {
        isVersionNewer(ServerVersion.V_1_14);
        super.metadata.setIndex(OFFSET, EntityDataTypes.BOOLEAN, value);
    }

}
