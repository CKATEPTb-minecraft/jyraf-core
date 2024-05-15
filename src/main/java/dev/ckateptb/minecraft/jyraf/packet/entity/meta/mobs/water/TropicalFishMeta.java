package dev.ckateptb.minecraft.jyraf.packet.entity.meta.mobs.water;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import dev.ckateptb.minecraft.jyraf.packet.entity.meta.Metadata;
import dev.ckateptb.minecraft.jyraf.packet.entity.meta.types.ObjectData;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.DyeColor;
import org.jetbrains.annotations.NotNull;

public class TropicalFishMeta extends BaseFishMeta implements ObjectData {

    public static final byte OFFSET = BaseFishMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 1;

    public TropicalFishMeta(int entityId, Metadata metadata) {
        super(entityId, metadata);
    }

    public static int getVariantID(Variant variant) {
        Pattern pattern = variant.getPattern();
        DyeColor bodyColor = variant.getBodyColor();
        DyeColor patternColor = variant.getPatternColor();
        return pattern.getId() & '\uffff' | (bodyColor.ordinal() & 255) << 16 | (patternColor.ordinal() & 255) << 24;
    }

    public static Variant getVariantFromID(int variantID) {
        Pattern pattern = Pattern.fromVariant(variantID);
        DyeColor bodyColor = DyeColor.values()[(variantID >> 16) & 0xFF];
        DyeColor patternColor = DyeColor.values()[(variantID >> 24) & 0xFF];
        return new Variant(pattern, bodyColor, patternColor);
    }

    public Variant getVariant() {
        return getVariantFromID(super.metadata.getIndex(OFFSET, 0));
    }

    public void setVariant(Variant variant) {
        super.metadata.setIndex(OFFSET, EntityDataTypes.INT, getVariantID(variant));
    }

    @Override
    public int getObjectData() {
        // TODO: returns Entity ID of the owner (???)
        return 0;
    }

    @Override
    public boolean requiresVelocityPacketAtSpawn() {
        return false;
    }

    @Getter
    public enum Pattern {
        KOB(0, 0),
        SUNSTREAK(0, 1),
        SNOOPER(0, 2),
        DASHER(0, 3),
        BRINELY(0, 4),
        SPOTTY(0, 5),
        FLOPPER(1, 0),
        STRIPEY(1, 1),
        GLITTER(1, 2),
        BLOCKFISH(1, 3),
        BETTY(1, 4),
        CLAYFISH(1, 5);

        private final int size;
        private final int id;

        Pattern(int size, int id) {
            this.size = size;
            this.id = size | id << 8;
        }

        public static Pattern fromVariant(int variant) {
            int id = variant & '\uffff';
            for (Pattern pattern : values()) {
                if (pattern.id == id) {
                    return pattern;
                }
            }
            return Pattern.KOB;
        }
    }

    @Getter
    @Setter
    public static class Variant {
        private Pattern pattern;
        private DyeColor bodyColor;
        private DyeColor patternColor;

        public Variant(@NotNull Pattern pattern, DyeColor bodyColor, DyeColor patternColor) {
            this.pattern = pattern;
            this.bodyColor = bodyColor;
            this.patternColor = patternColor;
        }
    }

}
