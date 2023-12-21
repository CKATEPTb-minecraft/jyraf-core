package dev.ckateptb.minecraft.jyraf.rpc;

import lombok.SneakyThrows;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public interface ReflectionProcedureCall {
    @SneakyThrows
    static ReflectionProcedureCall deserialize(String string) {
        Pattern pattern = Pattern.compile("(.+)\\.(.+)\\(|(?:(.+?):.(.+?))(?:,.|\\))|[)]*:.(.+)", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(string.trim());
        List<String> result = new ArrayList<>();
        while (matcher.find()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                result.add(matcher.group(i));
            }
        }
        String[] args = result.stream().filter(Objects::nonNull).toArray(String[]::new);
        if (args.length < 3) throw new RuntimeException();
        Class<?> clazz = Class.forName(args[0]);
        List<String> params = new ArrayList<>();
        for (int i = 2; i < args.length - 1; i += 2) {
            params.add(args[i + 1]);
        }
        List<String> vars = new ArrayList<>();
        for (int i = 2; i < args.length - 1; i += 2) {
            vars.add(args[i]);
        }
        Method method = clazz.getMethod(args[1], params.stream().map(value -> {
            try {
                return Class.forName(value);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }).toArray(Class[]::new));
        method.setAccessible(true);
        String[] paramVars = vars.toArray(String[]::new);
        if (!method.getReturnType().getName().equals(args[args.length - 1])) throw new RuntimeException();
        return new ReflectionProcedureCall() {
            @Override
            public Method getMethod() {
                return method;
            }

            @Override
            public String[] getParams() {
                return paramVars;
            }
        };
    }

    Method getMethod();

    String[] getParams();

    @SneakyThrows
    default Object call(Object instance) {
        return this.getMethod().invoke(instance, Arrays.stream(this.getParams()).map(string -> {
            string = string.trim();
            if (string.equalsIgnoreCase("{}")) return null;
            if (string.startsWith("class-")) {
                try {
                    return Class.forName(string.substring(6));
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
            return RPC.getVariable(string);
        }).toArray(Object[]::new));
    }

    default String serialize() {
        Method method = this.getMethod();
        Class<?> clazz = method.getDeclaringClass();
        String name = method.getName();
        String params = Arrays.stream(method.getParameterTypes()).map(param -> "{}: " + param).collect(Collectors.joining(", "));
        Class<?> returnType = method.getReturnType();
        return String.format("%s.%s(%s):%s", clazz, name, params, returnType);
    }
}
