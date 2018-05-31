package com.adven.concordion.extensions.exam.db.kv.repositories;

import com.adven.concordion.extensions.exam.kafka.protobuf.ProtoToJson;
import com.google.common.base.Optional;
import com.google.protobuf.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.jsonunit.JsonAssert;
import net.javacrumbs.jsonunit.core.Configuration;

/**
 * @author Ruslan Ustits
 */
@Slf4j
@RequiredArgsConstructor
public final class ProtobufProcessor implements ValueProcessor {

    private final Configuration jsonCfg;

    @Override
    public Optional<String> convert(final Object value) {
        Message cast = null;
        try {
            cast = Message.class.cast(value);
        } catch (ClassCastException e) {
            log.error("Failed to cast value={} to string", value);
        }
        return cast == null ? Optional.<String>absent() : new ProtoToJson<>().convert(cast);
    }

    @Override
    public boolean verify(String first, String second) {
        boolean result;
        try {
            JsonAssert.assertJsonEquals(first, second, jsonCfg);
            result = true;
        } catch (AssertionError | Exception e) {
            log.warn("Failed to assert values=[{}, {}]", first, second, e);
            result = false;
        }
        return result;
    }

}
