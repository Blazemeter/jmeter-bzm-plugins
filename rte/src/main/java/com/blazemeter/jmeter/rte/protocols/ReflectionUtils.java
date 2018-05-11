package com.blazemeter.jmeter.rte.protocols;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Utility class to avoid reflection boiler plate code used with existing libraries that don't
 * provide proper access for required extension.
 */
public class ReflectionUtils {

  public static Method getAccessibleMethod(Class<?> clazz, String methodName) {
    try {
      Method method = clazz.getDeclaredMethod(methodName);
      method.setAccessible(true);
      return method;
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e); //NOSONAR
    }
  }

  public static Field getAccessibleField(Class<?> clazz, String fieldName) {
    try {
      Field target = clazz.getDeclaredField(fieldName);
      target.setAccessible(true);
      return target;
    } catch (NoSuchFieldException e) {
      throw new RuntimeException(e); //NOSONAR
    }
  }

  public static <T> T getFieldValue(Field field, Class<T> fieldClass, Object object) {
    try {
      return fieldClass.cast(field.get(object));
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e); //NOSONAR
    }
  }

  public static void setFieldValue(Field field, Object value, Object object) {
    try {
      field.set(object, value);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e); //NOSONAR
    }
  }

  public static void invokeMethod(Method method, Object object) {
    invokeMethod(method, Void.class, object);
  }

  public static <T> T invokeMethod(Method method, Class<T> returnType, Object object) {
    try {
      return returnType.cast(method.invoke(object));
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new RuntimeException(e); //NOSONAR
    }
  }

}
