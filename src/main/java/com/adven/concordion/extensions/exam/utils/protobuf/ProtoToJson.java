package com.adven.concordion.extensions.exam.utils.protobuf;

import com.google.common.base.Optional;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ProtoToJson<T extends Message> extends ProtoConverter<T, String> {

    public Optional<String> convert(final T from) {
        if (from == null) {
            return Optional.absent();
        }
        addDescriptor(from.getDescriptorForType());

        final JsonFormat.TypeRegistry registry = buildTypeRegistry();
        String json = null;
        try {
            json = JsonFormat.printer()
                .usingTypeRegistry(registry)
                .print(from);
        } catch (InvalidProtocolBufferException e) {
            log.error("Unable to convert message={} to json", from);
        }
        return Optional.fromNullable(json);
    }
}
