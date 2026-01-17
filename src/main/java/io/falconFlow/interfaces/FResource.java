package io.falconFlow.interfaces;


import java.lang.annotation.*;
import java.util.Optional;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FResource {
    String description();
    String name();
    boolean selected() default false;

}
