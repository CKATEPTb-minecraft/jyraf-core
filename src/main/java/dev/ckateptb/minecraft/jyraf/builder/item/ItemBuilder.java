package dev.ckateptb.minecraft.jyraf.builder.item;

import com.destroystokyo.paper.profile.CraftPlayerProfile;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import dev.ckateptb.minecraft.jyraf.builder.Builder;
import dev.ckateptb.minecraft.jyraf.component.Text;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ItemBuilder implements Builder<ItemStack> {
    private final ItemStack item;
    private final ItemMeta meta;

    public ItemBuilder(Material material) {
        this(new ItemStack(material));
    }

    public ItemBuilder(ItemStack item) {
        this.item = item.clone();
        this.meta = this.item.getItemMeta();
    }

    public ItemBuilder name(String name) {
        if (this.meta != null) {
            this.meta.displayName(Text.of(name));
        }
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
        if (lore != null && this.meta != null) {
            this.meta.lore(lore.stream().map(Text::of).toList());
        }
        return this;
    }

    public ItemBuilder color(Color color) {
        return this.durability(color.data());
    }

    public ItemBuilder durability(short durability) {
        if (this.meta instanceof Damageable damageable) {
            damageable.setDamage(durability);
        }
        return this;
    }

    public ItemBuilder enchant(Enchantment enchantment, int level) {
        if (this.meta != null) {
            this.meta.addEnchant(enchantment, level, true);
        }
        return this;
    }

    public ItemBuilder unenchant(Enchantment... enchantments) {
        if (this.meta != null) {
            for (Enchantment enchantment : enchantments) {
                this.meta.removeEnchant(enchantment);
            }
        }
        return this;
    }

    public ItemBuilder attribute(Attribute attribute, AttributeModifier modifier) {
        if (this.meta != null) {
            this.meta.addAttributeModifier(attribute, modifier);
        }
        return this;
    }

    public ItemBuilder unattribute(Attribute... attributes) {
        if (this.meta != null) {
            for (Attribute attribute : attributes) {
                this.meta.removeAttributeModifier(attribute);
            }
        }
        return this;
    }

    public ItemBuilder flag(ItemFlag... flag) {
        if (this.meta != null) {
            this.meta.addItemFlags(flag);
        }
        return this;
    }

    public ItemBuilder skull(String texture) {
        if (this.meta instanceof SkullMeta skullMeta) {
            UUID uuid = UUID.randomUUID();
            GameProfile profile = new GameProfile(uuid, uuid.toString());
            profile.getProperties().put("textures", new Property("textures", texture));
            skullMeta.setPlayerProfile(new CraftPlayerProfile(profile));
        }
        return this;
    }

    public ItemBuilder deflag(ItemFlag... flag) {
        if (this.meta != null) {
            this.meta.removeItemFlags(flag);
        }
        return this;
    }

    public ItemStack build() {
        if (this.meta != null) {
            this.item.setItemMeta(this.meta);
        }
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
