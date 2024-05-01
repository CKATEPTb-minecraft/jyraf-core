package dev.ckateptb.minecraft.jyraf.packet.entity.skin;

import com.destroystokyo.paper.profile.CraftPlayerProfile;
import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.retrooper.packetevents.protocol.player.TextureProperty;
import com.google.common.io.CharStreams;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.SneakyThrows;
import org.bukkit.entity.Player;
import org.joor.Reflect;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class Skin {
    private static final Gson gson = new Gson();
    private static final AsyncCache<String, List<TextureProperty>> CACHE = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(1))
            .buildAsync();

    public static Mono<List<TextureProperty>> from(Player player) {
        return Mono.fromFuture(CACHE.get(player.getName(), key -> {
            if (!(player.getPlayerProfile() instanceof CraftPlayerProfile profile)) return new ArrayList<>();
            return profile.getGameProfile().getProperties().values().stream()
                    .map(property -> {
                        Reflect reflect = Reflect.on(property);
                        String name = reflect.get("name");
                        String value = reflect.get("value");
                        String signature = reflect.get("signature");
                        return new TextureProperty(name, value, signature);
                    })
                    .collect(Collectors.toList());
        }));
    }

    public static Mono<List<TextureProperty>> from(String texture, String signature) {
        return Mono.fromFuture(CACHE.get(texture + signature, key ->
                List.of(new TextureProperty("textures", texture, signature))));
    }

    @SneakyThrows
    public static Mono<List<TextureProperty>> from(File file, boolean slim) {
        byte[] bytes = Files.readAllBytes(file.toPath());
        byte[] hash = MessageDigest.getInstance("MD5").digest(bytes);
        String checksum = new BigInteger(1, hash).toString(16);
        CompletableFuture<List<TextureProperty>> future = CACHE.getIfPresent(checksum);
        if (future != null) return Mono.fromFuture(future);
        return Mono.defer(() -> {
                    HttpURLConnection connection = postConnection(
                            "https://api.mineskin.org/generate/upload" + (slim ? "?model=slim" : "")
                    );
                    writeFile(connection, file, slim);
                    JsonObject data = readResponse(connection);
                    connection.disconnect();
                    return Mono.justOrEmpty(data);
                })
                .publishOn(Schedulers.single())
                .subscribeOn(Schedulers.single())
                .flatMap(jsonObject -> {
                    JsonObject texture = jsonObject.getAsJsonObject("texture");
                    String textureEncoded = texture.get("value").getAsString();
                    String signature = texture.get("signature").getAsString();
                    return from(textureEncoded, signature).doOnNext(textureProperties -> {
                        CACHE.put(checksum, CompletableFuture.completedFuture(textureProperties));
                    });
                });
    }

    @SneakyThrows
    private static HttpURLConnection postConnection(String url) {
        URL target = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) target.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36");
        connection.setRequestProperty("Cache-Control", "no-cache");
        connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=*****");
        connection.setConnectTimeout(1000);
        connection.setReadTimeout(30000);
        return connection;
    }

    private static void writeFile(HttpURLConnection connection, File file, boolean slim) {
        try (DataOutputStream stream = new DataOutputStream(connection.getOutputStream())) {
            stream.writeBytes("--*****\r\n");
            stream.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"skin.png\"\r\n");
            stream.writeBytes("Content-Type: image/png\r\n\r\n");
            stream.write(Files.readAllBytes(file.toPath()));
            stream.writeBytes("\r\n");
            stream.writeBytes("--*****\r\n");
            stream.writeBytes("Content-Disposition: form-data; name=\"name\";\r\n\r\n\r\n");
            if (slim) {
                stream.writeBytes("--*****\r\n");
                stream.writeBytes("Content-Disposition: form-data; name=\"variant\";\r\n\r\n");
                stream.writeBytes("slim\r\n");
            }
            stream.writeBytes("--*****--\r\n");
            stream.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    private static JsonObject readResponse(HttpURLConnection connection) {
        try (InputStreamReader reader = new InputStreamReader(connection.getInputStream())) {
            String str = CharStreams.toString(reader);
            if (connection.getResponseCode() != 200) {
                throw new RuntimeException("Failed to fetch skin.");
            }
            JsonObject output = gson.fromJson(str, JsonObject.class);
            return output.getAsJsonObject("data");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
