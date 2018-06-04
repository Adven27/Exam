package com.adven.concordion.extensions.exam.kafka.protobuf;

import com.adven.concordion.extensions.exam.html.Html;
import com.google.common.base.Optional;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Ruslan Ustits
 */
public final class ProtoBlockParser {

    private static final String PROTOBUF = "protobuf";
    private static final String CLASS = "class";
    private static final String DESCRIPTOR = "descriptor";
    private static final String DESCRIPTORS = DESCRIPTOR + "s";

    public Optional<ProtoEntity> parse(final Html html) {
        val protoBlock = html.first(PROTOBUF);
        if (protoBlock == null) {
            return Optional.absent();
        }
        val text = protoBlock.text();
        if (StringUtils.isBlank(text)) {
            return Optional.absent();
        }
        val className = protoBlock.attr(CLASS);

        final List<String> descriptors = new ArrayList<>();

        val descriptorAttr = protoBlock.attr(DESCRIPTOR);
        if (descriptorAttr != null) {
            descriptors.add(descriptorAttr);
        } else {
            descriptors.addAll(parseDescriptors(protoBlock));
        }
        return Optional.of(new ProtoEntity(text, className, descriptors));
    }

    private List<String> parseDescriptors(final Html protoBlock) {
        val descriptors = protoBlock.attr(DESCRIPTORS);
        final List<String> descriptorsList;
        if (descriptors == null) {
            descriptorsList = Collections.emptyList();
        } else {
            descriptorsList = new ArrayList<>(Arrays.asList(descriptors.split(",")));
        }
        return descriptorsList;
    }

}
