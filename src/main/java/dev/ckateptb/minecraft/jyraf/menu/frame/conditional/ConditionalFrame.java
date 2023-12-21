package dev.ckateptb.minecraft.jyraf.menu.frame.conditional;

import dev.ckateptb.minecraft.jyraf.menu.Menu;
import dev.ckateptb.minecraft.jyraf.menu.frame.Frame;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

@Getter
@Setter
@RequiredArgsConstructor
public class ConditionalFrame implements Frame, Frame.Clickable, Frame.Invalidable {
    private final Supplier<Boolean> condition;
    private final Frame success;
    private final Frame failed;
    private Menu menu;

    @Override
    public ItemStack render(Menu menu, int slot) {
        this.menu = menu;
        if (condition == null) {
            ItemStack first = success.render(menu, slot);
            return first == null || first.getType().isEmpty() ? failed.render(menu, slot) : first;
        }
        return condition.get() ? success.render(menu, slot) : failed.render(menu, slot);
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        Frame frame;
        if (condition == null) {
            int slot = event.getSlot();
            ItemStack first = success.render(menu, slot);
            frame = first == null || first.getType().isEmpty() ? failed : success;
        } else {
            frame = condition.get() ? success : failed;
        }
        if (frame instanceof Frame.Clickable clickable) {
            clickable.onClick(event);
        }
    }

    @Override
    public void invalidate(Menu menu, int slot) {
        if (success instanceof Frame.Invalidable invalidable) {
            invalidable.invalidate(menu, slot);
        }
        if (failed instanceof Frame.Invalidable invalidable) {
            invalidable.invalidate(menu, slot);
        }
    }

    @Override
    public Invalidable invalidate(BiConsumer<Menu, Integer> invalidate) {
        return this;
    }

    @RequiredArgsConstructor
    public static class Builder implements dev.ckateptb.minecraft.jyraf.builder.Builder<ConditionalFrame> {
        private final Supplier<Boolean> condition;
        private final Frame success;
        private final Frame failed;

        @Override
        public ConditionalFrame build() {
            return new ConditionalFrame(condition, success, failed);
        }
    }
}
