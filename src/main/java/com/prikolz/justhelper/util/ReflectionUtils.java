package com.prikolz.justhelper.util;

public class ReflectionUtils {
    public static boolean isClassLoaded(String classPath) {
        try {
            Class.forName(classPath);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
