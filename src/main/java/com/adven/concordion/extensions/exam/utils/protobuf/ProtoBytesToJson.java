package com.adven.concordion.extensions.exam.utils.protobuf;

import com.google.common.base.Optional;
import com.google.protobuf.Message;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.utils.Bytes;

@Slf4j
public final class ProtoBytesToJson<T extends Message> extends ProtoConverter<Bytes, String> {

    private final ProtoToJson<T> protoToJson;
    private final BytesToProto<T> bytesToProto;

    public ProtoBytesToJson(@NonNull final Class<T> protoClass) {
        protoToJson = new ProtoToJson<>();
        bytesToProto = new BytesToProto<>(protoClass);
    }

    @Override
    public Optional<String> convert(final Bytes from) {
        bytesToProto.addAllDescriptors(getDescriptors());
        protoToJson.addAllDescriptors(getDescriptors());
        final Optional<T> proto = bytesToProto.convert(from);
        if (proto.isPresent()) {
            return protoToJson.convert(proto.get());
        } else {
            return Optional.absent();
        }
    }

}
