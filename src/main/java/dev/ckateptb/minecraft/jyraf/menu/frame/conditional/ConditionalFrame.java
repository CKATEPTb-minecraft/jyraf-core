package dev.ckateptb.minecraft.jyraf.menu.frame.conditional;

import dev.ckateptb.minecraft.jyraf.menu.Menu;
import dev.ckateptb.minecraft.jyraf.menu.frame.Frame;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Supplier;

@Getter
@Setter
@RequiredArgsConstructor
public class ConditionalFrame implements Frame, Frame.Clickable {
    private final Supplier<Boolean> condition;
    private final Frame success;
    private final Frame failed;
    private Menu menu;

    @Override
    public ItemStack render(Menu menu, int slot) {
        this.menu = menu;
        return condition.get() ? success.render(menu, slot) : failed.render(menu, slot);
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        Frame frame = condition.get() ? success : failed;
        if (frame instanceof Frame.Clickable clickable) {
            clickable.onClick(event);
        }
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
