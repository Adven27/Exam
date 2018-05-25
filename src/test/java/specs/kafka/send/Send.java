package specs.kafka.send;

import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.utils.Bytes;
import specs.kafka.Kafka;

import static com.adven.concordion.extensions.exam.kafka.protobuf.TestEntity.Entity;

/**
 * @author Ruslan Ustits
 */
public class Send extends Kafka {

    public boolean hasReceivedEvent() throws InvalidProtocolBufferException {
        final ConsumerRecord<String, Bytes> record = consumeSingleEvent();
        final Entity entity = Entity.parseFrom(record.value().get());
        return entity.getName().equals("happy little name") && entity.getNumber() == 12;

    }

}
