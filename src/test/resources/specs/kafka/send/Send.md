### Send message to kafka

<div print="true">
    <e:summary/>
    <e:example name="Kafka test send">
        <e:when>
            <e:event-send topicName="test.produce.topic" key="messageKey" protobufClass="com.adven.concordion.extensions.exam.kafka.protobuf.TestEntity$Entity">
            {
                "name": "happy little name",
                "number": 12
            }
            </e:event-send>
        </e:when>
        <e:then>
            <span c:assertTrue="hasReceivedEvent()">Successfuly received event</span>
        </e:then>
    </e:example>
</div>    