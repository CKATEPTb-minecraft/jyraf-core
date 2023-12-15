package dev.ckateptb.minecraft.jyraf.example.config;

import dev.ckateptb.minecraft.jyraf.Jyraf;
import dev.ckateptb.minecraft.jyraf.config.hocon.HoconConfig;
import dev.ckateptb.minecraft.jyraf.container.annotation.Component;
import dev.ckateptb.minecraft.jyraf.container.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.spongepowered.configurate.objectmapping.meta.Comment;

import java.io.File;

@Getter
@Setter
@Component
public class ConfigExample extends HoconConfig {
    @Comment("This configuration file serves as an example for developers on how to use the API. If you are a server administrator, ignore this.")
    private String example = "ignore me!";
    private Boolean debug = false;

    @PostConstruct
    @SneakyThrows
    public void init() {
        this.load();
        this.save();
    }

    @Override
    public File getFile() {
        return Jyraf.getPlugin().getDataFolder().toPath().resolve("example.conf").toFile();
    }
}
