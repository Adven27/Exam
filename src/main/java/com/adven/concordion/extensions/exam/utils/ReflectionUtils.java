package com.adven.concordion.extensions.exam.utils;

import com.google.common.base.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Ruslan Ustits
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReflectionUtils {

    public static <T> Optional<T> getInstanceByStaticMethod(final Class<T> clazz, final String methodName) {
        return getInstanceByStaticMethod(clazz, methodName, clazz);
    }

    public static <T> Optional<T> getInstanceByStaticMethod(final Class<?> clazz, final String methodName,
                                                            final Class<T> toCast) {
        try {
            final Method method = clazz.getMethod(methodName);
            final T instance = toCast.cast(method.invoke(null));
            return Optional.of(instance);
        } catch (NoSuchMethodException e) {
            log.error("Unable to find method with name={}", methodName);
        } catch (IllegalAccessException e) {
            log.error("Has no access to initializing object of class={}", clazz);
        } catch (InvocationTargetException e) {
            log.error("Failed to invoke method={} of class={}", methodName, clazz);
        }
        return Optional.absent();
    }

}
