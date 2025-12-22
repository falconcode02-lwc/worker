package io.falconFlow.core;

import io.falconFlow.core.models.ClassDefination;
import java.util.Map;
import org.springframework.context.support.GenericApplicationContext;

public class DynamicBeanRegistrar {

  //  public static void registerDynamicBeans(GenericApplicationContext context) throws Exception {
  //    // loadResource("LoanApplicationModel.java", context);
  //    loadAllResources(context);
  //    // loadResource("classes/FuncCallBRE2_old.java", context);
  //  }
  //
  //  public static void loadAllResources(GenericApplicationContext context) throws Exception {
  //    PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
  //    Resource[] resources = resolver.getResources("classpath*:classes/*");
  //    Map<String, ClassDefination> sources = new HashMap<>();
  //    for (Resource resource : resources) {
  //      System.out.println("Found: " + resource.getFilename());
  //      // Example: read resource
  //      sources.put(resource.getFilename(), resource.getContentAsString(StandardCharsets.UTF_8));
  //
  //      //      try (InputStream in = resource.getInputStream()) {
  //      //        byte[] data = in.readAllBytes();
  //      //        System.out.println(" -> size: " + data.length);
  //      //      }
  //    }
  //    loadResource(sources, context);
  //  }

  public static void loadAllClasses(
      Map<String, ClassDefination> sources, GenericApplicationContext context) throws Exception {
    loadResource(sources, context);
  }

  private static void loadResource(
      Map<String, ClassDefination> sources, GenericApplicationContext context) throws Exception {
    System.out.println(context);
    DynamicCompiler compiler = new DynamicCompiler(context);
    // String sourceCode = resource.getContentAsString(StandardCharsets.UTF_8);

    try {
      // compiler.compileAndRegister(sourceCode);

      compiler.compileAndRegister(sources);
      // Object bean = context.getBean(cls);
      // System.out.println(bean.getClass().getName());
    } catch (Exception ex) {
      System.out.println(ex.toString());
    }
  }
}
