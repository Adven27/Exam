package specs.kafka.check;

import com.adven.concordion.extensions.exam.utils.protobuf.TestEntity;
import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.utils.Bytes;
import org.concordion.api.BeforeExample;
import specs.kafka.Kafka;

import static com.adven.concordion.extensions.exam.RandomUtils.anyString;
import static com.adven.concordion.extensions.exam.kafka.EventHeader.CORRELATION_ID;
import static com.adven.concordion.extensions.exam.kafka.EventHeader.REPLY_TOPIC;
import static java.nio.charset.StandardCharsets.UTF_8;

public class CheckAndReply extends Kafka {

    @BeforeExample
    public void setUp() throws Exception {
        final String message = "{\"name\": \"Make something good\", \"number\": 7}";
        final ProducerRecord<String, Bytes> r = new ProducerRecord<>(CONSUME_TOPIC,
            Bytes.wrap(message.getBytes(UTF_8)));
        r.headers().add(REPLY_TOPIC, PRODUCE_TOPIC.getBytes(UTF_8));
        r.headers().add(CORRELATION_ID, anyString().getBytes(UTF_8));
        produceEvent(r);
    }

    public boolean isCorrectResult() throws InvalidProtocolBufferException {
        return isCorrectResult(PRODUCE_TOPIC);
    }

    public boolean isCorrectResult(final String topic) throws InvalidProtocolBufferException {
        final ConsumerRecord<String, Bytes> record = consumeSingleEvent(topic, 1000L);
        final TestEntity.Entity entity = TestEntity.Entity.parseFrom(record.value().get());
        final TestEntity.Entity expected = TestEntity.Entity.newBuilder()
            .setName("OK")
            .setNumber(42)
            .build();
        return entity.equals(expected);
    }

}
