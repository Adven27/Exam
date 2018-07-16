package com.adven.concordion.extensions.exam.entities;

import com.adven.concordion.extensions.exam.utils.protobuf.ProtoToJson;
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
public final class ProtoEntity extends AbstractEntity {

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
        val messageOptional = ProtoUtils.fromJsonToProto(getValue(), className, descriptors);
        if (messageOptional.isPresent()) {
            return messageOptional.get().toByteArray();
        } else {
            return new byte[]{};
        }
    }

    @Override
    public boolean isEqualTo(final byte[] bytes) {
        final Optional<String> valueToCheck = ProtoUtils.fromBytesToJson(Bytes.wrap(bytes), className, descriptors);
        if (valueToCheck.isPresent()) {
            return isEqualTo(valueToCheck.get());
        }
        return false;
    }

    @Override
    public boolean isEqualTo(final Object object) {
        Message cast = null;
        try {
            cast = Message.class.cast(object);
        } catch (ClassCastException e) {
            log.error("Failed to cast value={} to string", object);
        }
        if (cast == null) {
            return false;
        } else {
            val convert = new ProtoToJson<>().convert(cast);
            return convert.isPresent() && isEqualTo(convert.get());
        }
    }

}
