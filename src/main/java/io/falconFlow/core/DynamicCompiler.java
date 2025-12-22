package io.falconFlow.core;

import com.google.common.base.Splitter;
import io.falconFlow.DSL.annotation.FCondition;
import io.falconFlow.DSL.annotation.FController;
import io.falconFlow.DSL.annotation.FFunction;
import io.falconFlow.DSL.annotation.FPlugin;
import io.falconFlow.core.inmemory.InMemoryJavaCompiler;
import io.falconFlow.core.inmemory.SourceCode;
import io.falconFlow.core.models.ClassDefination;
import io.falconFlow.helpers.CompilerInstance;
import io.falconFlow.services.constants.ConstantsPackagesnImports;
import io.falconFlow.services.falconparser.ClassExtractor;
import io.falconFlow.services.falconparser.GetterSetterConverter;
import java.util.*;
import javax.tools.*;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.support.GenericApplicationContext;

public class DynamicCompiler {
    private final GenericApplicationContext context;

    //  private final Map<String, byte[]> memoryCache = new ConcurrentHashMap<>();
    //  private final DynamicClassLoader classLoader;

    public DynamicCompiler(GenericApplicationContext context) {
        this.context = context;
        //   this.classLoader = new DynamicClassLoader(this.getClass().getClassLoader());
    }

    public Map.Entry<String, ClassDefination> compileAndRegister(Map<String, ClassDefination> sources)
            throws Exception {

        Map<String, ClassDefination> multiple_sources = new LinkedHashMap<>();
        for (Map.Entry<String, ClassDefination> entry : sources.entrySet()) {
            if (entry.getValue().getClassType().equalsIgnoreCase("model")
                    || entry.getValue().getClassType().equalsIgnoreCase("object")) {
                Map<String, String> str = ClassExtractor.extractClasses(entry.getValue().getSourceCode());
                for (Map.Entry<String, String> mdl : str.entrySet()) {
                    mdl.setValue(
                            ConstantsPackagesnImports.modelPackage
                                    + GetterSetterConverter.addGettersSetters(mdl.getValue()));
                    ClassDefination l = new ClassDefination();
                    l.setSourceCode(mdl.getValue());
                    l.setClassType(entry.getValue().getClassType());
                    l.setClassName(mdl.getKey());
                    multiple_sources.put(mdl.getKey(), l);
                }
            } else {
                multiple_sources.put(entry.getValue().getClassName(), entry.getValue());
            }
        }

        List<String> fqcns = new ArrayList<>();
        InMemoryJavaCompiler c = CompilerInstance.getInstance();

        // 🔹 1️⃣ Add classpath option here
        //    c.useOptions("-classpath", RuntimeClasspathResolver.buildClassPath());
        Map<String, Class<?>> compiledClasses = new HashMap<>();
        for (Map.Entry<String, ClassDefination> entry : multiple_sources.entrySet()) {

            // Map<String, String> str = ClassExtractor.extractClasses(entry.getValue());
            String sourceCode = entry.getValue().getSourceCode();
            String className = extractClassName(sourceCode);
            // System.out.println(className);
            String fqcn = extractFQCN(sourceCode, className);
            fqcns.add(fqcn);
            SourceCode previousCode = c.getSource(fqcn);
            String previoudSourceCode = "";

            if (previousCode != null) {
                previoudSourceCode = String.valueOf(previousCode.getCharContent(false));
            }
            if (multiple_sources.size() == 1) {
                System.out.println("fqcn >>> "+fqcn);
                c.clearSource(fqcn);
                try {
                    Class<?> compiledClass = c.compile(fqcn, sourceCode);

                    compiledClasses.put(fqcn, compiledClass);
                } catch (Exception ex) {

                    System.err.println(ex);
                    c.clearSource(fqcn);
                    if (!previoudSourceCode.isEmpty()) {
                        c.compile(fqcn, previoudSourceCode);
                        //compiledClasses.put(fqcn, previousCode.getClass());
                    }

                    throw new Exception(ex);
                }

            } else {
                c.addSource(fqcn, sourceCode);
            }
        }
        if (multiple_sources.size() > 1) {
            compiledClasses = c.compileAll();
        }

        // initialize beans;
        initBeans(compiledClasses, fqcns);
        // return the "main" class (e.g., first one)
        //  return compiledClasses.values().iterator().next();
        return multiple_sources.entrySet().stream().findFirst().get();
    }

