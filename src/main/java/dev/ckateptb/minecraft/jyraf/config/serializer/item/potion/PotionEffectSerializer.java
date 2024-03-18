package dev.ckateptb.minecraft.jyraf.config.serializer.item.potion;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.Locale;

public class PotionEffectSerializer implements TypeSerializer<PotionEffect> {

    private final PotionEffect empty;

    public PotionEffectSerializer() {
        this.empty = new PotionEffect(PotionEffectType.UNLUCK, 0, 0, true, true, true);
    }

    @Override
    public PotionEffect deserialize(Type type, ConfigurationNode node) throws SerializationException {
        String effectTypeStr = node.node("type").getString();
        if (effectTypeStr == null || effectTypeStr.isBlank())
            throw new SerializationException("Potion effect type is null or blank");

        PotionEffectType potionEffectType = PotionEffectType.getByName(effectTypeStr.toUpperCase(Locale.ROOT));
        if (potionEffectType == null)
            throw new SerializationException("Invalid potion effect type: " + effectTypeStr);

        int duration = node.hasChild("duration") ? node.node("duration").getInt() : 0;
        int amplifier = node.hasChild("amplifier") ? node.node("amplifier").getInt() : 0;
        boolean ambient = !node.hasChild("ambient") || node.node("ambient").getBoolean();
        boolean particles = !node.hasChild("particles") || node.node("particles").getBoolean();
        boolean icon = !node.hasChild("icon") || node.node("icon").getBoolean();

        return new PotionEffect(potionEffectType, duration, amplifier, ambient, particles, icon);
    }

    @Override
    public void serialize(Type type, @Nullable PotionEffect effect, ConfigurationNode node) throws SerializationException {
        if (effect == null) effect = this.empty;

        ConfigurationNode effectTypeNode = node.node("type");
        effectTypeNode.set(effect.getType().getName());
        node.node("duration").set(effect.getDuration());
        node.node("amplifier").set(effect.getAmplifier());
        if (!effect.isAmbient()) node.node("ambient").set(false);
        if (!effect.hasParticles()) node.node("particles").set(false);
        if (!effect.hasIcon()) node.node("icon").set(false);
    }

}