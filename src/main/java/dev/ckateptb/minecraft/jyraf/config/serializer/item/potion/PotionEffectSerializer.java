package dev.ckateptb.minecraft.jyraf.config.serializer.item.potion;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Locale;

public class PotionEffectSerializer implements TypeSerializer<PotionEffect> {

    private final PotionEffect empty;
    private final List<PotionEffectType> allowedTypes;

    public PotionEffectSerializer() {
        this.empty = new PotionEffect(PotionEffectType.UNLUCK, 0, 0, true, true, true);
        this.allowedTypes = List.of(PotionEffectType.values());
    }

    @Override
    public PotionEffect deserialize(Type type, ConfigurationNode node) throws SerializationException {
        String effectTypeStr = node.node("effectType").getString();
        if (effectTypeStr == null || effectTypeStr.isBlank())
            throw new SerializationException("Potion effect type is null or blank");

        PotionEffectType potionEffectType = PotionEffectType.getByName(effectTypeStr.toUpperCase(Locale.ROOT));
        if (potionEffectType == null)
            throw new SerializationException("Invalid potion effect type: " + effectTypeStr);

        int duration = 0;
        int amplifier = 0;
        boolean ambient = true;
        boolean particles = true;
        boolean icon = true;

        if (node.hasChild("duration")) duration = node.node("duration").getInt(0);
        if (node.hasChild("amplifier")) amplifier = node.node("amplifier").getInt(0);
        if (node.hasChild("ambient")) ambient = node.node("ambient").getBoolean(true);
        if (node.hasChild("particles")) particles = node.node("particles").getBoolean(true);
        if (node.hasChild("icon")) icon = node.node("icon").getBoolean(true);

        return new PotionEffect(potionEffectType, duration, amplifier, ambient, particles, icon);
    }

    @Override
    public void serialize(Type type, @Nullable PotionEffect effect, ConfigurationNode node) throws SerializationException {
        if (effect == null) effect = this.empty;

        ConfigurationNode effectTypeNode = node.node("effectType");
        if (effectTypeNode instanceof CommentedConfigurationNode commented) {
            commented.comment("Allowed options " + this.allowedTypes);
        }
        effectTypeNode.set(effect.getType().getName());
        node.node("duration").set(effect.getDuration());
        node.node("amplifier").set(effect.getAmplifier());
        if (!effect.isAmbient()) node.node("ambient").set(false);
        if (!effect.hasParticles()) node.node("particles").set(false);
        if (!effect.hasIcon()) node.node("icon").set(false);
    }

}