    private void initBeans(Map<String, Class<?>> compiledClasses, List<String> fqcns) {
        for (String fqcn : fqcns) {
            Class<?> cls = compiledClasses.get(fqcn);
            if (cls.isAnnotationPresent(FFunction.class) || cls.isAnnotationPresent(FCondition.class)|| cls.isAnnotationPresent(FController.class)|| cls.isAnnotationPresent(FPlugin.class)) {
                String beanName = "";
                if (cls.isAnnotationPresent(FFunction.class)) {
                    FFunction ann = cls.getAnnotation(FFunction.class);
                    beanName = ann.value().isEmpty() ? cls.getSimpleName() : ann.value();
                } else if (cls.isAnnotationPresent(FCondition.class)) {
                    FCondition ann = cls.getAnnotation(FCondition.class);
                    beanName = ann.value().isEmpty() ? cls.getSimpleName() : ann.value();
                }else if (cls.isAnnotationPresent(FPlugin.class)) {
                    FPlugin ann = cls.getAnnotation(FPlugin.class);
                    beanName = ann.value().isEmpty() ? cls.getSimpleName() : ann.value();
                }else if (cls.isAnnotationPresent(FController.class)) {
                    FController ann = cls.getAnnotation(FController.class);
                    beanName = ann.value().isEmpty() ? cls.getSimpleName() : ann.value();
                }

                if (unregisterBean(context, beanName)) {

                    // return compiledClasses;
                    System.gc();
                    DynamicApiRegistry.unregister(beanName);
                    System.out.println(beanName + " Removed.");
                }


                context.registerBean(
                        beanName,
                        (Class<Object>) cls,
                        () -> {
                            try {
                                Object bean = cls.getDeclaredConstructor().newInstance();
                                context.getAutowireCapableBeanFactory().autowireBean(bean);
                                return bean;
                            } catch (Exception e) {
                                throw new RuntimeException(
                                        "Failed to instantiate dynamic bean " + cls.getName(), e);
                            }
                        });
                context.getBean(beanName);// force init
                //extractAndStoreMetadata(cls);
            }else{
                extractAndStoreMetadata(cls);
            }
        }
    }

    private void extractAndStoreMetadata(Class<?> cls) {
        Map<String, String> members = new LinkedHashMap<>();

        Arrays.stream(cls.getDeclaredFields())
                .forEach(f -> members.put(f.getName(), "property"));

        Arrays.stream(cls.getDeclaredMethods())
                .filter(m -> !m.getName().startsWith("get") && !m.getName().startsWith("set"))
                .forEach(m -> members.put(m.getName(), "function"));

        // ✅ Save in global registry / cache
        DynamicApiRegistry.register(
                cls.getSimpleName().toLowerCase(),
                members
        );

        System.out.println("✅ API metadata updated for: " + cls.getSimpleName());
    }


    public boolean unregisterBean(GenericApplicationContext appContext, String beanName) {
        DefaultListableBeanFactory beanFactory =
                (DefaultListableBeanFactory) appContext.getBeanFactory();
        boolean isDestroyed = false;
        if (beanFactory.containsSingleton(beanName)) {
            beanFactory.destroySingleton(beanName);
            isDestroyed = true;
        }
        if (beanFactory.containsBeanDefinition(beanName)) {
            beanFactory.removeBeanDefinition(beanName);
            isDestroyed = true;
        }

        if (isDestroyed) {
            System.out.println("✅ Bean removed: " + beanName);
        }

        return isDestroyed;
    }

    /**
     * Extract public class name from source
     */
    private String extractClassName(String source) {
        for (String line : Splitter.onPattern("\\R").omitEmptyStrings().trimResults().split(source)) {
            if (line.startsWith("public class ") || line.startsWith("class ")) {
                List<String> tokens = Splitter.onPattern("\\s+").omitEmptyStrings().splitToList(line);
                for (int i = 0; i < tokens.size(); i++) {
                    if (tokens.get(i).equals("class") && i + 1 < tokens.size()) {
                        return tokens.get(i + 1);
                    }
                }
            }
        }
        throw new IllegalStateException("No class found in source");
    }

    /**
     * Extract fully qualified class name (with package if present)
     */
    private String extractFQCN(String source, String className) {
        return source
                .lines()
                .map(String::trim)
                .filter(line -> line.startsWith("package "))
                .findFirst()
                .map(line -> line.replaceFirst("package\\s+", "").replace(";", "").trim() + "." + className)
                .orElse(className);
    }

    /** Custom ClassLoader to define class from byte[] */
    //  public static class DynamicClassLoader extends ClassLoader {
    //    public DynamicClassLoader(ClassLoader parent) {
    //      super(parent);
    //    }
    //
    //    public Class<?> defineClass(String name, byte[] bytes) {
    //      return super.defineClass(name, bytes, 0, bytes.length);
    //    }
    //  }
}
