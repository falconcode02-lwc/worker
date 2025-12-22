package io.falconFlow.helpers;

import io.falconFlow.core.inmemory.InMemoryJavaCompiler;

public class CompilerInstance {
  static final InMemoryJavaCompiler c = InMemoryJavaCompiler.newInstance();
  ;
  public static InMemoryJavaCompiler getInstance() {
    return c;
  }
}
