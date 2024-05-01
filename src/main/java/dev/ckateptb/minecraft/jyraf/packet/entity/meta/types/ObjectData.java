package dev.ckateptb.minecraft.jyraf.packet.entity.meta.types;

public interface ObjectData {

    int getObjectData();

    boolean requiresVelocityPacketAtSpawn();

}
