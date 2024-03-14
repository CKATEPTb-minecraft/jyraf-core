package dev.ckateptb.minecraft.jyraf.packet.entity.skin;

import com.destroystokyo.paper.profile.CraftPlayerProfile;
import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.retrooper.packetevents.protocol.player.TextureProperty;
import dev.ckateptb.minecraft.jyraf.Jyraf;
import org.bukkit.entity.Player;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Skin {
    private static final AsyncCache<String, List<TextureProperty>> CACHE = Caffeine.newBuilder().expireAfterWrite(Duration.ofMinutes(1))
            .buildAsync();

    public static Mono<List<TextureProperty>> from(Player player) {
        return Mono.fromFuture(CACHE.get(player.getName(), key -> {
            if (!(player.getPlayerProfile() instanceof CraftPlayerProfile profile)) return new ArrayList<>();
            return profile.getGameProfile().getProperties().values().stream()
                    .map(property -> new TextureProperty(property.name(), property.value(), property.signature()))
                    .collect(Collectors.toList());
        }));
    }

    public static Mono<List<TextureProperty>> from(String texture, String signature) {
        return Mono.fromFuture(CACHE.get(texture + signature, key -> List.of(new TextureProperty("textures", texture, signature))));
    }

    public static Mono<List<TextureProperty>> from(File file, boolean slim) {
        return HttpClient.create()
                .post()
                .uri("https://api.mineskin.org/generate/upload")
                .sendForm((httpClientRequest, httpClientForm) -> {
                    httpClientRequest.addHeader("User-Agent", HttpClient.USER_AGENT);
                    httpClientForm.attr("visibility", "1");
                    httpClientForm.attr("variant", slim ? "slim" : "classic");
                    httpClientForm.file("file", file);
                })
                .responseSingle((httpClientResponse, byteBufMono) -> byteBufMono.asString())
                .flatMap(result -> {
                    BasicConfigurationNode node = Jyraf.getGsonMapper().createNode();
                    try {
                        return Mono.just(node.set(result));
                    } catch (SerializationException e) {
                        return Mono.error(e);
                    }
                })
                .flatMap(node -> {
                    if (node.hasChild("error")) {
                        return Mono.error(new RuntimeException(node.node("error").getString()));
                    }
                    BasicConfigurationNode data = node.node("data");
                    BasicConfigurationNode texture = data.node("texture");
                    String value = texture.node("value").getString();
                    String signature = texture.node("signature").getString();
                    return from(value, signature);
                });
    }
}
