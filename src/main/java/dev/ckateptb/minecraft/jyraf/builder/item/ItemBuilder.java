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
public class ItemBuilder<T extends ItemBuilder<T>> implements Builder<ItemStack> {

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

    public <X, Y> T set(@NonNull NamespacedKey key, @NonNull PersistentDataType<X, Y> type, Y value) {
        if (this.meta == null) return (T) this;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        if (value == null) {
            pdc.remove(key);
            return (T) this;
        }
        pdc.set(key, type, value);

        return (T) this;
    }

    public T name(String name) {
        if (this.meta == null) return (T) this;
        this.meta.displayName(Text.of(name));

        return (T) this;
    }

    public T amount(int amount) {
        this.item.setAmount(amount);
        return (T) this;
    }

    public T lore(String... lore) {
        return lore(Arrays.asList(lore));
    }

    public T lore(List<String> lore) {
        if (this.meta == null || lore == null || lore.isEmpty()) return (T) this;
        this.meta.lore(lore.stream().map(Text::of).toList());

        return (T) this;
    }

    public T color(Color color) {
        return this.durability(color.data());
    }

    public T durability(short durability) {
        if (!(this.meta instanceof Damageable damageable)) return (T) this;
        damageable.setDamage(durability);

        return (T) this;
    }

    public T enchant(Enchantment enchantment, int level) {
        if (this.meta == null) return (T) this;
        if (this.meta instanceof EnchantmentStorageMeta storageMeta) {
            storageMeta.addStoredEnchant(enchantment, level, true);
        } else {
            this.meta.addEnchant(enchantment, level, true);
        }

        return (T) this;
    }

    public T unenchant(Enchantment... enchantments) {
        if (this.meta == null) return (T) this;
        for (Enchantment enchantment : enchantments) {
            this.meta.removeEnchant(enchantment);
        }

        return (T) this;
    }

    public T attribute(Attribute attribute, AttributeModifier modifier) {
        return this.attribute(attribute, modifier, true);
    }

    public T attribute(Attribute attribute, AttributeModifier modifier, boolean overwrite) {
        if (this.meta == null) return (T) this;
        if (overwrite) {
            Multimap<Attribute, AttributeModifier> attributes = this.meta.getAttributeModifiers();
            AttributeModifier finalModifier = modifier;
            // changing the UUID if item already has modifier with the same UUID
            if (attributes != null && attributes.values().stream().anyMatch(mod -> mod.getUniqueId().equals(finalModifier.getUniqueId()))) {
                modifier = new AttributeModifier(UUID.randomUUID(), modifier.getName(), modifier.getAmount(), modifier.getOperation(), modifier.getSlot());
            }
        }

        this.meta.addAttributeModifier(attribute, modifier);

        return (T) this;
    }

    public T unattribute(Attribute... attributes) {
        if (this.meta == null) return (T) this;
        for (Attribute attribute : attributes) {
            this.meta.removeAttributeModifier(attribute);
        }

        return (T) this;
    }

    public T flag(ItemFlag... flag) {
        if (this.meta == null) return (T) this;
        this.meta.addItemFlags(flag);

        return (T) this;
    }

    public T deflag(ItemFlag... flag) {
        if (this.meta == null) return (T) this;
        this.meta.removeItemFlags(flag);

        return (T) this;
    }

    public T unbreakable(boolean unbreakable) {
        meta.setUnbreakable(unbreakable);

        return (T) this;
    }

    public T skull(String texture) {
        if (!(this.meta instanceof SkullMeta skullMeta)) return (T) this;
        PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
        profile.getProperties().add(new ProfileProperty("textures", texture));
        skullMeta.setPlayerProfile(profile);

        return (T) this;
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