package dev.ckateptb.minecraft.jyraf.builder.item;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.google.common.collect.Multimap;
import dev.ckateptb.minecraft.jyraf.builder.Builder;
import dev.ckateptb.minecraft.jyraf.component.Text;
import lombok.Getter;
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
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Getter
@SuppressWarnings({"unchecked"})
public class ItemBuilder<X extends ItemBuilder<X>> implements Builder<ItemStack> {

    private final ItemStack item;
    private final ItemMeta meta;

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

    public <T, Z> X set(@NonNull Plugin plugin, @NonNull String key, @NonNull PersistentDataType<T, Z> type, Z value) {
        return this.set(plugin.getName().toLowerCase(Locale.ROOT), key, type, value);
    }

    public <T, Z> X set(@NonNull String namespace, @NonNull String key, @NonNull PersistentDataType<T, Z> type, Z value) {
        if (this.meta == null) return (X) this;
        NamespacedKey namespacedKey = new NamespacedKey(namespace, key);
        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        if (value == null) {
            pdc.remove(namespacedKey);
            return (X) this;
        }
        pdc.set(namespacedKey, type, value);

        return (X) this;
    }

    public X name(String name) {
        if (this.meta == null) return (X) this;
        this.meta.displayName(Text.of(name));

        return (X) this;
    }

    public X amount(int amount) {
        this.item.setAmount(amount);
        return (X) this;
    }

    public X lore(String... lore) {
        return lore(Arrays.asList(lore));
    }

    public X lore(List<String> lore) {
        if (this.meta == null || lore == null || lore.isEmpty()) return (X) this;
        this.meta.lore(lore.stream().map(Text::of).toList());

        return (X) this;
    }

    public X color(Color color) {
        return this.durability(color.data());
    }

    public X durability(short durability) {
        if (!(this.meta instanceof Damageable damageable)) return (X) this;
        damageable.setDamage(durability);

        return (X) this;
    }

    public X enchant(Enchantment enchantment, int level) {
        if (this.meta == null) return (X) this;
        this.meta.addEnchant(enchantment, level, true);

        return (X) this;
    }

    public X storedEnchant(Enchantment enchantment, int level) {
        if (!(this.meta instanceof EnchantmentStorageMeta storageMeta)) return (X) this;
        storageMeta.addStoredEnchant(enchantment, level, true);

        return (X) this;
    }

    public X unenchant(Enchantment... enchantments) {
        if (this.meta == null) return (X) this;
        for (Enchantment enchantment : enchantments) {
            this.meta.removeEnchant(enchantment);
        }

        return (X) this;
    }

    public X attribute(Attribute attribute, AttributeModifier modifier) {
        return this.attribute(attribute, modifier, true);
    }

    public X attribute(Attribute attribute, AttributeModifier modifier, boolean overwrite) {
        if (this.meta == null) return (X) this;
        if (overwrite) {
            Multimap<Attribute, AttributeModifier> attributes = this.meta.getAttributeModifiers();
            AttributeModifier finalModifier = modifier;
            // changing the UUID if item already has modifier with the same UUID
            if (attributes != null && attributes.values().stream().anyMatch(mod -> mod.getUniqueId().equals(finalModifier.getUniqueId()))) {
                modifier = new AttributeModifier(UUID.randomUUID(), modifier.getName(), modifier.getAmount(), modifier.getOperation(), modifier.getSlot());
            }
        }

        this.meta.addAttributeModifier(attribute, modifier);

        return (X) this;
    }

    public X unattribute(Attribute... attributes) {
        if (this.meta == null) return (X) this;
        for (Attribute attribute : attributes) {
            this.meta.removeAttributeModifier(attribute);
        }

        return (X) this;
    }

    public X flag(ItemFlag... flag) {
        if (this.meta == null) return (X) this;
        this.meta.addItemFlags(flag);

        return (X) this;
    }

    public X unbreakable(boolean unbreakable) {
        meta.setUnbreakable(unbreakable);

        return (X) this;
    }

    public X skull(String texture) {
        if (!(this.meta instanceof SkullMeta skullMeta)) return (X) this;
        PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
        profile.getProperties().add(new ProfileProperty("textures", texture));
        skullMeta.setPlayerProfile(profile);

        return (X) this;
    }

    public X deflag(ItemFlag... flag) {
        if (this.meta == null) return (X) this;
        this.meta.removeItemFlags(flag);

        return (X) this;
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