package io.falconFlow.core.inmemory;

import java.util.HashMap;
import java.util.Map;

public class DynamicClassLoader extends ClassLoader {

  private Map<String, CompiledCode> customCompiledCode = new HashMap<>();

  public DynamicClassLoader(ClassLoader parent) {
    super(parent);
  }

  public void addCode(CompiledCode cc) {
    customCompiledCode.put(cc.getName(), cc);
  }

  @Override
  protected Class<?> findClass(String name) throws ClassNotFoundException {
    CompiledCode cc = customCompiledCode.get(name);
      System.out.print(name + ">>>"); System.out.println(cc);
    if (cc == null) {
        System.out.print(super.getParent());
      return super.getParent().loadClass(name);
    }
    byte[] byteCode = cc.getByteCode();
    return defineClass(name, byteCode, 0, byteCode.length);
  }
}
