package dev.ckateptb.minecraft.jyraf.builder.potion;

import dev.ckateptb.minecraft.jyraf.builder.item.ItemBuilder;
import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;

import java.util.List;
import java.util.Objects;

public class PotionBuilder extends ItemBuilder<PotionBuilder> {

    public PotionBuilder() {
        this(PotionType.DEFAULT);
    }

    public PotionBuilder(@NonNull PotionType type) {
        this(type.getMaterial());
    }

    public PotionBuilder(@NonNull ItemStack stack) {
        super(stack);
    }

    public PotionBuilder(@NonNull ItemStack stack, boolean clone) {
        super(clone ? stack.clone() : stack, clone);
        Material material = stack.getType();
        if (material != Material.POTION && material != Material.SPLASH_POTION && material != Material.LINGERING_POTION)
            throw new IllegalArgumentException("Material should be provided as potion, provided: " + material.name());
    }

    public PotionBuilder(@NonNull Material material) {
        super(new ItemStack(material));
        Objects.requireNonNull(material, "material cannot be null");
        if (material != Material.POTION && material != Material.SPLASH_POTION && material != Material.LINGERING_POTION)
            throw new IllegalArgumentException("Material should be provided as potion, provided: " + material.name());
    }

    public PotionBuilder color(org.bukkit.Color color) {
        if (!(getMeta() instanceof PotionMeta meta)) return this;
        meta.setColor(color);

        return this;
    }

    public PotionBuilder effect(PotionEffect effect) {
        return effect(effect, true);
    }

    public PotionBuilder effects(List<PotionEffect> effects) {
        effects.forEach(this::effect);
        return this;
    }

    public PotionBuilder baseData(PotionData baseData) {
        if (!(getMeta() instanceof PotionMeta meta)) return this;
        meta.setBasePotionData(baseData);

        return this;
    }

    public PotionBuilder effect(PotionEffect effect, boolean overwrite) {
        if (!(getMeta() instanceof PotionMeta meta)) return this;
        meta.addCustomEffect(effect, overwrite);

        return this;
    }

    @Override
    public ItemStack build() {
        if (getMeta() == null) return getItem();
        getItem().setItemMeta(getMeta());

        return getItem();
    }

    public enum PotionType {

        DEFAULT,
        SPLASH,
        LINGERING;

        public Material getMaterial() {
            return switch (this) {
                case DEFAULT -> Material.POTION;
                case SPLASH -> Material.SPLASH_POTION;
                case LINGERING -> Material.LINGERING_POTION;
            };
        }

        public static PotionType getByMaterial(@NonNull Material material) {
            return switch (material) {
                case POTION -> DEFAULT;
                case SPLASH_POTION -> SPLASH;
                case LINGERING_POTION -> LINGERING;
                default -> null;
            };
        }

    }

}