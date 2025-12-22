package io.falconFlow.core;

import java.net.URI;
import javax.tools.SimpleJavaFileObject;

public class InMemoryJavaFile extends SimpleJavaFileObject {
  private final String sourceCode;

  public InMemoryJavaFile(String className, String sourceCode) {
    super(
        URI.create("string:///" + className.replace('.', '/') + Kind.SOURCE.extension),
        Kind.SOURCE);
    this.sourceCode = sourceCode;
  }

  @Override
  public CharSequence getCharContent(boolean ignoreEncodingErrors) {
    return sourceCode;
  }
}
