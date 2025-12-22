package io.falconFlow.configuration;

import io.falconFlow.core.DynamicBeanRegistrar;
import io.falconFlow.core.models.ClassDefination;
import io.falconFlow.dao.DB;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Priority;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Priority(10)
@Configuration
public class RegisterClasses {

  @Autowired DB db;

  @Autowired ApplicationContext ctx;

  @PostConstruct
  void init() {

    System.out.println("Inside RegisterClasses >>>> !");
    Map<String, ClassDefination> sources = new HashMap<>();
    List<Map<String, Object>> ccff =
        this.db.selectAsMap(
            "select id , class_name, raw_process_class,class_type from ff_functions where class_type in('function','model', 'condition','object', 'controller','plugin') and isdeleted=false");
    for (Map<String, Object> g : ccff) {
      String code = g.get("raw_process_class").toString();
      String className = g.get("class_name").toString();
      String classType = g.get("class_type").toString();
      ClassDefination d = new ClassDefination();
      d.setClassName(className);
      d.setClassType(classType);
      d.setSourceCode(code);
      System.out.println("RegisterClasses | found --> " + "| " + classType + " |" + className);
      // System.err.println(code);
      sources.put(className, d);
    }
    try {
      DynamicBeanRegistrar.loadAllClasses(sources, (GenericApplicationContext) ctx);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
