package specs.kafka.check;

import com.adven.concordion.extensions.exam.utils.protobuf.TestEntity;
import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.utils.Bytes;
import org.concordion.api.BeforeSpecification;
import specs.kafka.Kafka;

import static com.adven.concordion.extensions.exam.RandomUtils.anyString;
import static com.adven.concordion.extensions.exam.kafka.EventHeader.CORRELATION_ID;
import static com.adven.concordion.extensions.exam.kafka.EventHeader.REPLY_TOPIC;

public class CheckAndReply extends Kafka {

    @BeforeSpecification
    public void setUp() throws Exception {
        final String message = "{\"name\": \"Make something good\", \"number\": 7}";
        final ProducerRecord<String, Bytes> r = new ProducerRecord<>(CONSUME_TOPIC,
            Bytes.wrap(message.getBytes("UTF-8")));
        r.headers().add(REPLY_TOPIC, PRODUCE_TOPIC.getBytes("UTF-8"));
        r.headers().add(CORRELATION_ID, anyString().getBytes("UTF-8"));
        produceEvent(r);
    }

    public boolean isCorrectResult() throws InvalidProtocolBufferException {
        final ConsumerRecord<String, Bytes> record = consumeSingleEvent();
        final TestEntity.Entity entity = TestEntity.Entity.parseFrom(record.value().get());
        final TestEntity.Entity expected = TestEntity.Entity.newBuilder()
            .setName("OK")
            .setNumber(42)
            .build();
        return entity.equals(expected);
    }

}
