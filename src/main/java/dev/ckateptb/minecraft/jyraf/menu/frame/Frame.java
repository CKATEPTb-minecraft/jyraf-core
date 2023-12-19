package dev.ckateptb.minecraft.jyraf.menu.frame;

import dev.ckateptb.minecraft.jyraf.builder.item.ItemBuilder;
import dev.ckateptb.minecraft.jyraf.menu.Menu;
import dev.ckateptb.minecraft.jyraf.menu.frame.button.ButtonFrame;
import dev.ckateptb.minecraft.jyraf.menu.frame.conditional.ConditionalFrame;
import dev.ckateptb.minecraft.jyraf.menu.frame.item.ItemFrame;
import dev.ckateptb.minecraft.jyraf.menu.frame.pageable.PageableFrame;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface Frame {
    ItemStack render(Menu menu, int slot);

    Menu getMenu();

    void setMenu(Menu menu);

    interface Clickable {
        void onClick(InventoryClickEvent event);
    }

    interface Builder {
        default ItemFrame item(Material material, Consumer<ItemBuilder> consumer) {
            return this.item(material, consumer, (BiConsumer<Menu, Integer>) null);
        }

        default ItemFrame item(Material material, Consumer<ItemBuilder> consumer, BiConsumer<Menu, Integer> invalidate) {
            ItemBuilder builder = new ItemBuilder(material);
            consumer.accept(builder);
            return this.item(builder.build(), invalidate);
        }

        default ItemFrame item(ItemStack stack, Consumer<ItemBuilder> consumer) {
            return this.item(stack, consumer, (BiConsumer<Menu, Integer>) null);
        }

        default ItemFrame item(ItemStack stack, Consumer<ItemBuilder> consumer, BiConsumer<Menu, Integer> invalidate) {
            ItemBuilder builder = new ItemBuilder(stack);
            consumer.accept(builder);
            return this.item(builder.build(), invalidate);
        }

        default ItemFrame item(ItemStack stack) {
            return this.item(stack, (BiConsumer<Menu, Integer>) null);
        }

        default ItemFrame item(ItemStack stack, BiConsumer<Menu, Integer> invalidate) {
            ItemFrame.Builder builder = new ItemFrame.Builder();
            builder.item(stack);
            builder.invalidate(invalidate);
            return builder.build();
        }

        default ButtonFrame item(Material material, Consumer<ItemBuilder> consumer, Menu.ClickHandler handler, BiConsumer<Menu, Integer> invalidate) {
            ItemBuilder builder = new ItemBuilder(material);
            consumer.accept(builder);
            return this.item(builder.build(), handler, invalidate);
        }

        default ButtonFrame item(Material material, Consumer<ItemBuilder> consumer, Menu.ClickHandler handler) {
            return this.item(material, consumer, handler, null);
        }

        default ButtonFrame item(ItemStack stack, Consumer<ItemBuilder> consumer, Menu.ClickHandler handler, BiConsumer<Menu, Integer> invalidate) {
            ItemBuilder builder = new ItemBuilder(stack);
            consumer.accept(builder);
            return this.item(builder.build(), handler, invalidate);
        }

        default ButtonFrame item(ItemStack stack, Consumer<ItemBuilder> consumer, Menu.ClickHandler handler) {
            return this.item(stack, consumer, handler, null);
        }

        default ButtonFrame item(ItemStack stack, Menu.ClickHandler handler) {
            return this.item(stack, handler, null);
        }

        default ButtonFrame item(ItemStack stack, Menu.ClickHandler handler, BiConsumer<Menu, Integer> invalidate) {
            ButtonFrame.Builder builder = new ButtonFrame.Builder();
            builder.item(stack);
            builder.addClickHandler(handler);
            builder.invalidate(invalidate);
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

        default ConditionalFrame conditional(@Nullable Frame first, Frame second) {
            return this.conditional(null, first, second);
        }
    }

    interface Invalidable {
        void invalidate(Menu menu, int slot);

        Invalidable invalidate(BiConsumer<Menu, Integer> invalidate);
    }
}
