package dev.ckateptb.minecraft.jyraf.config.serializer.item;

import com.destroystokyo.paper.profile.CraftPlayerProfile;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import dev.ckateptb.minecraft.jyraf.builder.item.ItemBuilder;
import dev.ckateptb.minecraft.jyraf.builder.item.potion.PotionBuilder;
import dev.ckateptb.minecraft.jyraf.component.Text;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
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
import java.util.stream.StreamSupport;

public class ItemStackSerializer implements TypeSerializer<ItemStack> {
    private final ItemStack empty = new ItemStack(Material.AIR);
    private final String allowedEnchantments = StreamSupport.stream(Registry.ENCHANTMENT.spliterator(), false)
            .map(enchantment -> enchantment.getKey().getKey()).collect(Collectors.joining(", "));
    private final String allowedAttributes = StreamSupport.stream(Registry.ATTRIBUTE.spliterator(), false)
            .map(attribute -> attribute.getKey().getKey()).collect(Collectors.joining(", "));
    private final String allowedFlags = Arrays.stream(ItemFlag.values())
            .map(ItemFlag::name).collect(Collectors.joining(", "));

    @Override
    public ItemStack deserialize(Type token, ConfigurationNode node) throws SerializationException {
        ItemBuilder<?> builder = new ItemBuilder(Material.valueOf(node.node("type").getString()))
                .amount(node.node("amount").getInt());

        if (node.hasChild("name")) {
            builder.name(node.node("name").getString());
        }

        if (node.hasChild("lore")) {
            builder.lore(node.node("lore").getList(String.class));
        }

        if (node.hasChild("attributes")) {
            ConfigurationNode attributes = node.node("attributes");
            if (attributes.isMap()) {
                Map<Object, ? extends ConfigurationNode> map = attributes.childrenMap();
                map.forEach((key, configurationNode) -> {
                    if (configurationNode.isList()) {
                        try {
                            Attribute attribute = Attribute.valueOf(((String) key).toUpperCase());

                            List<AttributeModifier> list = configurationNode.getList(AttributeModifier.class);
                            if (list == null || list.isEmpty()) return;
                            for (AttributeModifier modifier : list) {
                                builder.attribute(attribute, modifier);
                            }
                        } catch (SerializationException | IllegalArgumentException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
            }
        }

        if (node.hasChild("flags")) {
            builder.flag(Optional.ofNullable(node.node("flags").getList(String.class))
                    .orElse(Collections.emptyList()).stream().map(ItemFlag::valueOf).toArray(ItemFlag[]::new));
        }

        if (node.hasChild("data")) {
            builder.durability((short) node.node("data").getInt());
        }

        if (node.hasChild("skull")) {
            builder.skull(node.node("skull").getString());
        }

        if (node.hasChild("unbreakable")) {
            builder.unbreakable(node.node("unbreakable").getBoolean());
        }

        if (node.hasChild("enchants")) {
            ConfigurationNode enchantsNode = node.node("enchants");
            if (enchantsNode.isMap()) {
                Map<Object, ? extends ConfigurationNode> map = enchantsNode.childrenMap();
                map.forEach((key, configurationNode) -> {
                    Enchantment enchantment = Registry.ENCHANTMENT.get(NamespacedKey.minecraft((String) key));
                    if (enchantment != null) {
                        builder.enchant(enchantment, configurationNode.getInt());
                    }
                });
            }
        }

        if (node.hasChild("potion")) {
            ConfigurationNode potionNode = node.node("potion");
            PotionBuilder potionBuilder = new PotionBuilder(builder.build(), false);

            if (potionNode.hasChild("color")) potionBuilder.color(potionNode.node("color").get(Color.class));
            if (potionNode.hasChild("data"))
                potionBuilder.data(potionNode.node("effect").get(PotionData.class));
            if (potionNode.hasChild("effects"))
                potionBuilder.effect(Objects.requireNonNull(potionNode.node("effects").getList(PotionEffect.class)).toArray(new PotionEffect[0]));

            return potionBuilder.build();
        }

        return builder.build();
    }

    @Override
    public void serialize(Type type, @Nullable ItemStack item, ConfigurationNode node) throws SerializationException {
        if (item == null) item = this.empty;
        node.node("type").set(item.getType().toString());
        node.node("amount").set(item.getAmount());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            Component displayName = meta.displayName();
            if (displayName != null) {
                node.node("name").set(Text.of(displayName));
            }
            node.node("lore").setList(String.class, Optional.ofNullable(meta.lore()).orElse(Collections.emptyList()).stream().map(Text::of).toList());
            ConfigurationNode enchants = node.node("enchants");
            if (enchants instanceof CommentedConfigurationNode commented) {
                commented.comment("Allowed options " + this.allowedEnchantments);
            }

            (meta instanceof EnchantmentStorageMeta book ? book.getStoredEnchants() : meta.getEnchants())
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

            ConfigurationNode attributes = node.node("attributes");
            if (attributes instanceof CommentedConfigurationNode commented) {
                commented.comment("Allowed options " + this.allowedAttributes);
            }
            Multimap<Attribute, AttributeModifier> modifiersMultimap = meta.getAttributeModifiers();
            if (modifiersMultimap != null) {
                modifiersMultimap.asMap()
                        .forEach((key, value) -> {
                            try {
                                attributes.node(key).setList(AttributeModifier.class, new ArrayList<>(value));
                            } catch (SerializationException e) {
                                throw new RuntimeException(e);
                            }
                        });
            }

            if (meta instanceof Damageable damageable) {
                node.node("data").set(damageable.getDamage());
            }

            if (meta instanceof SkullMeta skullMeta) {
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

            if (meta.isUnbreakable()) node.node("unbreakable").set(true);

            if (meta instanceof PotionMeta potionMeta) {
                node.node("potion", "data").set(PotionData.class, potionMeta.getBasePotionData());
                node.node("potion", "effects").setList(PotionEffect.class, potionMeta.getCustomEffects());
                node.node("potion", "color").setList(PotionEffect.class, potionMeta.getCustomEffects());
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
