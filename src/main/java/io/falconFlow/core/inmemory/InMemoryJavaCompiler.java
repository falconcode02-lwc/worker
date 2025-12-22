package io.falconFlow.core.inmemory;

import java.io.File;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import javax.tools.*;

/**
 * InMemoryJavaCompiler -------------------- Dynamically compiles Java source code in memory at
 * runtime. Works across Spring Boot fat JAR/WAR, external Tomcat, and IDE execution.
 */
/// ** Compile Java sources in-memory */
public class InMemoryJavaCompiler {
  private JavaCompiler javac;
  private DynamicClassLoader classLoader;
  private Iterable<String> options;
  boolean ignoreWarnings = false;

  private Map<String, SourceCode> sourceCodes = new HashMap<String, SourceCode>();

  public static InMemoryJavaCompiler newInstance() {
    return new InMemoryJavaCompiler();
  }

  private InMemoryJavaCompiler() {
    this.javac = ToolProvider.getSystemJavaCompiler();
    this.classLoader = new DynamicClassLoader(Thread.currentThread().getContextClassLoader());
  }

  public InMemoryJavaCompiler useParentClassLoader(ClassLoader parent) {
    this.classLoader = new DynamicClassLoader(parent);
    return this;
  }

  public ClassLoader getClassloader() {
    return classLoader;
  }

  public InMemoryJavaCompiler useOptions(String... options) {
    this.options = Arrays.asList(options);

    return this;
  }

  public InMemoryJavaCompiler ignoreWarnings() {
    ignoreWarnings = true;
    return this;
  }

  public Map<String, Class<?>> compileAll() throws Exception {
    if (sourceCodes.isEmpty()) {
      throw new CompilationException("No source code to compile");
    }

    Collection<SourceCode> compilationUnits = sourceCodes.values();
    DiagnosticCollector<JavaFileObject> collector = new DiagnosticCollector<>();
      if (this.classLoader != null) {
          this.classLoader = null;
          System.gc(); // optional hint
      }
    this.classLoader = new DynamicClassLoader(Thread.currentThread().getContextClassLoader());
    ExtendedStandardJavaFileManager fileManager =
        new ExtendedStandardJavaFileManager(
            javac.getStandardFileManager(null, null, null), classLoader);

    String classPath = buildRuntimeClasspath();

      System.out.println(System.getProperty("java.home"));
      System.out.println(System.getProperty("java.version"));
      System.out.println(ToolProvider.getSystemJavaCompiler());

      String javaVersion = System.getProperty("java.version");
      boolean isValhalla = javaVersion.contains("valhalla") || javaVersion.contains("23");

      if (isValhalla) {
          // For JDK 23+ builds
         // useOptions("--proc:none", "-Xlint:-options");
      } else {
          // For JDK 17 or stable

          useOptions("-proc:none", "-Xlint:-options","--proc:none");
      }


//    System.out.println("classPath >> Loaded");
    useOptions("-classpath", classPath );

//      useOptions("-proc:none");       // disable annotation processing
//     useOptions("-Xlint:unchecked");
//      useOptions("-Xdiags:verbose");

      // silence "NOTE" message


    JavaCompiler.CompilationTask task =
        javac.getTask(null, fileManager, collector, options, null, compilationUnits);

    boolean result = task.call();

    if (!result || !collector.getDiagnostics().isEmpty()) {
      StringBuilder exceptionMsg = new StringBuilder();
      exceptionMsg.append("Unable to compile the source");
      for (Diagnostic<? extends JavaFileObject> d : collector.getDiagnostics()) {
        exceptionMsg
            .append("\n[kind=")
            .append(d.getKind())
            .append(", line=")
            .append(d.getLineNumber())
            .append(", message=")
            .append(d.getMessage(Locale.US))
            .append("]");
      }
      throw new CompilationException(exceptionMsg.toString());
    }

    Map<String, Class<?>> classes = new HashMap<>();
    for (String className : sourceCodes.keySet()) {
      classes.put(className, classLoader.loadClass(className));
    }
    return classes;
  }

  public Class<?> compile(String className, String sourceCode) throws Exception {
    return addSource(className, sourceCode).compileAll().get(className);
  }

  public SourceCode getSource(String className) throws Exception {
    return sourceCodes.get(className);
  }

  public InMemoryJavaCompiler addSource(String className, String sourceCode) throws Exception {
    sourceCodes.put(className, new SourceCode(className, sourceCode));
    return this;
  }

  public void clearSource(String className) {


    sourceCodes.remove(className);
  }

  private String buildRuntimeClasspath() {
    StringBuilder cp = new StringBuilder();

    try {

      System.out.println("Detected ClassLoader: " + this.classLoader.getClass().getName());

      // ✅ Spring Boot embedded case (WAR or JAR)
      if (this.classLoader.getClass().getName().contains("LaunchedURLClassLoader")) {
        try {
          var getURLs = this.classLoader.getClass().getMethod("getURLs");
          getURLs.setAccessible(true);
          URL[] urls = (URL[]) getURLs.invoke(this.classLoader);
          for (URL url : urls) {
            File f = new File(url.toURI());
            if (f.exists()) {
              cp.append(f.getAbsolutePath()).append(File.pathSeparator);
            }
          }
        } catch (Exception e) {
          System.err.println(e);
        }
      }

      // ✅ External Tomcat (exploded)
      else if (System.getProperty("catalina.base") != null) {
        String catalinaBase = System.getProperty("catalina.base");
        Path webappsDir = Paths.get(catalinaBase, "webapps");
        System.err.println(webappsDir);
        if (Files.exists(webappsDir)) {
          try (DirectoryStream<Path> apps = Files.newDirectoryStream(webappsDir)) {
            for (Path appDir : apps) {
              if (Files.isDirectory(appDir.resolve("WEB-INF"))) {
                Path classesDir = appDir.resolve("WEB-INF/classes");
                Path libDir = appDir.resolve("WEB-INF/lib");

                if (Files.exists(classesDir))
                  cp.append(classesDir.toAbsolutePath()).append(File.pathSeparator);

                if (Files.exists(libDir)) {
                  try (DirectoryStream<Path> jars = Files.newDirectoryStream(libDir, "*.jar")) {
                    for (Path jar : jars) {
                        cp.append(jar.toAbsolutePath()).append(File.pathSeparator);
                        System.err.println(jar.toAbsolutePath());
                    }
                  }
                }
              }
            }
          }
        }
      }

      // ✅ Fallback for IDE / plain Java run
      if (cp.isEmpty()) {
        cp.append(System.getProperty("java.class.path"));
      }

    } catch (Exception e) {
      System.err.println(e);
    }

    System.out.println("====== Runtime Classpath Detected ======");
    System.out.println(cp.length());
    System.out.println("========================================");
    return cp.toString();
  }


}
