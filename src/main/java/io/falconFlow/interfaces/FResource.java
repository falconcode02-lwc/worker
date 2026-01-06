package io.falconFlow.interfaces;


import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FResource {
    String descr();
    String name();
}
