package dev.ckateptb.minecraft.jyraf.container.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Qualifier {
    String DEFAULT_QUALIFIER = "_DEFAULT";

    String value();
}
