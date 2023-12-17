package dev.ckateptb.minecraft.jyraf.menu;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

@Getter
@Setter
public abstract class AbstractMenu implements Menu {
    protected boolean closable = true;
    protected boolean editable = true;
    protected transient CloseHandler closeHandler;

    @Override
    public int getSize() {
        return this.getInventory().getSize();
    }

    @Override
    public void open(Player target) {
        target.openInventory(this.getInventory());
    }

    @Override
    public synchronized void close() {
        this.setClosable(true);
        this.getInventory().close();
    }
}
