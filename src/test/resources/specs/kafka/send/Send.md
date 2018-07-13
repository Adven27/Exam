### Send message to kafka

<div print="true">
    <e:summary/>
    <e:example name="Send simple message to kafka">
        <e:when>
            <e:event-send topicName="test.produce.topic" key="messageKey">
                <value>
                {
                    "name": "happy little name",
                    "number": 12
                }
                </value>
            </e:event-send>
        </e:when>
        <e:then>
            <span c:assertTrue="hasReceivedSimpleEvent()">Successfuly received event</span>
        </e:then>
    </e:example>
    <e:example name="Send protobuf message to kafka">
        <e:when>
            <e:event-send topicName="test.produce.topic" key="messageKey">
                <value>
                    <protobuf class="com.adven.concordion.extensions.exam.kafka.protobuf.TestEntity$Entity">
                    {
                        "name": "happy little name",
                        "number": 12
                    }
                    </protobuf>
                </value>
            </e:event-send>
        </e:when>
        <e:then>
            <span c:assertTrue="hasReceivedProtobufEvent()">Successfuly received event</span>
        </e:then>
    </e:example>
    <e:example name="Send protobuf message with headers to kafka">
        <e:when>
            <e:event-send topicName="test.produce.topic" key="messageKey">
                <headers>
                    <replyToTopic>test.reply.topic</replyToTopic>
                    <correlationId>123</correlationId>
                </headers>
                <value>
                    <protobuf class="com.adven.concordion.extensions.exam.kafka.protobuf.TestEntity$Entity">
                    {
                        "name": "happy little name",
                        "number": 12
                    }
                    </protobuf>
                </value>
            </e:event-send>
        </e:when>
        <e:then>
            <span c:assertTrue="hasReceivedProtobufEventWithHeaders()">Successfuly received event with correct headers</span>
        </e:then>
    </e:example>
    <e:example name="Send empty event">
        <e:when>
            <e:event-send topicName="test.produce.topic"/>
        </e:when>
        <e:then>
            <span c:assertTrue="hasReceivedEventWithNoMessage()">Successfuly received empty event</span>
        </e:then>
    </e:example>
</div>    