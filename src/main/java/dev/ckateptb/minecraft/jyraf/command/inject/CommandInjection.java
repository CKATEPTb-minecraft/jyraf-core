package dev.ckateptb.minecraft.jyraf.command.inject;

import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.arguments.parser.ParserRegistry;
import cloud.commandframework.bukkit.BukkitCommandManager;
import cloud.commandframework.bukkit.CloudBukkitCapabilities;
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.meta.SimpleCommandMeta;
import cloud.commandframework.minecraft.extras.MinecraftExceptionHandler;
import cloud.commandframework.paper.PaperCommandManager;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.ckateptb.minecraft.jyraf.command.Command;
import dev.ckateptb.minecraft.jyraf.container.callback.ComponentRegisterCallback;
import dev.ckateptb.minecraft.jyraf.environment.Environment;
import lombok.SneakyThrows;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public class CommandInjection implements ComponentRegisterCallback {
    private final static Cache<Plugin, AnnotationParser<CommandSender>> COMMAND_CACHE = Caffeine.newBuilder().build();

    @Override
    public void handle(Object component, String qualifier, Plugin owner) {
        if (!(component instanceof Command command)) return;
        AnnotationParser<CommandSender> parser = COMMAND_CACHE.get(owner, plugin -> {
            BukkitCommandManager<CommandSender> manager = Environment.isPaper() ?
                    this.paperCommandManager(plugin) :
                    this.bukkitCommandManager(plugin);
            if (manager instanceof PaperCommandManager<CommandSender> paperManager) {
                if (paperManager.hasCapability(CloudBukkitCapabilities.BRIGADIER)) {
                    paperManager.registerBrigadier();
                }
                if (paperManager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
                    paperManager.registerAsynchronousCompletions();
                }
            }
            new MinecraftExceptionHandler<CommandSender>()
                    .withInvalidSyntaxHandler()
                    .withInvalidSenderHandler()
                    .withNoPermissionHandler()
                    .withArgumentParsingHandler()
                    .apply(manager, (sender) -> sender);
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
    }

    @SneakyThrows
    private BukkitCommandManager<CommandSender> bukkitCommandManager(Plugin plugin) {
        return BukkitCommandManager.createNative(plugin, CommandExecutionCoordinator.SimpleCoordinator.simpleCoordinator());
    }

    @SneakyThrows
    private PaperCommandManager<CommandSender> paperCommandManager(Plugin plugin) {
        return PaperCommandManager.createNative(plugin,
                AsynchronousCommandExecutionCoordinator.<CommandSender>builder().build());
    }
}
