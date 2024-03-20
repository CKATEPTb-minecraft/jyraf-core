package dev.ckateptb.minecraft.jyraf.container.handler;

import dev.ckateptb.minecraft.jyraf.container.api.Container;

public interface ContainerInitializeHandler {
    void handle(Container container, Long count);
}
