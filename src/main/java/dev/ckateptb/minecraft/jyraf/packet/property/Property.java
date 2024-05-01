package dev.ckateptb.minecraft.jyraf.packet.property;

import dev.ckateptb.minecraft.jyraf.packet.entity.enums.TeamColor;
import dev.ckateptb.minecraft.jyraf.packet.managed.RepositoryManaged;
import lombok.Getter;

import java.util.Map;

@Getter
public enum Property {
    ENTITY_SPEED(0.2),
    ENTITY_TEAM(TeamColor.WHITE),
    VIEW_DISTANCE(20d);

    private final Object defaultValue;

    <V> Property(V defaultValue) {
        this.defaultValue = defaultValue;
    }

    @SuppressWarnings("unchecked")
    public <V> V parse(RepositoryManaged entry, Class<V> clazz) {
        Map<String, ?> properties = entry.getProperties();
        String name = this.name();
        if (properties.containsKey(name)) return (V) properties.get(name);
        return (V) this.defaultValue;
    }

    public <V> void apply(RepositoryManaged entry, V value) {
        Map<String, Object> properties = entry.getProperties();
        String name = this.name();
        properties.put(name, value);
    }
}
