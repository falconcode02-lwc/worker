package io.falconFlow.DSL.annotation;

import io.falconFlow.DSL.Helpers.Global;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

@Component
public class FControllerAspect implements BeanPostProcessor {

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
    Class<?> clazz = bean.getClass();
    if (clazz.isAnnotationPresent(FController.class)) {
      Global.addService(beanName);
      System.out.println("✅ Registered Controller Bean: " + beanName);
    }
    return bean;
  }
}
