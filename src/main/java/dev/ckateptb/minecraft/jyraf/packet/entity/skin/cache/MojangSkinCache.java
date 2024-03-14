package dev.ckateptb.minecraft.jyraf.packet.entity.skin.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.ckateptb.minecraft.jyraf.container.annotation.Component;
import dev.ckateptb.minecraft.jyraf.packet.entity.skin.Skin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.joor.Reflect;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CompletableFuture;

@Component
public class MojangSkinCache {
    private final Cache<String, Skin> cache = Caffeine.newBuilder().expireAfterWrite(Duration.of(1, ChronoUnit.MINUTES)).build();
    private final Cache<String, CachedId> idCache = Caffeine.newBuilder().expireAfterWrite(Duration.of(1, ChronoUnit.MINUTES)).build();

    public CompletableFuture<Skin> fetchByName(String name) {
        Player player = Bukkit.getPlayerExact(name);
        if (player != null && player.isOnline()) return CompletableFuture.completedFuture(getFromPlayer(player));

        CachedId cachedId = idCache.getIfPresent(name.toLowerCase());
        if (cachedId != null) return fetchByUUID(cachedId.getId());

        return CompletableFuture.supplyAsync(() -> {
            URL url = parseUrl("https://api.mojang.com/users/profiles/minecraft/" + name);
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                try (Reader reader = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)) {
                    JsonObject obj = JsonParser.parseReader(reader).getAsJsonObject();
                    if (obj.has("errorMessage")) return fetchByNameFallback(name).join();
                    String id = obj.get("id").getAsString();
                    idCache.put(name.toLowerCase(), new CachedId(id));
                    Skin skin = fetchByUUID(id).join();
                    if (skin == null) return fetchByNameFallback(name).join();
                    return skin;
                }
            } catch (IOException exception) {
                return fetchByNameFallback(name).join();
            } finally {
                if (connection != null) connection.disconnect();
            }
        });
    }

    public CompletableFuture<Skin> fetchByNameFallback(String name) {
        Player player = Bukkit.getPlayerExact(name);
        if (player != null && player.isOnline()) return CompletableFuture.completedFuture(getFromPlayer(player));
        CachedId cachedId = idCache.getIfPresent(name.toLowerCase());
        if (cachedId != null) return fetchByUUID(cachedId.getId());

        return CompletableFuture.supplyAsync(() -> {
            URL url = parseUrl("https://api.ashcon.app/mojang/v2/user/" + name);
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                try (Reader reader = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)) {
                    JsonObject obj = JsonParser.parseReader(reader).getAsJsonObject();
                    if (obj.has("error")) return null;
                    String uuid = obj.get("uuid").getAsString();
                    idCache.put(name.toLowerCase(), new CachedId(uuid));
                    JsonObject textures = obj.get("textures").getAsJsonObject();
                    String value = textures.get("raw").getAsJsonObject().get("value").getAsString();
                    String signature = textures.get("raw").getAsJsonObject().get("signature").getAsString();
                    Skin skin = new Skin(value, signature);
                    cache.put(uuid, skin);
                    return skin;
                }
            } catch (IOException ignored) {
            } finally {
                if (connection != null) connection.disconnect();
            }
            return null;
        });
    }

    public CompletableFuture<Skin> fetchByUrl(URL url, String variant) {
        return CompletableFuture.supplyAsync(() -> {
            URL apiUrl = parseUrl("https://api.mineskin.org/generate/url");
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) apiUrl.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("accept", "application/json");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                OutputStream outStream = connection.getOutputStream();
                DataOutputStream out = new DataOutputStream(outStream);
                out.writeBytes("{\"variant\":\"" + variant + "\",\"url\":\"" + url.toString() + "\"}");
                out.flush();
                out.close();
                outStream.close();

                try (Reader reader = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)) {
                    JsonObject obj = JsonParser.parseReader(reader).getAsJsonObject();
                    if (obj.has("error")) return null;
                    if (!obj.has("data")) return null;
                    JsonObject texture = obj.get("data").getAsJsonObject().get("texture").getAsJsonObject();
                    return new Skin(texture.get("value").getAsString(), texture.get("signature").getAsString());
                }

            } catch (IOException ignored) {
            } finally {
                if (connection != null) connection.disconnect();
            }
            return null;
        });
    }

    public boolean isNameFullyCached(String s) {
        String name = s.toLowerCase();
        CachedId id = idCache.getIfPresent(name);
        if (id == null) return false;
        Skin skin = cache.getIfPresent(id.getId());
        if (id.isExpired() || skin == null) return false;
        return !skin.isExpired();
    }

    public Skin getFullyCachedByName(String s) {
        String name = s.toLowerCase();
        CachedId id = idCache.getIfPresent(name);
        if (id == null) return null;
        Skin skin = cache.getIfPresent(id.getId());
        if (id.isExpired() || skin == null) return null;
        if (skin.isExpired()) return null;
        return skin;
    }

    public CompletableFuture<Skin> fetchByUUID(String uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null && player.isOnline()) return CompletableFuture.completedFuture(getFromPlayer(player));

        Skin skin = cache.getIfPresent(uuid);
        if (skin != null) {
            if (!skin.isExpired()) return CompletableFuture.completedFuture(skin);
        }

        return CompletableFuture.supplyAsync(() -> {
            URL url = parseUrl("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false");
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                try (Reader reader = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)) {
                    JsonObject obj = JsonParser.parseReader(reader).getAsJsonObject();
                    if (obj.has("errorMessage")) return null;
                    return cache.get(uuid, key -> new Skin(obj));
                }
            } catch (IOException ignored) {
            } finally {
                if (connection != null) connection.disconnect();
            }
            return null;
        });
    }

    public Skin getFromPlayer(Player player) {
        Object properties = Reflect.on(player)
                .call("getHandle")
                .call("getGameProfile")
                .call("getProperties")
                .get();
        return new Skin(properties);
    }

    private static URL parseUrl(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException exception) {
            throw new RuntimeException(exception);
        }
    }
}