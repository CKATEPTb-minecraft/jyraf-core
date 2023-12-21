package dev.ckateptb.minecraft.jyraf.rpc;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

public class RPC {
    private static final Cache<String, Object> VARIABLES = Caffeine.newBuilder().build();
    private static final Cache<String, Object> CACHE = Caffeine.newBuilder().build();

    public static Object getVariable(String key) {
        if (key == null) return null;
        if (key.startsWith("\"") && key.endsWith("\"")) {
            return key.substring(1, key.length() - 1);
        } else if (key.startsWith("'") && key.endsWith("'")) {
            return key.substring(1, key.length() - 1);
        }
        return VARIABLES.getIfPresent(key);
    }

    public static void setVariable(String key, Object variable) {
        VARIABLES.put(key, variable);
    }

    /*
        Describe the function call as a string in the following format.
        className.methodName(param or {} for null: paramClassName, ...): returnClassName
        If you want an instance to be used for the call use %varname at start
        If you want the result to be written to a variable use $varname= before className
        If you want the result to be cached use ^ before className and after varname (if present)
        If you want to pass a class as an argument use class-className: java.lang.Class
        example:
            $plugin=org.bukkit.Bukkit.getPlugin("Jyraf-Core": java.lang.String): dev.ckateptb.minecraft.jyraf.Jyraf
            %plugin $scheduler=^dev.ckateptb.minecraft.jyraf.Jyraf.syncScheduler(): dev.ckateptb.minecraft.jyraf.schedule.SyncScheduler
     */
    public static Object process(String... args) {
        if (args == null || args.length == 0) return null;
        Object finalResult = null;
        for (String rpc : args) {
            rpc = rpc.trim();
            String instance = null;
            if (rpc.startsWith("%")) {
                int index = rpc.indexOf(" ");
                instance = rpc.substring(1, index);
                rpc = rpc.replace("%" + instance + " ", "").trim();
                instance = instance.trim();
            }
            String variable = null;
            if (rpc.startsWith("$")) {
                int index = rpc.indexOf("=");
                variable = rpc.substring(1, index);
                rpc = rpc.replace("$" + variable + "=", "").trim();
                variable = variable.trim();
            }
            Object result;
            if (rpc.startsWith("^")) {
                rpc = rpc.substring(1);
                String finalInstance = instance;
                String finalRpc = rpc;
                result = CACHE.get(instance + rpc, string -> RPC.call(finalInstance, finalRpc));
            } else {
                result = call(instance, rpc);
            }
            if (variable != null) setVariable(variable, result);
            finalResult = result;
        }
        return finalResult;
    }

    private static Object call(String instance, String rpc) {
        ReflectionProcedureCall deserialize = ReflectionProcedureCall.deserialize(rpc);
        return deserialize.call(getVariable(instance));
    }
}
