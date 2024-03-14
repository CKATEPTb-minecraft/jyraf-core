package dev.ckateptb.minecraft.jyraf.packet.entity.property;

import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataType;
import dev.ckateptb.minecraft.jyraf.packet.entity.PacketEntity;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

import java.util.*;

@Getter
public abstract class PacketEntityProperty<T> {
    private final String name;
    private final T defaultValue;
    private final Class<T> type;
    private final Set<PacketEntityProperty<?>> dependencies = new HashSet<>();
    @Setter
    private boolean playerModifiable = true;
    protected PacketEntityProperty(String name, T defaultValue, Class<T> clazz) {
        this.name = name.toLowerCase();
        this.defaultValue = defaultValue;
        this.type = clazz;
    }

    public void addDependency(PacketEntityProperty<?> property) {
        this.dependencies.add(property);
    }

    protected static <V> EntityData newEntityData(int index, EntityDataType<V> type, V value) {
        return new EntityData(index, type, value);
    }

    public List<EntityData> applyStandalone(Player player, PacketEntity packetEntity, boolean isSpawned) {
        Map<Integer, EntityData> map = new HashMap<>();
        apply(player, packetEntity, isSpawned, map);
        for (PacketEntityProperty<?> property : this.dependencies) property.apply(player, packetEntity, isSpawned, map);
        return new ArrayList<>(map.values());
    }

    abstract public void apply(Player player, PacketEntity entity, boolean isSpawned, Map<Integer, EntityData> properties);
}
