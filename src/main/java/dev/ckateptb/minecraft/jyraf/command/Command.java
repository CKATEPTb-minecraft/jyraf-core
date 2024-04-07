package dev.ckateptb.minecraft.jyraf.command;

import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.arguments.parser.ParserParameters;
import io.leangen.geantyref.TypeToken;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Function;

public interface Command {

    default Map<TypeToken<?>, Function<@NotNull ParserParameters, @NotNull ArgumentParser<CommandSender, ?>>> getParsers() {
        return Map.of();
    }
}
