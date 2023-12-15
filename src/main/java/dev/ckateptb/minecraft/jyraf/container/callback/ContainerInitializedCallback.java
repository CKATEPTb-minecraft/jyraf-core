package dev.ckateptb.minecraft.jyraf.container.callback;

import dev.ckateptb.minecraft.jyraf.container.api.Container;

public interface ContainerInitializedCallback {
    void handle(Container container, Long count);
}
