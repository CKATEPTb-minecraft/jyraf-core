package dev.ckateptb.minecraft.jyraf.command;

import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.arguments.parser.ParserParameters;
import io.leangen.geantyref.TypeToken;
import lombok.NonNull;
import org.bukkit.command.CommandSender;

import java.util.Map;
import java.util.function.Function;

public interface Command {

    default Map<TypeToken<?>, Function<@NonNull ParserParameters, @NonNull ArgumentParser<CommandSender, ?>>> getParsers() {
        return Map.of();
    }
}
