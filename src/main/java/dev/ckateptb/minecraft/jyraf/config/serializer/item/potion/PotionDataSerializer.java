package dev.ckateptb.minecraft.jyraf.config.serializer.item.potion;

import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.Locale;

public class PotionDataSerializer implements TypeSerializer<PotionData> {

    private final PotionData empty;

    public PotionDataSerializer() {
        this.empty = new PotionData(PotionType.LUCK, false, false);
    }

    @Override
    public PotionData deserialize(Type type, ConfigurationNode node) throws SerializationException {
        ConfigurationNode potionTypeNode = node.node("type");
        String potionTypeStr = potionTypeNode.getString();
        if (potionTypeStr == null || potionTypeStr.isBlank()) return this.empty;

        PotionType potionType;
        try {
            potionType = PotionType.valueOf(potionTypeStr.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new SerializationException(e);
        }

        boolean extended = false;
        boolean upgraded = false;

        if (node.hasChild("extended")) extended = node.node("extended").getBoolean(false);
        if (node.hasChild("upgraded")) upgraded = node.node("upgraded").getBoolean(false);

        return new PotionData(potionType, extended, upgraded);
    }

    @Override
    public void serialize(Type type, @Nullable PotionData data, ConfigurationNode node) throws SerializationException {
        if (data == null) data = this.empty;

        ConfigurationNode potionTypeNode = node.node("type");
        potionTypeNode.set(data.getType());
        if (data.isExtended()) node.node("extended").set(true);
        if (data.isUpgraded()) node.node("upgraded").set(true);
    }

}