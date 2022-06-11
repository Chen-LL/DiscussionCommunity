package com.kuney.community.util;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

/**
 * @author kuneychen
 * @since 2022/6/10 14:47
 */
public class ObjCheckUtils {

    /**
     * If the object is null, return true, else return false.
     *
     * @param obj the object
     * @return the boolean
     */
    public static boolean isNull(Object obj) {
        return obj == null;
    }


    /**
     * If one is null, return true, else return false.
     *
     * @param objects the objects
     * @return the boolean
     */
    public static boolean isNull(Object... objects) {
        for (Object obj : objects) {
            if (isNull(obj)) {
                return true;
            }
        }
        return false;
    }

    /**
     * If the object is not null, return true, else return false.
     *
     * @param obj the object
     * @return the boolean
     */
    public static boolean nonNull(Object obj) {
        return obj != null;
    }

    /**
     * If all is not null, return true, else return false.
     *
     * @param objects the objects
     * @return the boolean
     */
    public static boolean nonNull(Object... objects) {
        return !isNull(objects);
    }

    /**
     * If the string is null or empty,
     * or the content is all space symbols,
     * or the content is "null",
     * return true, else return false.
     *
     * @param str the str
     * @return the boolean
     */
    public static boolean isBlank(String str) {
        return isNull(str) || str.isEmpty() || str.trim().isEmpty() || "null".equals(str);
    }

    /**
     * Is blank boolean.
     *
     * @param strings the strings
     * @return the boolean
     */
    public static boolean isBlank(String... strings) {
        for (String str : strings) {
            if (isBlank(str)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Is not blank boolean.
     *
     * @param str the strings
     * @return the boolean
     */
    public static boolean nonBlank(String str) {
        return !isBlank(str);
    }

    /**
     * Is not blank boolean.
     *
     * @param strings the strings
     * @return the boolean
     */
    public static boolean nonBlank(String... strings) {
        return !isBlank(strings);
    }

    /**
     * check Array,Collection,Map
     *
     * @param obj the object
     * @return the boolean
     */
    public static boolean isEmpty(Object obj) {
        if (obj == null) {
            return true;
        }

        if (obj.getClass().isArray()) {
            return Array.getLength(obj) == 0;
        }
        if (obj instanceof Collection) {
            return ((Collection<?>) obj).isEmpty();
        }
        if (obj instanceof Map) {
            return ((Map<?, ?>) obj).isEmpty();
        }

        // else
        return false;
    }

    /**
     * If one is empty, the result is true;
     * If all is non empty,the result is false.
     *
     * @param objs the objects
     * @return the boolean
     */
    public static boolean isEmpty(Object... objs) {
        for (Object obj : objs) {
            if (isEmpty(obj)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Non empty boolean.
     *
     * @param obj the object
     * @return the boolean
     */
    public static boolean nonEmpty(Object obj) {
        return !isEmpty(obj);
    }

    /**
     * Is not empty boolean.
     *
     * @param objs the objects
     * @return the boolean
     */
    public static boolean nonEmpty(Object... objs) {
        return !isEmpty(objs);
    }


}
