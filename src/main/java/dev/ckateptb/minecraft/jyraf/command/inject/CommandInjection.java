package dev.ckateptb.minecraft.jyraf.command.inject;

import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.arguments.parser.ParserRegistry;
import cloud.commandframework.bukkit.BukkitCommandManager;
import cloud.commandframework.bukkit.CloudBukkitCapabilities;
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.execution.FilteringCommandSuggestionProcessor;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.meta.SimpleCommandMeta;
import cloud.commandframework.minecraft.extras.MinecraftExceptionHandler;
import cloud.commandframework.paper.PaperCommandManager;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.ckateptb.minecraft.jyraf.command.Command;
import dev.ckateptb.minecraft.jyraf.container.handler.ComponentRegisterHandler;
import dev.ckateptb.minecraft.jyraf.environment.Environment;
import lombok.SneakyThrows;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.function.Function;

public class CommandInjection implements ComponentRegisterHandler {
    private final static Cache<Plugin, AnnotationParser<CommandSender>> COMMAND_CACHE = Caffeine.newBuilder().build();
    private final Function<CommandSender, CommandSender> mapperFunction = Function.identity();

    @SneakyThrows
    @Override
    public void handle(Object component, String qualifier, Plugin owner) {
        if (!(component instanceof Command command)) return;
        AnnotationParser<CommandSender> parser = COMMAND_CACHE.get(owner, plugin -> {
            BukkitCommandManager<CommandSender> manager = Environment.isPaper() ?
                    this.paperCommandManager(plugin) :
                    this.bukkitCommandManager(plugin);
            manager.commandSuggestionProcessor(new FilteringCommandSuggestionProcessor<>(
                    FilteringCommandSuggestionProcessor.Filter.<CommandSender>contains(true).andTrimBeforeLastSpace()
            ));
            if (manager instanceof PaperCommandManager<CommandSender> paperManager) {
                if (paperManager.hasCapability(CloudBukkitCapabilities.BRIGADIER)) {
                    paperManager.registerBrigadier();
                }
                if (paperManager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
                    paperManager.registerAsynchronousCompletions();
                }
            }
            new MinecraftExceptionHandler<CommandSender>()
                    .withDefaultHandlers()
                    .withDecorator(value -> Component.text()
                            .append(Component.text("[", NamedTextColor.DARK_GRAY))
                            .append(Component.text(owner.getName(), NamedTextColor.GOLD))
                            .append(Component.text("] ", NamedTextColor.DARK_GRAY))
                            .append(value).build())
                    .apply(manager, commandSender -> commandSender);

            return new AnnotationParser<>(
                    manager,
                    CommandSender.class,
                    sender -> SimpleCommandMeta.builder()
                            .with(CommandMeta.DESCRIPTION, "No description")
                            .build()
            );
        });
        ParserRegistry<CommandSender> registry = parser.manager().parserRegistry();
        command.getParsers().forEach(registry::registerParserSupplier);
        parser.parse(command);
        parser.parseContainers();
    }

    @SneakyThrows
    private BukkitCommandManager<CommandSender> bukkitCommandManager(Plugin plugin) {
        return new BukkitCommandManager<>(plugin, CommandExecutionCoordinator.SimpleCoordinator.simpleCoordinator(), this.mapperFunction, this.mapperFunction);
    }

    @SneakyThrows
    private PaperCommandManager<CommandSender> paperCommandManager(Plugin plugin) {
        return new PaperCommandManager<>(plugin, AsynchronousCommandExecutionCoordinator.<CommandSender>builder().build(), this.mapperFunction, this.mapperFunction);
    }
}
