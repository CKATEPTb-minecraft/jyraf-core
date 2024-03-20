package dev.ckateptb.minecraft.jyraf.packet.entity.skin;

import com.destroystokyo.paper.profile.CraftPlayerProfile;
import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.io.CharStreams;
import dev.ckateptb.minecraft.packetevents.api.protocol.player.TextureProperty;
import lombok.SneakyThrows;
import org.bukkit.entity.Player;
import org.joor.Reflect;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Skin {
    private static final AsyncCache<String, List<TextureProperty>> CACHE = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(1))
            .buildAsync();

    public static Mono<List<TextureProperty>> from(Player player) {
        return Mono.fromFuture(CACHE.get(player.getName(), key -> {
            if (!(player.getPlayerProfile() instanceof CraftPlayerProfile profile)) return new ArrayList<>();
            return profile.getGameProfile().getProperties().values().stream()
                    .map(property -> {
                        Reflect reflect = Reflect.on(property);
                        String name = reflect.field("name").as(String.class);
                        String value = reflect.field("value").as(String.class);
                        String signature = reflect.field("signature").as(String.class);
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
        return Mono.defer(() -> {
                    HttpURLConnection connection = postConnection(
                            "https://api.mineskin.org/generate/upload" + (slim ? "?model=slim" : "")
                    );
                    writeFile(connection, file, slim);
                    JSONObject data = readResponse(connection);
                    connection.disconnect();
                    return Mono.justOrEmpty(data);
                })
                .publishOn(Schedulers.single())
                .subscribeOn(Schedulers.single())
                .flatMap(jsonObject -> {
                    JSONObject texture = (JSONObject) jsonObject.get("texture");
                    String textureEncoded = (String) texture.get("value");
                    String signature = (String) texture.get("signature");
                    return from(textureEncoded, signature);
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
    private static JSONObject readResponse(HttpURLConnection connection) {
        try (InputStreamReader reader = new InputStreamReader(connection.getInputStream())) {
            String str = CharStreams.toString(reader);
            if (connection.getResponseCode() != 200) {
                throw new RuntimeException("Failed to fetch skin.");
            }
            JSONObject output = (JSONObject) new JSONParser().parse(str);
            return (JSONObject) output.get("data");
        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
