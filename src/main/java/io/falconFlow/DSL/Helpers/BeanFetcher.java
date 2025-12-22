package io.falconFlow.DSL.Helpers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class BeanFetcher {

  @Autowired private ApplicationContext applicationContext;

  public Object getFunctionByName(String beanName) {
      System.out.println("Searching for Bean >>> " + beanName);

    if (applicationContext.containsBean(beanName)) {
      return applicationContext.getBean(beanName);
    }
    throw new IllegalArgumentException("No bean found with name: " + beanName);
  }

  public <T> T getFunctionByName(String beanName, Class<T> type) {
    return applicationContext.getBean(beanName, type);
  }
}
