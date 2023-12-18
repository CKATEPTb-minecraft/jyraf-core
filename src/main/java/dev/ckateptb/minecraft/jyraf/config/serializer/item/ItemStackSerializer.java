package dev.ckateptb.minecraft.jyraf.config.serializer.item;

import com.destroystokyo.paper.profile.CraftPlayerProfile;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
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
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public class ItemStackSerializer implements TypeSerializer<ItemStack> {
    private final ItemStack EMPTY = new ItemStack(Material.AIR);

    @Override
    public ItemStack deserialize(Type token, ConfigurationNode node) throws SerializationException {
        ItemStack itemStack = new ItemStack(Material.valueOf(node.node("type").getString()));
        itemStack.setAmount(node.node("amount").getInt());
        ItemMeta meta = itemStack.getItemMeta();
        meta.displayName(Text.of(node.node("name").getString()));
        meta.lore(Optional.ofNullable(node.node("lore").getList(String.class))
                .orElse(Collections.emptyList()).stream().map(Text::of).toList());
        ConfigurationNode enchantsNode = node.node("enchants");
        if (enchantsNode.isMap()) {
            Map<Object, ? extends ConfigurationNode> map = enchantsNode.childrenMap();
            map.forEach((key, configurationNode) -> {
                Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft((String) key));
                if (enchantment != null) {
                    int level = configurationNode.getInt();
                    meta.addEnchant(enchantment, level, true);
                }
            });
        }
        meta.addItemFlags(Optional.ofNullable(node.node("flags").getList(String.class))
                .orElse(Collections.emptyList()).stream().map(ItemFlag::valueOf).toArray(ItemFlag[]::new));
        if (node.hasChild("data") && meta instanceof Damageable damageable) {
            damageable.setDamage(node.node("data").getInt());
        }
        if (node.hasChild("skull") && meta instanceof SkullMeta skullMeta) {
            String textures = node.node("skull").getString();
            GameProfile profile = new GameProfile(UUID.randomUUID(), null);
            profile.getProperties().put("textures", new Property("textures", textures));
            skullMeta.setPlayerProfile(new CraftPlayerProfile(profile));
        }
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    @Override
    public void serialize(Type type, @Nullable ItemStack item, ConfigurationNode node) throws SerializationException {
        if (item == null) item = this.EMPTY;
        node.node("type").set(item.getType().toString());
        node.node("amount").set(item.getAmount());
        ItemMeta meta = item.getItemMeta();
        node.node("name").set(Text.of(meta.displayName()));
        node.node("lore").setList(String.class, Optional.ofNullable(meta.lore()).orElse(Collections.emptyList()).stream().map(Text::of).toList());
        ConfigurationNode enchants = node.node("enchants");
        meta.getEnchants()
                .forEach((key, value) -> {
                    try {
                        enchants.node(key.getKey().getKey()).set(value);
                    } catch (SerializationException e) {
                        throw new RuntimeException(e);
                    }
                });
        node.node("flags").setList(String.class, meta.getItemFlags().stream().map(ItemFlag::name).collect(Collectors.toList()));
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
                        node.node("skull").set(property.getValue());
                    }
                }
            }
        }
    }

    @Override
    public @Nullable ItemStack emptyValue(Type specificType, ConfigurationOptions options) {
        return this.EMPTY.clone();
    }
}
