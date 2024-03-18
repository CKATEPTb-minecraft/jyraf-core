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
public class ItemBuilder<B extends ItemBuilder<B>> implements Builder<ItemStack> {

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

    public <K, V> B set(@NonNull NamespacedKey key, @NonNull PersistentDataType<K, V> type, V value) {
        if (this.meta == null) return (B) this;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        if (value == null) {
            pdc.remove(key);
            return (B) this;
        }
        pdc.set(key, type, value);

        return (B) this;
    }

    public B name(String name) {
        if (this.meta == null) return (B) this;
        this.meta.displayName(Text.of(name));

        return (B) this;
    }

    public B amount(int amount) {
        this.item.setAmount(amount);
        return (B) this;
    }

    public B lore(String... lore) {
        return lore(Arrays.asList(lore));
    }

    public B lore(List<String> lore) {
        if (this.meta == null || lore == null || lore.isEmpty()) return (B) this;
        this.meta.lore(lore.stream().map(Text::of).toList());

        return (B) this;
    }

    public B color(Color color) {
        return this.durability(color.data());
    }

    public B durability(short durability) {
        if (!(this.meta instanceof Damageable damageable)) return (B) this;
        damageable.setDamage(durability);

        return (B) this;
    }

    public B enchant(Enchantment enchantment, int level) {
        if (this.meta == null) return (B) this;
        if (this.meta instanceof EnchantmentStorageMeta storageMeta) {
            storageMeta.addStoredEnchant(enchantment, level, true);
        } else {
            this.meta.addEnchant(enchantment, level, true);
        }

        return (B) this;
    }

    public B unenchant(Enchantment... enchantments) {
        if (this.meta == null) return (B) this;
        for (Enchantment enchantment : enchantments) {
            this.meta.removeEnchant(enchantment);
        }

        return (B) this;
    }

    public B attribute(Attribute attribute, AttributeModifier modifier) {
        return this.attribute(attribute, modifier, true);
    }

    public B attribute(Attribute attribute, AttributeModifier modifier, boolean overwrite) {
        if (this.meta == null) return (B) this;
        if (overwrite) {
            Multimap<Attribute, AttributeModifier> attributes = this.meta.getAttributeModifiers();
            AttributeModifier finalModifier = modifier;
            // changing the UUID if item already has modifier with the same UUID
            if (attributes != null && attributes.values().stream().anyMatch(mod -> mod.getUniqueId().equals(finalModifier.getUniqueId()))) {
                modifier = new AttributeModifier(UUID.randomUUID(), modifier.getName(), modifier.getAmount(), modifier.getOperation(), modifier.getSlot());
            }
        }

        this.meta.addAttributeModifier(attribute, modifier);

        return (B) this;
    }

    public B unattribute(Attribute... attributes) {
        if (this.meta == null) return (B) this;
        for (Attribute attribute : attributes) {
            this.meta.removeAttributeModifier(attribute);
        }

        return (B) this;
    }

    public B flag(ItemFlag... flag) {
        if (this.meta == null) return (B) this;
        this.meta.addItemFlags(flag);

        return (B) this;
    }

    public B deflag(ItemFlag... flag) {
        if (this.meta == null) return (B) this;
        this.meta.removeItemFlags(flag);

        return (B) this;
    }

    public B unbreakable(boolean unbreakable) {
        meta.setUnbreakable(unbreakable);

        return (B) this;
    }

    public B skull(String texture) {
        if (!(this.meta instanceof SkullMeta skullMeta)) return (B) this;
        PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
        profile.getProperties().add(new ProfileProperty("textures", texture));
        skullMeta.setPlayerProfile(profile);

        return (B) this;
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