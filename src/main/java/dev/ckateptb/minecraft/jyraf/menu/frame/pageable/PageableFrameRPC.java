package dev.ckateptb.minecraft.jyraf.menu.frame.pageable;

import dev.ckateptb.minecraft.jyraf.menu.frame.Frame;

import java.util.List;

public class PageableFrameRPC extends PageableFrame implements Frame.RPC {
    private final String[] rpc;

    @SuppressWarnings("unchecked")
    public PageableFrameRPC(String... rpc) {
        this.rpc = rpc;
        this.setFrames((List<Frame>) dev.ckateptb.minecraft.jyraf.rpc.RPC.process(rpc));
    }

    @Override
    public String[] getRPC() {
        return new String[0];
    }
}
