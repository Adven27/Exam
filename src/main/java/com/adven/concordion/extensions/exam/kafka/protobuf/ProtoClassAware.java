package com.adven.concordion.extensions.exam.kafka.protobuf;

import com.google.common.base.Optional;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Ruslan Ustits
 */
@Slf4j
@RequiredArgsConstructor
public abstract class ProtoClassAware<F, T> extends ProtoConverter<F, T> {

    private static final String GET_INSTANCE_METHOD_NAME = "getDefaultInstance";

    @NonNull
    @Getter(AccessLevel.PROTECTED)
    private final Class<T> protoClass;

    protected Optional<T> protoInstance() {
        try {
            final Method method = protoClass.getMethod(GET_INSTANCE_METHOD_NAME);
            final T instance = protoClass.cast(method.invoke(null));
            return Optional.of(instance);
        } catch (NoSuchMethodException e) {
            log.error("Unable to find method with name={}", GET_INSTANCE_METHOD_NAME);
        } catch (IllegalAccessException e) {
            log.error("Has no access to initializing object of class={}", protoClass);
        } catch (InvocationTargetException e) {
            log.error("Failed to invoke method={} of class={}", GET_INSTANCE_METHOD_NAME, protoClass);
        }
        return Optional.absent();
    }

    protected T castToProto(final Object object) {
        return protoClass.cast(object);
    }

}
