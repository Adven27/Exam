### Send message to kafka

<div>
    <e:summary/>
    <e:example name="kafka test send">
        <e:event-send topicName="test.topic" key="messageKey" protobufClass="com.adven.concordion.extensions.exam.kafka.protobuf.TestEntity$Entity">
        {
            "name": "test name",
            "number": 123
        }
        </e:event-send>
    </e:example>
</div>    