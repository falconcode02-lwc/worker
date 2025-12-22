package io.falconFlow.DSL.annotation;

import org.springframework.stereotype.Service;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Service
public @interface FCondition {
  String value() default "";
}
