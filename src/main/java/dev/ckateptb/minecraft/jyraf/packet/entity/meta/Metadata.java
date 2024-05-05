package dev.ckateptb.minecraft.jyraf.packet.entity.meta;

import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import dev.ckateptb.minecraft.jyraf.packet.entity.PacketEntity;
import dev.ckateptb.minecraft.jyraf.packet.factory.PacketFactory;
import dev.ckateptb.minecraft.jyraf.lazy.LazyLoader;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unchecked")
public class Metadata {

    private final int entityId;
    private final Map<Byte, EntityData> notNotifiedChanges = new HashMap<>();
    private final Map<Byte, EntityData> metadataMap = new ConcurrentHashMap<>();
    @Getter
    private final LazyLoader.Later<PacketEntity> entity = LazyLoader.later();
    private volatile boolean notifyAboutChanges = true;

    public Metadata(int entityId) {
        this.entityId = entityId;
    }

    public <T> T getIndex(byte index, @Nullable T defaultValue) {
        EntityData value = this.metadataMap.get(index);
        return value != null ? (T) value.getValue() : defaultValue;
    }

    public <T> void setIndex(byte index, @NotNull EntityDataType<T> dataType, T value) {

        final EntityData entry = new EntityData(index, dataType, value);
        this.metadataMap.put(index, entry);

        this.entity.consume(entity -> {
            Set<Player> currentViewers = entity.getCurrentViewers();
            if (currentViewers.isEmpty())
                return;
            if (!this.notifyAboutChanges) {
                synchronized (this.notNotifiedChanges) {
                    this.notNotifiedChanges.put(index, entry);
                }
            } else {
                PacketFactory.INSTANCE.consume(factory -> {
                    entity.metadata(currentViewers);
                });
            }
        });
    }

    public void setNotifyAboutChanges(boolean notifyAboutChanges) {
        if (this.notifyAboutChanges == notifyAboutChanges) {
            return;
        }

        List<EntityData> entries = null;
        synchronized (this.notNotifiedChanges) {
            this.notifyAboutChanges = notifyAboutChanges;
            if (notifyAboutChanges) {
                entries = new ArrayList<>(this.notNotifiedChanges.values());
                if (entries.isEmpty()) {
                    return;
                }
                this.notNotifiedChanges.clear();
            }
        }
        List<EntityData> finalEntries = entries;
        this.entity.consume(entity -> {
            Set<Player> currentViewers = entity.getCurrentViewers();
            if (finalEntries == null || currentViewers.isEmpty()) {
                return;
            }
            WrapperPlayServerEntityMetadata packet = new WrapperPlayServerEntityMetadata(entityId, finalEntries);
            PacketFactory.INSTANCE.consume(factory ->
                    currentViewers.forEach(player ->
                            factory.sendPacket(player, packet)
                    )
            );
        });
    }


    public boolean isNotifyingChanges() {
        return notifyAboutChanges;
    }

    @NotNull
    List<EntityData> getEntries() {
        return Collections.unmodifiableList(new ArrayList<>(metadataMap.values()));
    }

    public WrapperPlayServerEntityMetadata createPacket() {
        return new WrapperPlayServerEntityMetadata(entityId, getEntries());
    }

}