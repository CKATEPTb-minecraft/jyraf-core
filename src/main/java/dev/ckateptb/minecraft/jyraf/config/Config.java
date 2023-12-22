package dev.ckateptb.minecraft.jyraf.config;

import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.io.File;

@ConfigSerializable
public interface Config {
    void initialize();

    void load();

    void save() throws ConfigurateException;

    File getFile();

    interface Autoloader {

    }

}
