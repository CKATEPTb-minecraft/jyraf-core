package dev.ckateptb.minecraft.jyraf.builder.item;

import dev.ckateptb.minecraft.jyraf.builder.Builder;
import dev.ckateptb.minecraft.jyraf.component.Text;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

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
        this.meta.lore(lore.stream().map(Text::of).toList());
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
        this.item.addUnsafeEnchantment(enchantment, level);
        return this;
    }

    public ItemBuilder unenchant(Enchantment... enchantments) {
        for (Enchantment enchantment : enchantments) {
            this.item.removeEnchantment(enchantment);
        }
        return this;
    }

    public ItemBuilder flag(ItemFlag... flag) {
        this.meta.addItemFlags(flag);
        return this;
    }

    public ItemBuilder deflag(ItemFlag... flag) {
        this.meta.removeItemFlags(flag);
        return this;
    }

    public ItemStack build() {
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
