### Send message to kafka

<div print="true">
    <e:summary/>
    <e:example name="Kafka test send">
        <e:when>
            <e:event-send topicName="test.produce.topic" key="messageKey">
                <protobuf class="com.adven.concordion.extensions.exam.kafka.protobuf.TestEntity$Entity">
                {
                    "name": "happy little name",
                    "number": 12
                }
                </protobuf>
            </e:event-send>
        </e:when>
        <e:then>
            <span c:assertTrue="hasReceivedEvent()">Successfuly received event</span>
        </e:then>
    </e:example>
</div>    