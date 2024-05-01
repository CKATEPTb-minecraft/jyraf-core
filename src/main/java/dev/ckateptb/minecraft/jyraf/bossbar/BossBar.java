package dev.ckateptb.minecraft.jyraf.bossbar;

import dev.ckateptb.minecraft.jyraf.component.Text;
import dev.ckateptb.minecraft.jyraf.packet.basic.Displayable;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.apache.commons.math3.util.FastMath;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Getter
public class BossBar extends Displayable {

    private final UUID uid = UUID.randomUUID();
    private final @NotNull net.kyori.adventure.bossbar.BossBar kyoriBossBar;
    private final @NotNull Set<net.kyori.adventure.bossbar.BossBar.Flag> flags = Collections.synchronizedSet(new HashSet<>());
    private @NotNull Component title;
    private int progress;
    private @NotNull net.kyori.adventure.bossbar.BossBar.Overlay overlay;
    private @NotNull net.kyori.adventure.bossbar.BossBar.Color color;
    private @Nullable net.kyori.adventure.bossbar.BossBar.Listener listener;

    public BossBar(@NotNull Component title, int progress, @NotNull net.kyori.adventure.bossbar.BossBar.Overlay overlay, @NotNull net.kyori.adventure.bossbar.BossBar.Color color, @NotNull Set<net.kyori.adventure.bossbar.BossBar.Flag> flags, @NotNull Location location, boolean global, @NotNull Collection<Player> allowedViewers) {
        super(location, allowedViewers);
        Objects.requireNonNull(title);
        Objects.requireNonNull(overlay);
        Objects.requireNonNull(color);
        Objects.requireNonNull(flags);
        this.title = title;
        this.progress = progress;
        this.overlay = overlay;
        this.color = color;
        this.global = global;
        this.flags.addAll(flags);
        this.kyoriBossBar = net.kyori.adventure.bossbar.BossBar.bossBar(title, FastMath.max(0.0f, FastMath.min(progress / 100.0f, 1.0f)), color, overlay, flags);
    }

    public void setListener(@Nullable net.kyori.adventure.bossbar.BossBar.Listener listener) {
        if (this.listener != null && this.listener.equals(listener)) return;
        this.kyoriBossBar.removeListener(this.listener);
        this.listener = listener;
        if (listener != null) this.kyoriBossBar.addListener(this.listener);
    }

    public void removeFlag(@NotNull net.kyori.adventure.bossbar.BossBar.Flag... flags) {
        Objects.requireNonNull(flags);
        this.kyoriBossBar.removeFlags(flags);
    }

    public void addFlag(@NotNull net.kyori.adventure.bossbar.BossBar.Flag... flags) {
        Objects.requireNonNull(flags);
        this.kyoriBossBar.addFlags(flags);
    }

    public void setTitle(@NotNull Component title) {
        Objects.requireNonNull(title);
        this.title = title;
        this.kyoriBossBar.name(title);
    }

    public void setTitle(@NotNull String title) {
        Objects.requireNonNull(title);
        this.title = Text.of(title);
        this.kyoriBossBar.name(this.title);
    }

    public void setProgress(int progress) {
        if (this.progress == progress) return;
        this.progress = progress;
        this.kyoriBossBar.progress(FastMath.max(0.0f, FastMath.min(progress / 100.0f, 1.0f)));
    }

    public void setProgress(float progress) {
        int targetProgress = (int) (progress * 100);
        if (this.progress == targetProgress) return;
        this.progress = targetProgress;
        this.kyoriBossBar.progress(FastMath.max(0.0f, FastMath.min(progress, 1.0f)));
    }

    public void setOverlay(@NotNull net.kyori.adventure.bossbar.BossBar.Overlay overlay) {
        Objects.requireNonNull(overlay);
        if (this.overlay.equals(overlay)) return;
        this.overlay = overlay;
        this.kyoriBossBar.overlay(overlay);
    }

    public void setColor(@NotNull net.kyori.adventure.bossbar.BossBar.Color color) {
        Objects.requireNonNull(color);
        if (this.color.equals(color)) return;
        this.color = color;
        this.kyoriBossBar.color(color);
    }

    public @NotNull World getWorld() {
        return this.location.getWorld();
    }

    public void setLocation(@NotNull Location location) {
        Objects.requireNonNull(location);
        if (this.location.equals(location)) return;
        this.location = location;
    }

    @Override
    public void destroy(Player player) {
        player.hideBossBar(this.kyoriBossBar);
    }

    @Override
    public void display(Player player) {
        player.showBossBar(this.kyoriBossBar);
    }

    public static final class Builder {
        private final BossBar bossBar;

        public Builder(@NotNull Location location, @NotNull String title) {
            this(location, Text.of(title));
        }

        public Builder(@NotNull Location location, @NotNull Component title) {
            this.bossBar = new BossBar(title, 0, net.kyori.adventure.bossbar.BossBar.Overlay.PROGRESS, net.kyori.adventure.bossbar.BossBar.Color.RED, new HashSet<>(), location, true, new ArrayList<>());
        }

        public @NotNull Builder color(@NotNull net.kyori.adventure.bossbar.BossBar.Color color) {
            Objects.requireNonNull(color);
            this.bossBar.setColor(color);
            return this;
        }

        public @NotNull Builder overlay(@NotNull net.kyori.adventure.bossbar.BossBar.Overlay overlay) {
            Objects.requireNonNull(overlay);
            this.bossBar.setOverlay(overlay);
            return this;
        }

        public @NotNull Builder listener(@NotNull net.kyori.adventure.bossbar.BossBar.Listener listener) {
            Objects.requireNonNull(listener);
            this.bossBar.setListener(listener);
            return this;
        }

        public @NotNull Builder global(boolean global) {
            this.bossBar.setGlobal(global);
            return this;
        }

        public @NotNull Builder viewers(@NotNull Player... viewers) {
            Objects.requireNonNull(viewers);
            this.bossBar.setGlobal(false);
            this.bossBar.allowedViewers.addAll(Arrays.stream(viewers).toList());
            return this;
        }

        public @NotNull Builder progress(int progress) {
            this.bossBar.setProgress(progress);
            return this;
        }

        public @NotNull Builder progress(float progress) {
            this.bossBar.setProgress(progress);
            return this;
        }

        public @NotNull Builder location(@NotNull Location location) {
            Objects.requireNonNull(location);
            this.bossBar.setLocation(location);
            return this;
        }

        public @NotNull Builder title(@NotNull String title) {
            Objects.requireNonNull(title);
            this.bossBar.setTitle(title);
            return this;
        }

        public @NotNull Builder title(@NotNull Component title) {
            Objects.requireNonNull(title);
            this.bossBar.setTitle(title);
            return this;
        }

        public @NotNull BossBar build() {
            return this.bossBar;
        }
    }

}