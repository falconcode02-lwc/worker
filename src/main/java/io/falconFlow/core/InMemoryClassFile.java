package io.falconFlow.core;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URI;
import javax.tools.SimpleJavaFileObject;

public class InMemoryClassFile extends SimpleJavaFileObject {
  private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

  public InMemoryClassFile(String className, Kind kind) {
    super(URI.create("mem:///" + className.replace('.', '/') + kind.extension), kind);
  }

  @Override
  public OutputStream openOutputStream() {
    return outputStream;
  }

  public byte[] getBytes() {
    return outputStream.toByteArray();
  }
}
