package io.falconFlow.helpers;

import java.lang.reflect.Field;

public class ObjectUtil {

  public static void copyFields(Object src, Object dest) {
    Field[] fields = src.getClass().getDeclaredFields();

    for (Field srcField : fields) {
      try {
        srcField.setAccessible(true);
        Object value = srcField.get(src);

        Field destField;
        try {
          destField = dest.getClass().getDeclaredField(srcField.getName());
        } catch (NoSuchFieldException e) {
          continue; // skip if not present in target
        }

        destField.setAccessible(true);

        // Only copy if type matches
        if (destField.getType().isAssignableFrom(srcField.getType())) {
          destField.set(dest, value);
        }
      } catch (Exception ignored) {
        System.out.println(ignored.getMessage());
      }
    }
  }
}
