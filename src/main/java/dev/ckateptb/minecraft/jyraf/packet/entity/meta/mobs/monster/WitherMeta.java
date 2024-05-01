package dev.ckateptb.minecraft.jyraf.packet.entity.meta.mobs.monster;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import dev.ckateptb.minecraft.jyraf.packet.entity.meta.Metadata;
import dev.ckateptb.minecraft.jyraf.packet.entity.meta.types.MobMeta;

public class WitherMeta extends MobMeta {

    public static final byte OFFSET = MobMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 4;

    private int centerHead = -1;
    private int leftHead = -1;
    private int rightHead = -1;

    public WitherMeta(int entityId, Metadata metadata) {
        super(entityId, metadata);
    }

    public int getCenterHead() {
        return centerHead;
    }

    public void setCenterHead(int centerHead) {
        this.centerHead = centerHead;
        super.metadata.setIndex(offset(OFFSET, 0), EntityDataTypes.INT, centerHead == -1 ? 0 : centerHead);
    }

    public int getLeftHead() {
        return leftHead;
    }

    public void setLeftHead(int leftHead) {
        this.leftHead = leftHead;
        super.metadata.setIndex(offset(OFFSET, 1), EntityDataTypes.INT, leftHead == -1 ? 0 : leftHead);
    }

    public int getRightHead() {
        return rightHead;
    }

    public void setRightHead(int rightHead) {
        this.rightHead = rightHead;
        super.metadata.setIndex(offset(OFFSET, 2), EntityDataTypes.INT, rightHead == -1 ? 0 : rightHead);
    }

    public int getInvulnerableTime() {
        return super.metadata.getIndex(offset(OFFSET, 3), 0);
    }

    public void setInvulnerableTime(int value) {
        super.metadata.setIndex(offset(OFFSET, 3), EntityDataTypes.INT, value);
    }

}
