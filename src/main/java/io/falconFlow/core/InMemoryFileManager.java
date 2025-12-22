package io.falconFlow.core;

import java.util.HashMap;
import java.util.Map;
import javax.tools.*;

public class InMemoryFileManager extends ForwardingJavaFileManager<JavaFileManager> {
  private final Map<String, InMemoryClassFile> classFiles = new HashMap<>();

  protected InMemoryFileManager(JavaFileManager fileManager) {
    super(fileManager);
  }

  @Override
  public JavaFileObject getJavaFileForOutput(
      Location location, String className, JavaFileObject.Kind kind, FileObject sibling) {
    InMemoryClassFile classFile = new InMemoryClassFile(className, kind);
    classFiles.put(className, classFile); // ✅ store ALL compiled classes (outer + inner)
    return classFile;
  }

  public Map<String, byte[]> getAllClassBytes() {
    Map<String, byte[]> result = new HashMap<>();
    for (var entry : classFiles.entrySet()) {
      result.put(entry.getKey(), entry.getValue().getBytes());
    }
    return result;
  }
}
