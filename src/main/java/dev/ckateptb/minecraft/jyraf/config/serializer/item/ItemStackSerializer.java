package dev.ckateptb.minecraft.jyraf.config.serializer.item;

import com.destroystokyo.paper.profile.CraftPlayerProfile;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import dev.ckateptb.minecraft.jyraf.builder.item.ItemBuilder;
import dev.ckateptb.minecraft.jyraf.component.Text;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.joor.Reflect;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public class ItemStackSerializer implements TypeSerializer<ItemStack> {
    private final ItemStack empty = new ItemStack(Material.AIR);
    private final String allowedEnchantments = Arrays.stream(Enchantment.values())
            .map(enchantment -> enchantment.getKey().getKey()).collect(Collectors.joining(", "));
    private final String allowedFlags = Arrays.stream(ItemFlag.values())
            .map(ItemFlag::name).collect(Collectors.joining(", "));

    @Override
    public ItemStack deserialize(Type token, ConfigurationNode node) throws SerializationException {
        ItemBuilder builder = new ItemBuilder(Material.valueOf(node.node("type").getString()))
                .amount(node.node("amount").getInt())
                .name(node.node("name").getString())
                .lore(node.node("lore").getList(String.class));
        ConfigurationNode enchantsNode = node.node("enchants");
        if (enchantsNode.isMap()) {
            Map<Object, ? extends ConfigurationNode> map = enchantsNode.childrenMap();
            map.forEach((key, configurationNode) -> {
                Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft((String) key));
                if (enchantment != null) {
                    int level = configurationNode.getInt();
                    builder.enchant(enchantment, level);
                }
            });
        }
        builder.flag(Optional.ofNullable(node.node("flags").getList(String.class))
                .orElse(Collections.emptyList()).stream().map(ItemFlag::valueOf).toArray(ItemFlag[]::new));
        if (node.hasChild("data")) {
            builder.durability((short) node.node("data").getInt());
        }
        if (node.hasChild("skull")) {
            builder.skull(node.node("skull").getString());
        }
        return builder.build();
    }

    @Override
    public void serialize(Type type, @Nullable ItemStack item, ConfigurationNode node) throws SerializationException {
        if (item == null) item = this.empty;
        node.node("type").set(item.getType().toString());
        node.node("amount").set(item.getAmount());
        ItemMeta meta = item.getItemMeta();
        node.node("name").set(Text.of(meta.displayName()));
        node.node("lore").setList(String.class, Optional.ofNullable(meta.lore()).orElse(Collections.emptyList()).stream().map(Text::of).toList());
        ConfigurationNode enchants = node.node("enchants");
        if (enchants instanceof CommentedConfigurationNode commented) {
            commented.comment("Allowed options " + this.allowedEnchantments);
        }
        meta.getEnchants()
                .forEach((key, value) -> {
                    try {
                        enchants.node(key.getKey().getKey()).set(value);
                    } catch (SerializationException e) {
                        throw new RuntimeException(e);
                    }
                });
        ConfigurationNode flags = node.node("flags");
        if (flags instanceof CommentedConfigurationNode commented) {
            commented.comment("Allowed options " + this.allowedFlags);
        }
        flags.setList(String.class, meta.getItemFlags().stream().map(ItemFlag::name).collect(Collectors.toList()));
        if (meta instanceof Damageable damageable) {
            node.node("data").set(damageable.getDamage());
        }
        if (meta instanceof SkullMeta skullMeta && skullMeta.hasOwner()) {
            PlayerProfile playerProfile = skullMeta.getPlayerProfile();
            if (playerProfile instanceof CraftPlayerProfile craftProfile) {
                GameProfile profile = craftProfile.getGameProfile();
                PropertyMap properties = profile.getProperties();
                if (properties.containsKey("textures")) {
                    Iterator<Property> iterator = properties.get("textures").iterator();
                    if (iterator.hasNext()) {
                        Property property = iterator.next();
                        node.node("skull").set(Reflect.on(property)
                                .as(PropertyProxy.class)
                                .value());
                    }
                }
            }
        }
    }

    @Override
    public @Nullable ItemStack emptyValue(Type specificType, ConfigurationOptions options) {
        return this.empty.clone();
    }

    // Necessary to support older versions of authlib, which are implemented without record
    public interface PropertyProxy {
        String getValue();

        default String value() {
            return this.getValue();
        }
    }
}
