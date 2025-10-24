package com.docflow.util;

import java.lang.reflect.Method;

public final class BeanSetter {
    private BeanSetter() {}

    public static void trySet(Object target, String propName, Object value) {
        if (target == null || propName == null) return;
        String setter = "set" + Character.toUpperCase(propName.charAt(0)) + propName.substring(1);
        Method[] methods = target.getClass().getMethods();
        for (Method m : methods) {
            if (m.getName().equals(setter) && m.getParameterCount() == 1) {
                try {
                    Class<?> p = m.getParameterTypes()[0];
                    Object coerced = coerce(value, p);
                    if (coerced != null || !p.isPrimitive()) {
                        m.invoke(target, coerced);
                        return;
                    }
                } catch (Exception ignored) {}
            }
        }
    }

    private static Object coerce(Object value, Class<?> to) {
        if (value == null) return null;
        if (to.isAssignableFrom(value.getClass())) return value;
        if (to == long.class || to == Long.class) {
            if (value instanceof Number) return ((Number) value).longValue();
        }
        if (to == int.class || to == Integer.class) {
            if (value instanceof Number) return ((Number) value).intValue();
        }
        if (to == String.class) return String.valueOf(value);
        return value;
    }
}
