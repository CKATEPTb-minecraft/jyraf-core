package dev.ckateptb.minecraft.jyraf.config.serializer.menu.frame;

import dev.ckateptb.minecraft.jyraf.menu.Menu;
import dev.ckateptb.minecraft.jyraf.menu.frame.Frame;
import dev.ckateptb.minecraft.jyraf.menu.frame.button.ButtonFrameRPC;
import dev.ckateptb.minecraft.jyraf.menu.frame.conditional.ConditionalFrameRPC;
import dev.ckateptb.minecraft.jyraf.menu.frame.item.ItemFrame;
import dev.ckateptb.minecraft.jyraf.menu.frame.pageable.PageableFrame;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.Objects;

public class FrameSerializer implements TypeSerializer<Frame> {
    @Override
    public Frame deserialize(Type token, ConfigurationNode node) throws SerializationException {
        return switch (Objects.requireNonNull(node.node("type").get(FrameType.class))) {
            case ITEM -> Menu.builder().frame().item(node.node("item").get(ItemStack.class));
            case PAGINATION -> Menu.builder().frame().pagination(builder -> {
                try {
                    builder.addFrames(Objects.requireNonNull(node.node("frames")
                                    .getList(Frame.class))
                            .toArray(Frame[]::new));
                } catch (SerializationException e) {
                    throw new RuntimeException(e);
                }
            });
            case BUTTON -> {
                ButtonFrameRPC button = new ButtonFrameRPC(node.node("handler").get(String[].class));
                button.setItem(node.node("item").get(ItemStack.class));
                yield button;
            }
            case CONDITION -> new ConditionalFrameRPC(node.node("success").get(Frame.class),
                    node.node("failed").get(Frame.class),
                    node.node("condition").get(String[].class)
            );
        };
    }

    @Override
    public void serialize(Type token, @Nullable Frame frame, ConfigurationNode node) throws SerializationException {
        ConfigurationNode type = node.node("type");
        if (frame instanceof ItemFrame item) {
            if (item instanceof ButtonFrameRPC button) {
                type.set(FrameType.BUTTON);
                this.serializeButton(token, button, node);
            } else {
                type.set(FrameType.ITEM);
            }
            this.serializeItem(token, item, node);
        } else if (frame instanceof ConditionalFrameRPC condition) {
            type.set(FrameType.CONDITION);
            this.serializeCondition(token, condition, node);
        } else if (frame instanceof PageableFrame pagination) {
            type.set(FrameType.PAGINATION);
            this.serializePagination(token, pagination, node);
        }
    }

    private void serializeItem(Type token, ItemFrame frame, ConfigurationNode node) throws SerializationException {
        node.node("item").set(frame.getItem());
    }

    private void serializeButton(Type token, ButtonFrameRPC frame, ConfigurationNode node) throws SerializationException {
        node.node("handler").set(frame.getRPC());
    }

    private void serializeCondition(Type token, ConditionalFrameRPC frame, ConfigurationNode node) throws SerializationException {
        node.node("success").set(frame.getSuccess());
        node.node("failed").set(frame.getFailed());
        node.node("condition").set(frame.getRPC());
    }

    private void serializePagination(Type token, PageableFrame frame, ConfigurationNode node) throws SerializationException {
        node.node("frames").setList(Frame.class, frame.getFrames());
    }


    public enum FrameType {
        ITEM,
        BUTTON,
        CONDITION,
        PAGINATION
    }
}
