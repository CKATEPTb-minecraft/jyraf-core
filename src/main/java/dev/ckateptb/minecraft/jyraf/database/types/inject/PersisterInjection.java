package dev.ckateptb.minecraft.jyraf.database.types.inject;

import com.j256.ormlite.field.DataPersister;
import com.j256.ormlite.field.DataPersisterManager;
import dev.ckateptb.minecraft.jyraf.container.handler.ComponentRegisterHandler;
import org.bukkit.plugin.Plugin;

public class PersisterInjection implements ComponentRegisterHandler {
    @Override
    public void handle(Object object, String qualifier, Plugin owner) {
        if (!(object instanceof DataPersister persister)) return;
        DataPersisterManager.registerDataPersisters(persister);
    }
}
