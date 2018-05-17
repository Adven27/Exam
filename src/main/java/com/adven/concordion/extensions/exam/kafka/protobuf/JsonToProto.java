package com.adven.concordion.extensions.exam.kafka.protobuf;

import com.google.common.base.Optional;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Ruslan Ustits
 */
@Slf4j
public final class JsonToProto<T extends Message> extends ProtoConverter<String, T> {

    private static final String GET_INSTANCE_METHOD_NAME = "getDefaultInstance";

    private final Class<T> protoClass;

    public JsonToProto(final Class<T> protoClass) {
        this.protoClass = protoClass;
    }

    public Optional<T> convert(final String from) {
        final Optional<T.Builder> builder = buildMessageBuilder();
        if (builder.isPresent()) {
            return Optional.fromNullable(buildProtoObject(from, builder.get()));
        }
        return Optional.absent();
    }

    protected Optional<T.Builder> buildMessageBuilder() {
        T.Builder builder = null;
        try {
            final Method method = protoClass.getMethod(GET_INSTANCE_METHOD_NAME);
            final T message = protoClass.cast(method.invoke(null));
            builder = message.newBuilderForType();
        } catch (NoSuchMethodException e) {
            log.error("Unable to find method with name={}", GET_INSTANCE_METHOD_NAME);
        } catch (IllegalAccessException e) {
            log.error("Has no access to initializing object of class={}", protoClass);
        } catch (InvocationTargetException e) {
            log.error("Failed to invoke method={} of class={}", GET_INSTANCE_METHOD_NAME, protoClass);
        }
        return Optional.fromNullable(builder);
    }

    protected T buildProtoObject(final String json, final T.Builder messageBuilder) {
        addDescriptor(messageBuilder.getDescriptorForType());
        final JsonFormat.TypeRegistry typeRegistry = buildTypeRegistry();
        return buildProtoObject(json, messageBuilder, typeRegistry);
    }

    protected T buildProtoObject(final String json, final T.Builder messageBuilder,
                                 final JsonFormat.TypeRegistry typeRegistry) {
        JsonFormat.Parser parser = JsonFormat.parser();
        try {
            parser.usingTypeRegistry(typeRegistry)
                    .merge(json, messageBuilder);
            return protoClass.cast(messageBuilder.build());
        } catch (InvalidProtocolBufferException e) {
            log.error("Unable to parse json file={} to object={}", json, protoClass);
        }
        return null;
    }

}
