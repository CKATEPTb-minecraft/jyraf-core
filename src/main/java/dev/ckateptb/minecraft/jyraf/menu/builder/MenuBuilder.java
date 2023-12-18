package dev.ckateptb.minecraft.jyraf.menu.builder;

import dev.ckateptb.minecraft.jyraf.builder.item.ItemBuilder;
import dev.ckateptb.minecraft.jyraf.menu.Menu;
import dev.ckateptb.minecraft.jyraf.menu.chest.ChestMenu;
import dev.ckateptb.minecraft.jyraf.menu.frame.Frame;
import dev.ckateptb.minecraft.jyraf.menu.frame.button.ButtonFrame;
import dev.ckateptb.minecraft.jyraf.menu.frame.conditional.ConditionalFrame;
import dev.ckateptb.minecraft.jyraf.menu.frame.item.ItemFrame;
import dev.ckateptb.minecraft.jyraf.menu.frame.pageable.PageableFrame;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class MenuBuilder {
    public ChestBuilder chest(String title, int rows) {
        return new ChestBuilder(title, rows);
    }

    public interface Context {
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

        default ItemFrame item(Material material, Consumer<ItemBuilder> consumer) {
            ItemBuilder builder = new ItemBuilder(material);
            consumer.accept(builder);
            return this.item(builder.build());
        }

        default ItemFrame item(ItemStack stack, Consumer<ItemBuilder> consumer) {
            ItemBuilder builder = new ItemBuilder(stack);
            consumer.accept(builder);
            return this.item(builder.build());
        }

        default ItemFrame item(ItemStack stack) {
            ItemFrame.Builder builder = new ItemFrame.Builder();
            builder.item(stack);
            return builder.build();
        }

        default ButtonFrame item(Material material, Consumer<ItemBuilder> consumer, Menu.ClickHandler handler) {
            ItemBuilder builder = new ItemBuilder(material);
            consumer.accept(builder);
            return this.item(builder.build(), handler);
        }

        default ButtonFrame item(ItemStack stack, Consumer<ItemBuilder> consumer, Menu.ClickHandler handler) {
            ItemBuilder builder = new ItemBuilder(stack);
            consumer.accept(builder);
            return this.item(builder.build(), handler);
        }

        default ButtonFrame item(ItemStack stack, Menu.ClickHandler handler) {
            ButtonFrame.Builder builder = new ButtonFrame.Builder();
            builder.item(stack);
            builder.addClickHandler(handler);
            return builder.build();
        }

        default PageableFrame pagination(int[] slots, Consumer<PageableFrame.Builder> consumer) {
            PageableFrame.Builder builder = new PageableFrame.Builder(slots);
            consumer.accept(builder);
            return builder.build();
        }

        default ConditionalFrame conditional(Supplier<Boolean> condition, Frame success, Frame failed) {
            ConditionalFrame.Builder builder = new ConditionalFrame.Builder(condition, success, failed);
            return builder.build();
        }
    }

    public static class ChestBuilder {
        private final int rows;
        private final List<Menu.CloseHandler> closeHandlers = new ArrayList<>();
        private final Frame[] frames;
        public String title;
        private boolean editable = false;
        private boolean closable = true;

        public ChestBuilder(String title, int rows) {
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
