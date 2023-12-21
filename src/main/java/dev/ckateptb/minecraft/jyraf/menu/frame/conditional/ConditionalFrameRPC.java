package dev.ckateptb.minecraft.jyraf.menu.frame.conditional;

import dev.ckateptb.minecraft.jyraf.menu.frame.Frame;

public class ConditionalFrameRPC extends ConditionalFrame implements Frame.RPC {
    private final String[] rpc;

    public ConditionalFrameRPC(Frame success, Frame failed, String... rpc) {
        super(() -> (Boolean) dev.ckateptb.minecraft.jyraf.rpc.RPC.process(rpc), success, failed);
        this.rpc = rpc;
    }

    @Override
    public String[] getRPC() {
        return this.rpc;
    }
}
