### Send message to kafka

<div print="true">
    <e:summary/>
    <e:example name="Kafka test send">
        <e:when>
            <e:event-send topicName="test.topic" key="messageKey" protobufClass="com.adven.concordion.extensions.exam.kafka.protobuf.TestEntity$Entity">
            {
                "name": "test name",
                "number": 123
            }
            </e:event-send>
        </e:when>
    </e:example>
</div>    