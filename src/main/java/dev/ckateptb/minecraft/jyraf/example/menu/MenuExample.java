package dev.ckateptb.minecraft.jyraf.example.menu;

import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import dev.ckateptb.minecraft.jyraf.Jyraf;
import dev.ckateptb.minecraft.jyraf.command.Command;
import dev.ckateptb.minecraft.jyraf.component.Text;
import dev.ckateptb.minecraft.jyraf.container.annotation.Component;
import dev.ckateptb.minecraft.jyraf.menu.Menu;
import dev.ckateptb.minecraft.jyraf.menu.frame.button.ButtonFrame;
import dev.ckateptb.minecraft.jyraf.menu.frame.item.ItemFrame;
import dev.ckateptb.minecraft.jyraf.menu.frame.pageable.PageableFrame;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import reactor.core.publisher.Mono;

import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
public class MenuExample implements Command {
    private final Jyraf plugin;

    @CommandMethod("jyraf debug menu")
    @CommandPermission("jyraf.command.debug.menu")
    public void help(Player sender) {
        Mono.just(
                        Menu.builder().chest("&6&lExample", 5)
                                .editable(false)
                                .closable(true)
                                .addCloseHandler(event -> sender.sendMessage("closed"))
                                .updateContext(ctx -> { // Draw borders
                                    ItemFrame item = ctx.item(Material.RED_STAINED_GLASS_PANE, builder -> builder
                                            .name("")
                                            .flag(ItemFlag.values()));
                                    ctx.row(1, item);
                                    ctx.set(item, 9, 17, 18, 26, 27, 35);
                                    ctx.row(5, item);
                                })
                                .updateContext(ctx -> { // Draw action button
                                    ButtonFrame item = ctx.item(Material.ENDER_CHEST, builder -> builder
                                                    .name("&6&lAction")
                                                    .lore("&f&lAction: &8Say hello"),
                                            event -> {
                                                sender.sendMessage(Text.of("&9Hello&6World"));
                                                Inventory inventory = event.getClickedInventory();
                                                if (inventory != null) inventory.close();
                                            });
                                    ctx.set(40, item);
                                })
                                .updateContext(ctx -> { // Draw pagination
                                    int[] slots = ctx.concat(ctx.range(10, 16), ctx.range(19, 25), ctx.range(28, 34));
                                    PageableFrame pagination = ctx.pagination(slots, builder ->
                                            IntStream.rangeClosed(1, (slots.length * 3) + 4).forEach(value ->
                                                    builder.addFrames(ctx.item(Material.values()[value], itemBuilder ->
                                                                    itemBuilder.name("Item #" + value), event ->
                                                                    sender.sendMessage("Clicked: " + value)
                                                            )
                                                    )
                                            )
                                    );
                                    ctx.set(slots, pagination);
                                    ItemFrame failed = ctx.item(Material.RED_STAINED_GLASS_PANE, builder -> builder
                                            .name("")
                                            .flag(ItemFlag.values()));
                                    ctx.set(38, ctx.conditional(pagination::hasPrevious, ctx.item(Material.ARROW, itemBuilder -> itemBuilder.name("previous"), event -> {
                                        pagination.addOffset(-slots.length);
                                    }), failed));
                                    ctx.set(42, ctx.conditional(pagination::hasNext, ctx.item(Material.ARROW, itemBuilder -> itemBuilder.name("next"), event -> {
                                        pagination.addOffset(slots.length);
                                    }), failed));
                                })
                                .build()
                )
                .subscribeOn(this.plugin.syncScheduler())
                .subscribe(menu -> menu.open(sender));
    }
}
