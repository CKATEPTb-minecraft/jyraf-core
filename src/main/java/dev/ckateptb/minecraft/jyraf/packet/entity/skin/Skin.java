package dev.ckateptb.minecraft.jyraf.packet.entity.skin;

import com.github.retrooper.packetevents.protocol.player.TextureProperty;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import org.joor.Reflect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Skin {
    private final long timestamp = System.currentTimeMillis();
    @Getter
    private final List<TextureProperty> properties;

    public Skin(String texture, String signature) {
        properties = new ArrayList<>(1);
        properties.add(new TextureProperty("textures", texture, signature));
    }

    public Skin(Collection<TextureProperty> properties) {
        this.properties = new ArrayList<>(properties);
    }

    public Skin(Object propertyMap) {
        this.properties = new ArrayList<>();
        for (Object instance : Reflect.on(propertyMap).call("values").as(Collection.class)) {
            Reflect property = Reflect.on(instance);
            String name = property.field("name").as(String.class);
            String value = property.field("value").as(String.class);
            String signature = property.field("signature").as(String.class);
            this.properties.add(new TextureProperty(name, value, signature));
        }
    }

    public Skin(JsonObject obj) {
        properties = new ArrayList<>();
        for (JsonElement e : obj.get("properties").getAsJsonArray()) {
            JsonObject o = e.getAsJsonObject();
            properties.add(new TextureProperty(o.get("name").getAsString(), o.get("value").getAsString(), o.has("signature") ? o.get("signature").getAsString() : null));
        }
    }

    public UserProfile apply(UserProfile profile) {
        profile.setTextureProperties(properties);
        return profile;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - timestamp > 60000L;
    }

    public String getTexture() {
        for (TextureProperty property : properties)
            if (property.getName().equalsIgnoreCase("textures"))
                return property.getValue();
        return null;
    }

    public String getSignature() {
        for (TextureProperty property : properties)
            if (property.getName().equalsIgnoreCase("textures"))
                return property.getSignature();
        return null;
    }
}