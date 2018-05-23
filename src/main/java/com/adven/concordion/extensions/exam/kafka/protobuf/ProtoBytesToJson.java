package com.adven.concordion.extensions.exam.kafka.protobuf;

import com.google.common.base.Optional;
import com.google.protobuf.Message;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.utils.Bytes;

/**
 * @author Ruslan Ustits
 */
@Slf4j
public final class ProtoBytesToJson<T extends Message> extends ProtoConverter<Bytes, String> {

    private final ProtoToJson<T> protoToJson;
    private final BytesToProto<T> bytesToProto;

    public ProtoBytesToJson(@NonNull final Class<T> protoClass) {
        protoToJson = new ProtoToJson<>();
        bytesToProto = new BytesToProto<>(protoClass);
    }

    public static <T extends Message> ProtoBytesToJson<T> forProtoClass(final String className) {
        try {
            final Class<T> clazz = (Class<T>) Class.forName(className);
            return new ProtoBytesToJson<>(clazz);
        } catch (ClassNotFoundException e) {
            log.error("Unable to instantiate converter for class={}", className, e);
        }
        return null;
    }

    @Override
    public Optional<String> convert(final Bytes from) {
        final Optional<T> proto = bytesToProto.convert(from);
        if (proto.isPresent()) {
            return protoToJson.convert(proto.get());
        } else {
            return Optional.absent();
        }
    }

}
