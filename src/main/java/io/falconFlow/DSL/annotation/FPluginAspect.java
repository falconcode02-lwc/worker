package io.falconFlow.DSL.annotation;

import io.falconFlow.DSL.Helpers.Global;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

@Component
public class FPluginAspect implements BeanPostProcessor {

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
    Class<?> clazz = bean.getClass();
    if (clazz.isAnnotationPresent(FPlugin.class)) {
      Global.addService(beanName);
      System.out.println("✅ Registered Plugin Bean: " + beanName);
    }
    return bean;
  }
}
