package com.adven.concordion.extensions.exam.kafka.check.verify;

import com.adven.concordion.extensions.exam.kafka.Event;
import com.adven.concordion.extensions.exam.kafka.protobuf.ProtoUtils;
import com.google.common.base.Optional;
import lombok.NonNull;
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
        final Optional<String> message = ProtoUtils.fromBytesToJson(first.getMessage(), protobufClass);
        if (message.isPresent()) {
            final String firstMessage = cleanup(message.get());
            final String secondMessage = cleanup(second.getMessage());
            return firstMessage.equals(secondMessage);
        }
        return false;
    }

    protected String cleanup(@NonNull final String message) {
        return message.replaceAll("\\s", "");
    }

}
