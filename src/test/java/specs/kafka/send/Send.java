package specs.kafka.send;

import com.adven.concordion.extensions.exam.kafka.EventHeader;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.utils.Bytes;
import specs.kafka.Kafka;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import static com.adven.concordion.extensions.exam.kafka.protobuf.TestEntity.Entity;

public class Send extends Kafka {

    public boolean hasReceivedSimpleEvent() throws IOException {
        final ConsumerRecord<String, Bytes> record = consumeSingleEvent();
        final String result = new String(record.value().get(), "UTF-8");
        final String expected = "{\"name\": \"happy little name\", \"number\": 12}";
        final ObjectMapper mapper = new ObjectMapper();
        final Map resultValues = mapper.readValue(result, Map.class);
        final Map expectedValue = mapper.readValue(expected, Map.class);
        return resultValues.equals(expectedValue);
    }

    public boolean hasReceivedProtobufEvent() throws InvalidProtocolBufferException {
        final ConsumerRecord<String, Bytes> record = consumeSingleEvent();
        final Entity entity = Entity.parseFrom(record.value().get());
        return entity.getName().equals("happy little name") && entity.getNumber() == 12;
    }

    public boolean hasReceivedProtobufEventWithHeaders() throws InvalidProtocolBufferException, UnsupportedEncodingException {
        final ConsumerRecord<String, Bytes> record = consumeSingleEvent();
        final Entity entity = Entity.parseFrom(record.value().get());
        final String replyToTopic = header(record.headers(), EventHeader.REPLY_TOPIC);
        final String correlationId = header(record.headers(), EventHeader.CORRELATION_ID);
        return entity.getName().equals("happy little name") && entity.getNumber() == 12
            && replyToTopic.equals("test.reply.topic") && correlationId.equals("123");
    }

    private String header(final Headers headers, final String headerName) throws UnsupportedEncodingException {
        final byte[] bytes = headers.headers(headerName).iterator().next().value();
        return new String(bytes, "UTF-8");
    }

}
