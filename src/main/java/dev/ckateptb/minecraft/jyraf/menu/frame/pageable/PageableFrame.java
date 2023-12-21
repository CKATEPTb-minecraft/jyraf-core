package dev.ckateptb.minecraft.jyraf.menu.frame.pageable;

import dev.ckateptb.minecraft.jyraf.menu.Menu;
import dev.ckateptb.minecraft.jyraf.menu.frame.Frame;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.Validate;
import org.apache.commons.math3.util.FastMath;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

@Getter
@Setter
public class PageableFrame implements Frame, Frame.Clickable {
    private Set<Integer> slots = new HashSet<>();
    private int offset;
    private List<Frame> frames = new ArrayList<>();
    private Menu menu;

    @Override
    public ItemStack render(Menu menu, int slot) {
        this.menu = menu;
        this.slots.add(slot);
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
                        (offset * FastMath.ceil((double) frames.size() / offset)) - slots.size()
                ), invalidate
        );
    }

    public int getSlots() {
        return this.slots.size();
    }

    public int[] getAllowedSlots() {
        return this.slots.stream().mapToInt(value -> value).toArray();
    }

    public boolean hasNext() {
        int rendered = this.slots.size() + this.offset;
        return rendered < this.frames.size();
    }

    public boolean hasPrevious() {
        return this.offset > 0;
    }

    public Frame getFrameAt(int slot) {
        Frame[] frames = this.frames.stream().skip(this.offset).limit(this.slots.size()).toArray(Frame[]::new);
        int index = this.slots.stream().map(String::valueOf).toList().indexOf(String.valueOf(slot));
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
