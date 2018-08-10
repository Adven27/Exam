package com.adven.concordion.extensions.exam.entities;

import com.adven.concordion.extensions.exam.utils.protobuf.ProtoUtils;
import com.google.common.base.Optional;
import com.google.protobuf.Message;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.kafka.common.utils.Bytes;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Getter
public final class ProtoEntity extends AbstractEntity<Message> {

    private final String className;
    private final List<String> descriptors;

    public ProtoEntity(final String value, final String className, final List<String> descriptors) {
        super(value);
        this.className = className;
        this.descriptors = descriptors;
    }

    public ProtoEntity(final String jsonValue, final String className, final String... descriptors) {
        this(jsonValue, className, Arrays.asList(descriptors));
    }

    @Override
    public byte[] toBytes() {
        val original = original();
        if (original.isPresent()) {
            return original.get().toByteArray();
        } else {
            return new byte[]{};
        }
    }

    @Override
    public Optional<Message> original() {
        return ProtoUtils.fromJsonToProto(getValue(), className, descriptors);
    }

    @Override
    public boolean isEqualTo(final byte[] bytes) {
        final Optional<Message> actual = ProtoUtils.fromBytesToProto(Bytes.wrap(bytes), className, descriptors);
        if (actual.isPresent()) {
            return isEqualTo(actual.get());
        }
        log.error("Failed to convert bytes={} to protobuf className={}", bytes, className);
        return false;
    }

    @Override
    public boolean isEqualTo(final Object object) {
        Message actual = null;
        try {
            actual = (Message) object;
        } catch (ClassCastException e) {
            log.error("Failed to cast value={} to protobuf class", object);
        }
        boolean result;
        if (actual == null) {
            result = false;
        } else {
            final Optional<Message> convert = ProtoUtils.fromJsonToProto(getValue(), className, descriptors);
            if (convert.isPresent()) {
                final Message expected = convert.get();
                result = actual.equals(expected);
                if (!result) {
                    log.error("Expected={} is not equal to actual={}", expected, actual);
                }
            } else {
                log.error("Failed to convert value={} for protobuf className={}", getValue(), className);
                result = false;
            }
        }
        return result;
    }

}
