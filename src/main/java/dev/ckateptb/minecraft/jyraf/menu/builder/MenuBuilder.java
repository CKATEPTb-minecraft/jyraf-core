package dev.ckateptb.minecraft.jyraf.menu.builder;

import dev.ckateptb.minecraft.jyraf.builder.item.ItemBuilder;
import dev.ckateptb.minecraft.jyraf.component.Text;
import dev.ckateptb.minecraft.jyraf.menu.Menu;
import dev.ckateptb.minecraft.jyraf.menu.chest.ChestMenu;
import dev.ckateptb.minecraft.jyraf.menu.frame.Frame;
import net.kyori.adventure.text.Component;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class MenuBuilder {
    @Deprecated
    public ChestBuilder chest(String title, int rows) {
        return this.chest(Text.of(title), rows);
    }

    public ChestBuilder chest(Component title, int rows) {
        return new ChestBuilder(title, rows);
    }

    public AnvilGUI.Builder anvil() {
        return new AnvilGUI.Builder();
    }

    public ItemBuilder item(Material material) {
        return new ItemBuilder(material);
    }

    public ItemBuilder item(ItemStack itemStack) {
        return new ItemBuilder(itemStack);
    }

    public Frame.Builder frame() {
        return (Context) (slot, frame) -> {
        };
    }

    public interface Context extends Frame.Builder {
        void set(int slot, Frame frame);

        default void set(int[] slots, Frame frame) {
            for (int slot : slots) {
                this.set(slot, frame);
            }
        }

        default void set(Frame frame, int... slots) {
            this.set(slots, frame);
        }

        default int[] range(int from, int to) {
            return IntStream.rangeClosed(from, to).toArray();
        }

        default int[] concat(int[]... values) {
            return Stream.of(values).flatMapToInt(Arrays::stream).toArray();
        }

        default void range(int from, int to, Frame frame) {
            this.set(this.range(from, to), frame);
        }

        default void row(int row, Frame frame) {
            int offset = row * 9;
            this.range(offset - 9, offset - 1, frame);
        }
    }

    public static class ChestBuilder {
        private final int rows;
        private final List<Menu.CloseHandler> closeHandlers = new ArrayList<>();
        private final Frame[] frames;
        public Component title;
        private boolean editable = false;
        private boolean closable = true;

        @Deprecated
        public ChestBuilder(String title, int rows) {
            this(Text.of(title), rows);
        }

        public ChestBuilder(Component title, int rows) {
            this.title = title;
            this.rows = rows;
            this.frames = new Frame[rows * 9];
        }

        public ChestBuilder editable(boolean editable) {
            this.editable = editable;
            return this;
        }

        public ChestBuilder closable(boolean closable) {
            this.closable = closable;
            return this;
        }

        public ChestBuilder addCloseHandler(Menu.CloseHandler handler) {
            this.closeHandlers.add(handler);
            return this;
        }

        public ChestBuilder removeCloseHandler(Menu.CloseHandler handler) {
            closeHandlers.remove(handler);
            return this;
        }

        public ChestBuilder updateContext(Consumer<Context> supplier) {
            supplier.accept((slot, frame) -> this.frames[slot] = frame);
            return this;
        }

        public ChestMenu build() {
            ChestMenu chestMenu = new ChestMenu(this.title, this.rows);
            chestMenu.setEditable(this.editable);
            chestMenu.setClosable(this.closable);
            for (int slot = 0; slot < frames.length; slot++) {
                chestMenu.setFrame(slot, frames[slot]);
            }
            chestMenu.setCloseHandler(event -> this.closeHandlers.forEach(handler -> handler.handle(event)));
            return chestMenu;
        }
    }
}
