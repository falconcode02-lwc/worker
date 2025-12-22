package io.falconFlow.core;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.util.*;

public class RuntimeClasspathResolver {

  public static String buildClassPath() {
    String catalinaBase = System.getProperty("catalina.base");
    if (catalinaBase != null && Files.exists(Paths.get(catalinaBase, "webapps"))) {
      return buildTomcatWebAppClassPath(catalinaBase);
    } else {
      return buildSpringBootClassPath();
    }
  }

  private static String buildTomcatWebAppClassPath(String catalinaBase) {
    String appName = detectDeployedAppName(catalinaBase);
    Path webInfClasses = Paths.get(catalinaBase, "webapps", appName, "WEB-INF", "classes");
    Path webInfLib = Paths.get(catalinaBase, "webapps", appName, "WEB-INF", "lib");

    StringBuilder cp = new StringBuilder();
    cp.append(webInfClasses.toAbsolutePath());
    try (DirectoryStream<Path> jars = Files.newDirectoryStream(webInfLib, "*.jar")) {
      for (Path jar : jars) {
        cp.append(File.pathSeparator).append(jar.toAbsolutePath());
      }
    } catch (Exception e) {
      System.out.println(e);
    }
    return cp.toString();
  }

  private static String detectDeployedAppName(String catalinaBase) {
    try {
      Path webappsDir = Paths.get(catalinaBase, "webapps");
      try (DirectoryStream<Path> stream = Files.newDirectoryStream(webappsDir)) {
        for (Path war : stream) {
          if (Files.isDirectory(war) && Files.exists(war.resolve("WEB-INF"))) {
            return war.getFileName().toString();
          }
        }
      }
    } catch (Exception ignored) {
      System.out.println(ignored);
    }
    return "ROOT";
  }

  private static String buildSpringBootClassPath() {
    StringBuilder cp = new StringBuilder();

    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    if (cl instanceof URLClassLoader ucl) {
      for (URL url : ucl.getURLs()) {
        appendIfExists(cp, url);
      }
    } else {
      // For Spring Boot's LaunchedURLClassLoader (fat JAR)
      try {
        Enumeration<URL> urls = cl.getResources("");
        while (urls.hasMoreElements()) {
          URL url = urls.nextElement();
          appendIfExists(cp, url);
        }
      } catch (Exception e) {
        System.out.println(e);
      }
    }

    if (cp.length() == 0) {
      cp.append(System.getProperty("java.class.path"));
    }

    return cp.toString();
  }

  private static void appendIfExists(StringBuilder cp, URL url) {
    try {
      File f = new File(url.toURI());
      if (f.exists()) {
        if (cp.length() > 0) cp.append(File.pathSeparator);
        cp.append(f.getAbsolutePath());
      }
    } catch (Exception ignored) {
      System.out.println(ignored);
    }
  }

  // Helper for debug
  public static void printClassPath() {
    System.out.println("=== Detected Classpath ===");
    Arrays.stream(buildClassPath().split(File.pathSeparator)).forEach(System.out::println);
  }
}
