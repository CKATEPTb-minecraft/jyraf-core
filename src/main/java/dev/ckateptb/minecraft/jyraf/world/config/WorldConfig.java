package dev.ckateptb.minecraft.jyraf.world.config;

import dev.ckateptb.minecraft.jyraf.Jyraf;
import dev.ckateptb.minecraft.jyraf.config.Config;
import dev.ckateptb.minecraft.jyraf.config.hocon.HoconConfig;
import dev.ckateptb.minecraft.jyraf.container.annotation.Component;
import lombok.Getter;
import org.spongepowered.configurate.objectmapping.meta.Comment;

import java.io.File;

@Getter
@Component
public class WorldConfig extends HoconConfig implements Config.Autoloader {
    @Comment("""
            This option optimizes work with entities for external threads.
            Why is this included in the configuration file?
            This option passively performs calculations regarding the entity's movement for further interactions.
            Such calculations are ongoing and add unnecessary load, which is justified only if it is not leveled in
            other places. For example, if you or your plugins do not use the Colliders or PacketEntity
            provided in jyraf-core, then you should disable this option. Otherwise this option will go a long way
            in helping you save your tps.""")
    private Boolean asyncEntityLookup = true;

    @Override
    public File getFile() {
        return Jyraf.getPlugin().getDataFolder().toPath().resolve("world.conf").toFile();
    }
}
