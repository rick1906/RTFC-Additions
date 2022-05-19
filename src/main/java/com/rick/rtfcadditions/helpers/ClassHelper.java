package com.rick.rtfcadditions.helpers;

import com.rick.rtfcadditions.debug.DebugUtils;
import java.lang.reflect.Field;

/**
 *
 * @author Rick
 */
public abstract class ClassHelper
{

    public static Field findField(Object object, String name)
    {
        return findField(object.getClass(), name, true);
    }

    public static Field findField(Class clazz, String name)
    {
        return findField(clazz, name, true);
    }

    public static Field findField(Class clazz, String name, boolean inSuperClasses)
    {
        try {
            Field field = clazz.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException ex) {
            if (inSuperClasses) {
                Class superClass = clazz.getSuperclass();
                if (superClass != null) {
                    return findField(superClass, name, inSuperClasses);
                }
            }
            return null;
        }
    }

    public static Field findField(Object object, String... names)
    {
        if (names.length > 0) {
            Field field = findField(object, names[0]);
            for (int i = 1; i < names.length; ++i) {
                object = getFieldValue(object, field);
                if (object != null) {
                    field = findField(object, names[i]);
                }
            }
            return field;
        }
        return null;
    }

    public static Field findField(Class clazz, Class type)
    {
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.getType() == type) {
                field.setAccessible(true);
                return field;
            }
        }
        return null;
    }

    public static Object getFieldValue(Object object, Field field)
    {
        try {
            return field.get(object);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            return null;
        }
    }

    public static Object getFieldValue(Object object, String name)
    {
        Field field = findField(object, name);
        if (field != null) {
            return getFieldValue(object, field);
        }
        return null;
    }

    public static boolean setFieldValue(Object object, Field field, Object value)
    {
        try {
            field.set(object, value);
            return true;
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            return false;
        }
    }

    public static boolean setFieldValue(Object object, String name, Object value)
    {
        Field field = findField(object, name);
        if (field != null) {
            return setFieldValue(object, name, value);
        }
        return false;
    }
}
