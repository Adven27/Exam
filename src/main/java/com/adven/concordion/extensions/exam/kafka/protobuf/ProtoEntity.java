package com.adven.concordion.extensions.exam.kafka.protobuf;

import com.google.common.base.Optional;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.kafka.common.utils.Bytes;

import java.util.Arrays;
import java.util.List;

/**
 * @author Ruslan Ustits
 */
@Getter
@RequiredArgsConstructor
public final class ProtoEntity {

    private final String jsonValue;
    private final String className;
    private final List<String> descriptors;

    public ProtoEntity(final String jsonValue, final String className, final String... descriptors) {
        this(jsonValue, className, Arrays.asList(descriptors));
    }

    public byte[] toBytes() {
        val messageOptional = ProtoUtils.fromJsonToProto(jsonValue, className, descriptors);
        if (messageOptional.isPresent()) {
            return messageOptional.get().toByteArray();
        } else {
            return new byte[]{};
        }
    }

    public boolean isEqualTo(final byte[] bytes) {
        final Optional<String> valueToCheck = ProtoUtils.fromBytesToJson(Bytes.wrap(bytes), className, descriptors);
        if (valueToCheck.isPresent()) {
            return isEqualTo(valueToCheck.get());
        }
        return false;
    }

    public boolean isEqualTo(@NonNull final String string) {
        final String expected = cleanup(jsonValue);
        final String actual = cleanup(string);
        return expected.equals(actual);
    }

    protected String cleanup(@NonNull final String message) {
        return message.replaceAll("\\s", "");
    }

}
