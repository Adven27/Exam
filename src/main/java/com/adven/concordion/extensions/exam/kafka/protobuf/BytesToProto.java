package com.adven.concordion.extensions.exam.kafka.protobuf;

import com.google.common.base.Optional;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.Parser;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.utils.Bytes;

@Slf4j
public final class BytesToProto<T extends Message> extends ProtoClassAware<Bytes, T> {

    public BytesToProto(final Class<T> protoClass) {
        super(protoClass);
    }

    @Override
    public Optional<T> convert(final Bytes from) {
        if (from == null || from.get().length == 0) {
            return Optional.absent();
        }
        return parse(from.get());
    }

    protected Optional<T> parse(@NonNull final byte[] bytes) {
        final Optional<T> entity = protoInstance();
        if (entity.isPresent()) {
            final Parser<? extends Message> parser = entity.get().getParserForType();
            try {
                final T result = castToProto(parser.parseFrom(bytes));
                return Optional.of(result);
            } catch (InvalidProtocolBufferException e) {
                log.error("Unable to convert bytes to proto toBytes");
            }
        }
        return Optional.absent();
    }

}
