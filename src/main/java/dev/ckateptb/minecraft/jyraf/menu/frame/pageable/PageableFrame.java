package dev.ckateptb.minecraft.jyraf.menu.frame.pageable;

import dev.ckateptb.minecraft.jyraf.menu.Menu;
import dev.ckateptb.minecraft.jyraf.menu.frame.Frame;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.Validate;
import org.apache.commons.math3.util.FastMath;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

@Getter
@Setter
public class PageableFrame implements Frame, Frame.Clickable {
    private int offset;
    private List<Frame> frames = new ArrayList<>();
    private Menu menu;

    @Override
    public ItemStack render(Menu menu, int slot) {
        this.menu = menu;
        Frame frame = this.getFrameAt(slot);
        return frame == null ? null : frame.render(menu, slot);
    }

    public int[] getAllowedSlots() {
        Validate.isTrue(this.menu != null);
        Frame[] frames = this.menu.getFrames();
        return IntStream.range(0, frames.length)
                .filter(slot -> frames[slot] == this)
                .toArray();
    }

    public void setOffset(int offset) {
        this.setOffset(offset, true);
    }

    public void setOffset(int offset, boolean invalidate) {
        int size = frames.size();
        Validate.isTrue(offset >= 0 && (size == 0 || offset < size));
        this.offset = offset;
        if (invalidate && this.menu != null) this.menu.invalidate();
    }

    public void addOffset(int offset) {
        this.addOffset(offset, true);
    }

    public void addOffset(int offset, boolean invalidate) {
        this.setOffset((int) FastMath.min(
                        FastMath.max(this.offset + offset, 0),
                        (offset * FastMath.ceil((double) frames.size() / offset)) - getAllowedSlots().length
                ), invalidate
        );
    }

    public int getSlots() {
        return this.getAllowedSlots().length;
    }

    public boolean hasNext() {
        int rendered = this.getAllowedSlots().length + this.offset;
        return rendered < this.frames.size();
    }

    public boolean hasPrevious() {
        return this.offset > 0;
    }

    public Frame getFrameAt(int slot) {
        int[] allowedSlots = this.getAllowedSlots();
        Frame[] frames = this.frames.stream().skip(this.offset).limit(allowedSlots.length).toArray(Frame[]::new);
        int index = Arrays.stream(allowedSlots).mapToObj(String::valueOf).toList().indexOf(String.valueOf(slot));
        if (index >= frames.length) return null;
        return frames[index];
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        int slot = event.getSlot();
        Frame frame = this.getFrameAt(slot);
        if (frame instanceof Frame.Clickable clickable) {
            clickable.onClick(event);
        }
    }

    public static class Builder implements dev.ckateptb.minecraft.jyraf.builder.Builder<PageableFrame> {
        private final List<Frame> frames = new ArrayList<>();
        public int offset;

        public Builder() {
        }

        public Builder offset(int offset) {
            this.offset = offset;
            return this;
        }

        public Builder addFrames(Frame... frames) {
            this.frames.addAll(Arrays.asList(frames));
            return this;
        }

        public Builder removeFrames(Frame... frames) {
            if (frames.length == 0) this.frames.clear();
            else this.frames.removeAll(Arrays.asList(frames));
            return this;
        }

        public PageableFrame build() {
            PageableFrame frame = new PageableFrame();
            frame.setOffset(this.offset);
            frame.setFrames(this.frames);
            return frame;
        }
    }
}
