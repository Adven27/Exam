package com.adven.concordion.extensions.exam.kafka.protobuf;

import com.google.common.base.Optional;
import com.google.protobuf.Message;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.utils.Bytes;

/**
 * @author Ruslan Ustits
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProtoUtils {

    public static Optional<String> fromBytesToJson(final Bytes bytes, final String className) {
        Optional<Class<Message>> clazz = safeForName(className);
        if (clazz.isPresent()) {
            return new ProtoBytesToJson<>(clazz.get()).convert(bytes);
        } else {
            return Optional.absent();
        }
    }

    public static Optional<Message> fromJsonToProto(final String message, final String className) {
        Optional<Class<Message>> clazz = safeForName(className);
        if (clazz.isPresent()) {
            return new JsonToProto<>(clazz.get()).convert(message);
        } else {
            return Optional.absent();
        }
    }

    protected static Optional<Class<Message>> safeForName(final String name) {
        try {
            return Optional.of((Class<Message>) Class.forName(name));
        } catch (ClassNotFoundException e) {
            log.error("Unable to find class for string={}", name, e);
        }
        return Optional.absent();
    }

}
