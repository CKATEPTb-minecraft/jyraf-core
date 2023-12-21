package dev.ckateptb.minecraft.jyraf.menu.frame.button;

import dev.ckateptb.minecraft.jyraf.menu.frame.Frame;
import org.bukkit.event.inventory.InventoryClickEvent;

public class ButtonFrameRPC extends ButtonFrame implements Frame.RPC {
    private final String[] rpc;

    public ButtonFrameRPC(String... rpc) {
        this.rpc = rpc;
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        dev.ckateptb.minecraft.jyraf.rpc.RPC.process(rpc);
    }

    @Override
    public String[] getRPC() {
        return rpc;
    }
}
