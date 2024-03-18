package dev.ckateptb.minecraft.jyraf.config.serializer.item.attribute;

import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.UUID;

public class AttributeModifierSerializer implements TypeSerializer<AttributeModifier> {
    @Override
    public AttributeModifier deserialize(Type type, ConfigurationNode node) throws SerializationException {
        UUID uuid = Optional.ofNullable(node.node("uuid").get(UUID.class)).orElse(UUID.randomUUID());
        String name = node.node("name").getString();
        if (name == null)
            throw new RuntimeException("Failed to deserialize AttributeModifier. The 'name' field is missing.");
        AttributeModifier.Operation operation = node.node("operation").get(AttributeModifier.Operation.class);
        if (operation == null)
            throw new RuntimeException("Failed to deserialize AttributeModifier. The 'operation' field is missing.");
        double amount = node.node("amount").getDouble();
        if (node.hasChild("slot")) {
            EquipmentSlot slot = node.node("slot").get(EquipmentSlot.class);
            return new AttributeModifier(uuid, name, amount, operation, slot);
        } else return new AttributeModifier(uuid, name, amount, operation);
    }

    @Override
    public void serialize(Type type, @Nullable AttributeModifier modifier, ConfigurationNode node) throws SerializationException {
        if (modifier == null) return;
        node.node("uuid").set(modifier.getUniqueId());
        node.node("name").set(modifier.getName());
        node.node("operation").set(modifier.getOperation());
        node.node("amount").set(modifier.getAmount());
        EquipmentSlot slot = modifier.getSlot();
        if (slot != null) {
            node.node("slot").set(slot);
        }
    }
}
