package dev.ckateptb.minecraft.jyraf.builder.item;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.google.common.collect.Multimap;
import dev.ckateptb.minecraft.jyraf.builder.Builder;
import dev.ckateptb.minecraft.jyraf.component.Text;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@SuppressWarnings({"unchecked"})
public class ItemBuilder<R extends ItemBuilder<R>> implements Builder<ItemStack> {

    protected final ItemStack item;
    protected final ItemMeta meta;

    public ItemBuilder(Material material) {
        this(new ItemStack(material));
    }

    public ItemBuilder(ItemStack item) {
        this(item, true);
    }

    public ItemBuilder(ItemStack item, boolean clone) {
        if (clone) this.item = item.clone();
        else this.item = item;
        this.meta = this.item.getItemMeta();
    }

    public <K, V> R setTag(@NonNull NamespacedKey key, @NonNull PersistentDataType<K, V> type, V value) {
        if (this.meta == null) return (R) this;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        if (value == null) {
            pdc.remove(key);
            return (R) this;
        }
        pdc.set(key, type, value);

        return (R) this;
    }

    public R name(String name) {
        if (this.meta == null) return (R) this;
        this.meta.displayName(Text.of(name));

        return (R) this;
    }

    public R amount(int amount) {
        this.item.setAmount(amount);
        return (R) this;
    }

    public R lore(String... lore) {
        return lore(Arrays.asList(lore));
    }

    public R lore(List<String> lore) {
        if (this.meta == null || lore == null || lore.isEmpty()) return (R) this;
        this.meta.lore(lore.stream().map(Text::of).toList());

        return (R) this;
    }

    public R color(Color color) {
        return this.durability(color.data());
    }

    public R durability(short durability) {
        if (!(this.meta instanceof Damageable damageable)) return (R) this;
        damageable.setDamage(durability);

        return (R) this;
    }

    public R enchant(Enchantment enchantment, int level) {
        if (this.meta == null) return (R) this;
        if (this.meta instanceof EnchantmentStorageMeta storageMeta) {
            storageMeta.addStoredEnchant(enchantment, level, true);
        } else {
            this.meta.addEnchant(enchantment, level, true);
        }

        return (R) this;
    }

    public R unenchant(Enchantment... enchantments) {
        if (this.meta == null) return (R) this;
        for (Enchantment enchantment : enchantments) {
            this.meta.removeEnchant(enchantment);
        }

        return (R) this;
    }

    public R attribute(Attribute attribute, AttributeModifier modifier) {
        return this.attribute(attribute, modifier, true);
    }

    public R attribute(Attribute attribute, AttributeModifier modifier, boolean overwrite) {
        if (this.meta == null) return (R) this;
        if (overwrite) {
            Multimap<Attribute, AttributeModifier> attributes = this.meta.getAttributeModifiers();
            AttributeModifier finalModifier = modifier;
            // changing the UUID if item already has modifier with the same UUID
            if (attributes != null && attributes.values().stream().anyMatch(mod -> mod.getUniqueId().equals(finalModifier.getUniqueId()))) {
                modifier = new AttributeModifier(UUID.randomUUID(), modifier.getName(), modifier.getAmount(), modifier.getOperation(), modifier.getSlot());
            }
        }

        this.meta.addAttributeModifier(attribute, modifier);

        return (R) this;
    }

    public R unattribute(Attribute... attributes) {
        if (this.meta == null) return (R) this;
        for (Attribute attribute : attributes) {
            this.meta.removeAttributeModifier(attribute);
        }

        return (R) this;
    }

    public R flag(ItemFlag... flag) {
        if (this.meta == null) return (R) this;
        this.meta.addItemFlags(flag);

        return (R) this;
    }

    public R deflag(ItemFlag... flag) {
        if (this.meta == null) return (R) this;
        this.meta.removeItemFlags(flag);

        return (R) this;
    }

    public R unbreakable(boolean unbreakable) {
        meta.setUnbreakable(unbreakable);

        return (R) this;
    }

    public R skull(String texture) {
        if (!(this.meta instanceof SkullMeta skullMeta)) return (R) this;
        PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
        profile.getProperties().add(new ProfileProperty("textures", texture));
        skullMeta.setPlayerProfile(profile);

        return (R) this;
    }

    public ItemStack build() {
        if (this.meta == null) return this.item;

        this.item.setItemMeta(this.meta);

        return this.item;
    }

    public enum Color {
        WHITE,
        ORANGE,
        MAGENTA,
        LIGHT_BLUE,
        YELLOW,
        LIME,
        PINK,
        GRAY,
        LIGHT_GRAY,
        CYAN,
        PURPLE,
        BLUE,
        BROWN,
        GREEN,
        RED,
        BLACK;

        short data() {
            return (short) this.ordinal();
        }
    }

}