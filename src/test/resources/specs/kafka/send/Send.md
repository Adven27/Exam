### Send message to kafka

<div print="true">
    <e:summary/>
    <e:example name="Send message to kafka">
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
            <span c:assertTrue="hasReceivedEvent()">Successfuly received event</span>
        </e:then>
    </e:example>
    <e:example name="Send message with headers to kafka">
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
            <span c:assertTrue="hasReceivedEvent()">Successfuly received event</span>
        </e:then>
    </e:example>
</div>    