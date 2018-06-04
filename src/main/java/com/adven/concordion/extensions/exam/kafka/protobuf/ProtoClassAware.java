package com.adven.concordion.extensions.exam.kafka.protobuf;

import com.adven.concordion.extensions.exam.utils.ReflectionUtils;
import com.google.common.base.Optional;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
        return ReflectionUtils.getInstanceByStaticMethod(protoClass, GET_INSTANCE_METHOD_NAME);
    }

    protected T castToProto(final Object object) {
        return protoClass.cast(object);
    }

}
