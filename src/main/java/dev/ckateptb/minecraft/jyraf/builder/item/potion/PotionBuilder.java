package dev.ckateptb.minecraft.jyraf.builder.item.potion;

import dev.ckateptb.minecraft.jyraf.builder.item.ItemBuilder;
import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;

import java.util.Arrays;

public class PotionBuilder extends ItemBuilder<PotionBuilder> {

    public PotionBuilder() {
        this(PotionType.POTION);
    }

    public PotionBuilder(@NonNull PotionType type) {
        this(type.getMaterial());
    }

    public PotionBuilder(@NonNull Material material) {
        this(new ItemStack(material));
    }

    public PotionBuilder(@NonNull ItemStack stack) {
        this(stack, true);
    }

    public PotionBuilder(@NonNull ItemStack stack, boolean clone) {
        super(stack, clone);
        Material material = stack.getType();
        if (material != Material.POTION && material != Material.SPLASH_POTION && material != Material.LINGERING_POTION)
            throw new IllegalArgumentException("Material should be provided as potion, provided: " + material.name());
    }

    public PotionBuilder color(org.bukkit.Color color) {
        if (!(this.meta instanceof PotionMeta meta)) return this;
        meta.setColor(color);

        return this;
    }

    public PotionBuilder effect(PotionEffect... effects) {
        Arrays.stream(effects).forEach(effect -> this.effect(effect, true));
        return this;
    }

    public PotionBuilder effect(PotionEffect effect, boolean overwrite) {
        if (!(this.meta instanceof PotionMeta meta)) return this;
        meta.addCustomEffect(effect, overwrite);

        return this;
    }

    public PotionBuilder data(PotionData baseData) {
        if (!(this.meta instanceof PotionMeta meta)) return this;
        meta.setBasePotionData(baseData);

        return this;
    }

    @Override
    public ItemStack build() {
        if (this.meta == null) return this.item;
        this.item.setItemMeta(this.meta);

        return this.item;
    }

    public enum PotionType {
        POTION,
        SPLASH_POTION,
        LINGERING_POTION;

        public Material getMaterial() {
            return Material.valueOf(name());
        }

        public static PotionType getByMaterial(@NonNull Material material) {
            return PotionType.valueOf(material.name());
        }
    }

}