package com.adven.concordion.extensions.exam.kafka.protobuf;

import com.google.common.base.Optional;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Ruslan Ustits
 */
@Slf4j
public final class JsonToProto<T extends Message> extends ProtoClassAware<String, T> {

    public JsonToProto(final Class<T> protoClass) {
        super(protoClass);
    }

    public Optional<T> convert(final String from) {
        if (StringUtils.isBlank(from)) {
            log.warn("Unable to convert from blank string");
            return Optional.absent();
        }
        final Optional<T.Builder> builder = buildMessageBuilder();
        if (builder.isPresent()) {
            return Optional.fromNullable(buildProtoObject(from, builder.get()));
        }
        return Optional.absent();
    }

    protected Optional<T.Builder> buildMessageBuilder() {
        final Optional<T> entity = protoInstance();
        if (entity.isPresent()) {
            return Optional.of(entity.get().newBuilderForType());
        } else {
            return Optional.absent();
        }
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
            return castToProto(messageBuilder.build());
        } catch (InvalidProtocolBufferException e) {
            log.error("Unable to parse json file={} to object={}", json, getProtoClass());
        }
        return null;
    }

}
