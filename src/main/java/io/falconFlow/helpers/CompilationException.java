package io.falconFlow.helpers;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.util.List;

public class CompilationException extends Exception {

  private final List<Diagnostic<? extends JavaFileObject>> diagnostics;

  public CompilationException(List<Diagnostic<? extends JavaFileObject>> diagnostics) {
    super(buildMessage(diagnostics));
    this.diagnostics = diagnostics;
  }

  public List<Diagnostic<? extends JavaFileObject>> getDiagnostics() {
    return diagnostics;
  }

  private static String buildMessage(List<Diagnostic<? extends JavaFileObject>> diagnostics) {
    StringBuilder sb = new StringBuilder("Compilation failed with errors:\n");
    for (Diagnostic<? extends JavaFileObject> d : diagnostics) {
      sb.append(
          String.format(
              "Kind: %s, Line: %d, Message: %s%n",
              d.getKind(), d.getLineNumber(), d.getMessage(null)));
    }
    return sb.toString();
  }
}
