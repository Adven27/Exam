### Check event and reply (protobuf)

<div print="true">
    <e:summary/>
    <e:example name="Event must be received and then a success event must be send back">
        <e:given>        
            <e:event-check topicName="test.consume.topic">
                <expected>
                    <protobuf class="com.adven.concordion.extensions.exam.utils.protobuf.TestEntity$Entity">
                    {
                        "name": "Make something good",
                        "number": 7
                    }
                    </protobuf>
                </expected>
                <reply>
                    <success>
                        <protobuf class="com.adven.concordion.extensions.exam.utils.protobuf.TestEntity$Entity">
                        {
                            "name": "OK",
                            "number": 42
                        }
                        </protobuf>
                    </success>
                    <fail>
                        <protobuf class="com.adven.concordion.extensions.exam.utils.protobuf.TestEntity$Entity">
                        {
                            "name": "FAIL",
                            "number": 13
                        }
                        </protobuf>
                    </fail>
                </reply>
            </e:event-check>
        </e:given>
        <e:when>
            <span c:assertTrue="isCorrectResult()">Received event is equal to expected and response is equal to succes reply</span>
        </e:when>
    </e:example>
</div>