package com.adven.concordion.extensions.exam.kafka.check.verify;

import com.adven.concordion.extensions.exam.kafka.Event;
import com.adven.concordion.extensions.exam.kafka.protobuf.ProtoBytesToJson;
import com.google.common.base.Optional;
import com.google.protobuf.Message;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.utils.Bytes;

/**
 * @author Ruslan Ustits
 */
@RequiredArgsConstructor
public final class MessageVerifier implements Verifier {

    private final String protobufClass;

    @Override
    public boolean verify(final Event<Bytes> first, final Event<String> second) {
        final Optional<String> message = convertToJson(first.getMessage());
        return message.isPresent() && message.get().equals(second.getMessage());
    }

    private Optional<String> convertToJson(final Bytes message) {
        final ProtoBytesToJson<? extends Message> converter = ProtoBytesToJson.forProtoClass(protobufClass);
        if (converter != null) {
            return converter.convert(message);
        } else {
            return Optional.absent();
        }
    }

}
