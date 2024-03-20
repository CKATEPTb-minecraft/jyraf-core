package dev.ckateptb.minecraft.jyraf.schedule;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Schedule {
    int initialDelay();

    int fixedRate();

    boolean async() default false;
}
