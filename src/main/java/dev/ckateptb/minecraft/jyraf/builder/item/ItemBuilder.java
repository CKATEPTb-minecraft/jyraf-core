package dev.ckateptb.minecraft.jyraf.builder.item;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.google.common.collect.Multimap;
import com.google.gson.JsonObject;
import com.jeff_media.persistentdataserializer.PersistentDataSerializer;
import dev.ckateptb.minecraft.jyraf.Jyraf;
import dev.ckateptb.minecraft.jyraf.builder.Builder;
import dev.ckateptb.minecraft.jyraf.component.Text;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.spongepowered.configurate.ConfigurationNode;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class ItemBuilder implements Builder<ItemStack> {

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

    public <K, V> ItemBuilder tag(String serialized) {
        if (this.meta == null) return this;
        PersistentDataSerializer.fromJson(serialized, this.meta.getPersistentDataContainer());
        return this;
    }

    public <K, V> ItemBuilder tag(NamespacedKey key, PersistentDataType<K, V> type, V value) {
        if (this.meta == null) return this;
        PersistentDataContainer container = this.meta.getPersistentDataContainer();
        container.set(key, type, value);
        return this;
    }

    public <K, V> ItemBuilder untag(NamespacedKey... keys) {
        if (this.meta == null) return this;
        PersistentDataContainer container = this.meta.getPersistentDataContainer();
        for (NamespacedKey key : keys) {
            container.remove(key);
        }
        return this;
    }

    public ItemBuilder name(String name) {
        if (this.meta == null) return this;
        this.meta.displayName(Text.of(name));

        return this;
    }

    public ItemBuilder amount(int amount) {
        this.item.setAmount(amount);
        return this;
    }

    public ItemBuilder lore(String... lore) {
        return lore(Arrays.asList(lore));
    }

    public ItemBuilder lore(List<String> lore) {
        if (this.meta == null || lore == null || lore.isEmpty()) return this;
        this.meta.lore(lore.stream().map(Text::of).toList());

        return this;
    }

    public ItemBuilder color(Color color) {
        return this.durability(color.data());
    }

    public ItemBuilder durability(short durability) {
        if (!(this.meta instanceof Damageable damageable)) return this;
        damageable.setDamage(durability);

        return this;
    }

    public ItemBuilder enchant(Enchantment enchantment, int level) {
        if (this.meta == null) return this;
        this.meta.addEnchant(enchantment, level, true);
        return this;
    }

    public ItemBuilder unenchant(Enchantment... enchantments) {
        if (this.meta == null) return this;
        for (Enchantment enchantment : enchantments) {
            this.meta.removeEnchant(enchantment);
        }

        return this;
    }

    public ItemBuilder attribute(Attribute attribute, AttributeModifier modifier) {
        return this.attribute(attribute, modifier, true);
    }

    public ItemBuilder attribute(Attribute attribute, AttributeModifier modifier, boolean overwrite) {
        if (this.meta == null) return this;
        if (overwrite) {
            Multimap<Attribute, AttributeModifier> attributes = this.meta.getAttributeModifiers();
            AttributeModifier finalModifier = modifier;
            // changing the UUID if item already has modifier with the same UUID
            if (attributes != null && attributes.values().stream().anyMatch(mod -> mod.getUniqueId().equals(finalModifier.getUniqueId()))) {
                modifier = new AttributeModifier(UUID.randomUUID(), modifier.getName(), modifier.getAmount(), modifier.getOperation(), modifier.getSlot());
            }
        }

        this.meta.addAttributeModifier(attribute, modifier);

        return this;
    }

    public ItemBuilder unattribute(Attribute... attributes) {
        if (this.meta == null) return this;
        for (Attribute attribute : attributes) {
            this.meta.removeAttributeModifier(attribute);
        }

        return this;
    }

    public ItemBuilder flag(ItemFlag... flag) {
        if (this.meta == null) return this;
        this.meta.addItemFlags(flag);

        return this;
    }

    public ItemBuilder deflag(ItemFlag... flag) {
        if (this.meta == null) return this;
        this.meta.removeItemFlags(flag);

        return this;
    }

    public ItemBuilder unbreakable(boolean unbreakable) {
        this.meta.setUnbreakable(unbreakable);
        return this;
    }

    public ItemBuilder skull(String texture) {
        if (!(this.meta instanceof SkullMeta skullMeta)) return this;
        PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
        profile.getProperties().add(new ProfileProperty("textures", texture));
        skullMeta.setPlayerProfile(profile);
        return this;
    }

    public ItemBuilder book(Consumer<BookBuilder> consumer) {
        if (!(this.meta instanceof EnchantmentStorageMeta)) return this;
        consumer.accept(new BookBuilder());
        return this;
    }

    public ItemBuilder skull(Consumer<SkullBuilder> consumer) {
        if (!(this.meta instanceof SkullMeta)) return this;
        consumer.accept(new SkullBuilder());
        return this;
    }

    public ItemBuilder potion(Consumer<PotionBuilder> consumer) {
        if (!(this.meta instanceof PotionMeta)) return this;
        consumer.accept(new PotionBuilder());
        return this;
    }

    public ItemStack build() {
        if (this.meta == null) return this.item;
        this.item.setItemMeta(this.meta);
        return this.item;
    }

    public class BookBuilder {
        public BookBuilder enchant(Enchantment enchantment, int level) {
            ((EnchantmentStorageMeta) ItemBuilder.this.meta).addStoredEnchant(enchantment, level, true);
            return this;
        }

        public BookBuilder unenchant(Enchantment... enchantments) {
            for (Enchantment enchantment : enchantments) {
                ((EnchantmentStorageMeta) ItemBuilder.this.meta).removeStoredEnchant(enchantment);
            }
            return this;
        }
    }

    public class SkullBuilder {
        public SkullBuilder url(String url) {
            return url(url, UUID.randomUUID());
        }

        @SneakyThrows
        public SkullBuilder url(String url, UUID profileUUID) {
            ConfigurationNode node = Jyraf.getGsonMapper().createNode();
            node.node("textures", "SKIN", "url").set(url);
            JsonObject json = node.get(JsonObject.class);
            if (json == null) return this;
            byte[] bytes = json.getAsString().getBytes(StandardCharsets.UTF_8);
            return texture(Base64.getEncoder().encodeToString(bytes), profileUUID);
        }

        public SkullBuilder texture(String texture) {
            return texture(texture, UUID.randomUUID());
        }

        public SkullBuilder texture(String texture, UUID profileUUID) {
            PlayerProfile profile = Bukkit.createProfile(profileUUID);
            profile.getProperties().add(new ProfileProperty("textures", texture));
            ((SkullMeta) ItemBuilder.this.meta).setPlayerProfile(profile);
            return this;
        }

        public SkullBuilder owner(UUID uuid) {
            ((SkullMeta) ItemBuilder.this.meta).setOwningPlayer(Bukkit.getOfflinePlayer(uuid));
            return this;
        }
    }

    public class PotionBuilder {
        public PotionBuilder color(org.bukkit.Color color) {
            ((PotionMeta) ItemBuilder.this.meta).setColor(color);
            return this;
        }

        public PotionBuilder effect(PotionEffect... effects) {
            for (PotionEffect effect : effects) {
                this.effect(effect, true);
            }
            return this;
        }

        public PotionBuilder uneffect(PotionEffectType type) {
            ((PotionMeta) ItemBuilder.this.meta).removeCustomEffect(type);
            return this;
        }

        public PotionBuilder effect(PotionEffect effect, boolean overwrite) {
            ((PotionMeta) ItemBuilder.this.meta).addCustomEffect(effect, overwrite);
            return this;
        }

        public PotionBuilder data(PotionData baseData) {
            ((PotionMeta) ItemBuilder.this.meta).setBasePotionData(baseData);
            return this;
        }
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