package dev.ckateptb.minecraft.jyraf.builder.skull;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import dev.ckateptb.minecraft.jyraf.builder.item.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

public class SkullBuilder extends ItemBuilder<SkullBuilder> {

    private static final Pattern BASE64_PATTERN = Pattern.compile("^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$");

    public SkullBuilder() {
        super(new ItemStack(Material.PLAYER_HEAD));
    }

    public SkullBuilder urlTexture(String url) {
        return urlTexture(url, UUID.randomUUID());
    }

    public SkullBuilder urlTexture(String url, UUID profileUUID) {
        if (url.isBlank()) return this;
        url = url.toLowerCase(Locale.ROOT);

        if (!url.startsWith("http://") && !url.startsWith("https://")) return this;
        String encoded = Base64.getEncoder().encodeToString(String.format("{textures:{SKIN:{url:\"%s\"}}}", url).getBytes(StandardCharsets.UTF_8));

        return texture(encoded, profileUUID);
    }

    public SkullBuilder texture(String texture) {
        return texture(texture, UUID.randomUUID());
    }

    public SkullBuilder texture(String texture, UUID profileUUID) {
        if (!(getMeta() instanceof SkullMeta meta)) return this;
        if (!BASE64_PATTERN.matcher(texture).find())
            throw new IllegalArgumentException("Invalid base64 texture: " + texture);
        PlayerProfile profile = Bukkit.createProfile(profileUUID);
        profile.getProperties().add(new ProfileProperty("textures", texture));
        meta.setPlayerProfile(profile);

        return this;
    }

    public SkullBuilder owner(UUID uuid) {
        if (!(getMeta() instanceof SkullMeta meta)) return this;
        meta.setOwningPlayer(Bukkit.getOfflinePlayer(uuid));
        return this;
    }

    @Override
    public ItemStack build() {
        if (getMeta() == null) return getItem();
        getItem().setItemMeta(getMeta());

        return getItem();
    }

}