package com.adven.concordion.extensions.exam.utils.protobuf;

import com.adven.concordion.extensions.exam.utils.ReflectionUtils;
import com.google.common.base.Optional;
import com.google.protobuf.Message;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.kafka.common.utils.Bytes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.google.protobuf.Descriptors.Descriptor;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProtoUtils {

    private static final String DESCRIPTOR_METHOD = "getDescriptor";

    public static Optional<String> fromBytesToJson(final Bytes bytes, final String className,
                                                   final Descriptor... descriptors) {
        Optional<Class<Message>> clazz = safeForName(className);
        if (clazz.isPresent()) {
            final ProtoBytesToJson<Message> converter = new ProtoBytesToJson<>(clazz.get());
            converter.addAllDescriptors(Arrays.asList(descriptors));
            return converter.convert(bytes);
        } else {
            return Optional.absent();
        }
    }

    public static Optional<String> fromBytesToJson(final Bytes bytes, final String className,
                                                   final List<String> descriptorClasses) {
        final List<Descriptor> descriptors = descriptorInstances(descriptorClasses);
        return fromBytesToJson(bytes, className, descriptors.toArray(new Descriptor[]{}));
    }

    public static Optional<Message> fromJsonToProto(final String message, final String className,
                                                    final Descriptor... descriptors) {
        Optional<Class<Message>> clazz = safeForName(className);
        if (clazz.isPresent()) {
            final JsonToProto<Message> converter = new JsonToProto<>(clazz.get());
            converter.addAllDescriptors(Arrays.asList(descriptors));
            return converter.convert(message);
        } else {
            return Optional.absent();
        }
    }

    public static Optional<Message> fromJsonToProto(final String message, final String className,
                                                    final List<String> descriptorClasses) {
        final List<Descriptor> descriptors = descriptorInstances(descriptorClasses);
        return fromJsonToProto(message, className, descriptors.toArray(new Descriptor[]{}));
    }

    public static List<Descriptor> descriptorInstances(final List<String> descriptorClasses) {
        final List<Descriptor> descriptors = new ArrayList<>();
        for (val descClass : descriptorClasses) {
            Optional<Descriptor> descriptor = descriptorInstance(descClass);
            if (descriptor.isPresent()) {
                descriptors.add(descriptor.get());
            } else {
                log.warn("Can't get descriptor for class={}", descClass);
            }
        }
        return descriptors;
    }

    public static Optional<Descriptor> descriptorInstance(final String className) {
        Optional<Class<Descriptor>> descriptorClass = safeForName(className);
        if (descriptorClass.isPresent()) {
            return ReflectionUtils.getInstanceByStaticMethod(
                descriptorClass.get(),
                DESCRIPTOR_METHOD,
                Descriptor.class);
        } else {
            return Optional.absent();
        }
    }

    public static Optional<Descriptor> descriptorInstance(final Class<?> protoClass) {
        return ReflectionUtils.getInstanceByStaticMethod(protoClass, DESCRIPTOR_METHOD, Descriptor.class);
    }

    protected static <T> Optional<T> safeForName(final String name) {
        try {
            return Optional.of((T) Class.forName(name));
        } catch (ClassNotFoundException e) {
            log.error("Unable to find class for string={}", name, e);
        }
        return Optional.absent();
    }

}
