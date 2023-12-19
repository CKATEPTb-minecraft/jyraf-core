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
import java.util.stream.Stream;

@Getter
@Setter
public class PageableFrame implements Frame, Frame.Clickable {
    private final int[] slots;
    private int offset;
    private List<Frame> frames = new ArrayList<>();
    private Menu menu;

    public PageableFrame(int[] slots) {
        this.slots = slots;
    }

    @Override
    public ItemStack render(Menu menu, int slot) {
        this.menu = menu;
        Frame frame = this.getFrameAt(slot);
        return frame == null ? null : frame.render(menu, slot);
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
                        (offset * FastMath.ceil((double) frames.size() / offset)) - slots.length
                ), invalidate
        );
    }

    public int getSlots() {
        return this.slots.length;
    }

    public int[] getAllowedSlots() {
        return this.slots;
    }

    public boolean hasNext() {
        int rendered = this.slots.length + this.offset;
        return rendered < this.frames.size();
    }

    public boolean hasPrevious() {
        return this.offset > 0;
    }

    public Frame getFrameAt(int slot) {
        Frame[] frames = this.frames.stream().skip(this.offset).limit(this.slots.length).toArray(Frame[]::new);
        int index = Stream.of(this.slots).flatMapToInt(Arrays::stream).mapToObj(String::valueOf).toList().indexOf(String.valueOf(slot));
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
        public final int[] slots;
        private final List<Frame> frames = new ArrayList<>();
        public int offset;

        public Builder(int[] slots) {
            this.slots = slots;
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
            PageableFrame frame = new PageableFrame(this.slots);
            frame.setOffset(this.offset);
            frame.setFrames(this.frames);
            return frame;
        }
    }
}
