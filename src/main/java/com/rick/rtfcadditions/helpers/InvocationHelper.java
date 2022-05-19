/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rick.rtfcadditions.helpers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class InvocationHelper
{
    public static Object invoke(Method method, Object obj, Object... args)
    {
        try {
            return method.invoke(obj, args);
        } catch (IllegalAccessException ex) {
            throw new ReflectionInvocationException(ex);
        } catch (IllegalArgumentException ex) {
            throw new ReflectionInvocationException(ex);
        } catch (InvocationTargetException ex) {
            throw new ReflectionInvocationException(ex);
        }
    }

    public static Method getMethod(Object instance, String methodName, Class<?>... parameterTypes)
    {
        try {
            Method method = instance.getClass().getMethod(methodName, parameterTypes);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException ex) {
            throw new ReflectionInvocationException(ex);
        } catch (SecurityException ex) {
            throw new ReflectionInvocationException(ex);
        }
    }

    public static class ReflectionInvocationException extends RuntimeException
    {

        public ReflectionInvocationException(Throwable failed)
        {
            super(failed);
        }

    }
}
