package dev.ckateptb.minecraft.jyraf.config.serializer.menu;

import dev.ckateptb.minecraft.jyraf.menu.Menu;
import dev.ckateptb.minecraft.jyraf.menu.chest.ChestMenu;
import dev.ckateptb.minecraft.jyraf.menu.frame.Frame;
import org.apache.commons.lang3.Validate;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MenuSerializer implements TypeSerializer<Menu> {
    @Override
    public Menu deserialize(Type token, ConfigurationNode node) throws SerializationException {
        MenuType type = node.node("type").get(MenuType.class);
        Validate.isTrue(type != null);
        return switch (type) {
            case CHEST -> {
                String title = node.node("title").getString();
                List<String> template = node.node("template").getList(String.class);
                Validate.isTrue(template != null);
                ConfigurationNode frameNode = node.node("frames");
                Map<String, Frame> map = new HashMap<>();
                yield Menu.builder().chest(title, template.size())
                        .editable(false)
                        .closable(true)
                        .updateContext(context -> {
                            AtomicInteger counter = new AtomicInteger();
                            for (String f : String.join("", template).split("|")) {
                                if (f.equalsIgnoreCase(" ")) {
                                    counter.getAndIncrement();
                                    continue;
                                }
                                Frame frame = map.computeIfAbsent(f, string -> {
                                    try {
                                        return frameNode.node(f).get(Frame.class);
                                    } catch (SerializationException e) {
                                        throw new RuntimeException(e);
                                    }
                                });
                                context.set(counter.getAndIncrement(), frame);
                            }
                        })
                        .build();
            }
        };
    }

    @Override
    public void serialize(Type token, @Nullable Menu menu, ConfigurationNode node) throws SerializationException {
        if (menu instanceof ChestMenu chestMenu) {
            node.node("type").set(MenuType.CHEST);
            this.serializeChest(token, chestMenu, node);
        }
    }

    public void serializeChest(Type token, ChestMenu menu, ConfigurationNode node) throws SerializationException {
        Iterator<String> iterator = Arrays.asList(
                "QWERTYUIOPASDFGHJKLZXCVBNMqwertyuiopasdfghjklzxcvbnmЙЦ".split("|")
        ).iterator();
        node.node("title").set(menu.getTitle());
        Frame[] frames = menu.getFrames();
        Map<Frame, String> frameKey = Arrays.stream(frames)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet())
                .stream()
                .collect(Collectors.toMap(key -> key, value -> iterator.next()));
        ConfigurationNode framesNode = node.node("frames");
        frameKey.forEach((frame, string) -> {
            try {
                framesNode.node(string).set(frame);
            } catch (SerializationException e) {
                throw new RuntimeException(e);
            }
        });
        List<String> frameTemplate = Pattern.compile(".{1,9}")
                .matcher(Arrays.stream(frames).map(frame -> {
                            if (frame == null) return " ";
                            return frameKey.get(frame);
                        })
                        .collect(Collectors.joining("")))
                .results()
                .map(MatchResult::group)
                .collect(Collectors.toList());
        System.out.println(frameTemplate);
        node.node("template").setList(String.class, frameTemplate);
    }

    private enum MenuType {
        CHEST
    }
}
