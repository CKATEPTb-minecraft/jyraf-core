package dev.ckateptb.minecraft.jyraf.menu.frame.button;

import dev.ckateptb.minecraft.jyraf.menu.Menu;
import dev.ckateptb.minecraft.jyraf.menu.frame.Frame;
import dev.ckateptb.minecraft.jyraf.menu.frame.item.ItemFrame;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ButtonFrame extends ItemFrame implements Frame.Clickable {
    private Menu.ClickHandler handler;

    @Override
    public void onClick(InventoryClickEvent event) {
        if (this.handler != null) this.handler.handle(event);
    }

    public static class Builder extends ItemFrame.Builder {
        private final List<Menu.ClickHandler> handlers = new ArrayList<>();

        @Override
        public Builder item(ItemStack itemStack) {
            return (Builder) super.item(itemStack);
        }

        public Builder addClickHandler(Menu.ClickHandler handler) {
            this.handlers.add(handler);
            return this;
        }

        public Builder removeClickHandler(Menu.ClickHandler handler) {
            this.handlers.remove(handler);
            return this;
        }

        @Override
        public ButtonFrame build() {
            ButtonFrame frame = new ButtonFrame();
            frame.setItem(this.itemStack);
            frame.setHandler(event -> this.handlers.forEach(handler -> handler.handle(event)));
            return frame;
        }
    }
}
