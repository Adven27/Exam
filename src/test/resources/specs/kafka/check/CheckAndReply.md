### Check event and reply

<div print="true">
    <e:summary/>
    <e:example name="Event must be received and then a success event must redirected to specified topic">
        <e:given>        
            <e:event-check topicName="test.consume.topic">
                <expected>
                {
                    "name": "Make something good",
                    "number": 7
                }
                </expected>
                <reply topicName="test.success.topic">
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
            <e:set var="successTopic" value="test.success.topic"/>
            <span c:assertTrue="isCorrectResult(#successTopic)">Received event is equal to expected and response is equal to succes reply</span>
        </e:when>
    </e:example>
    <e:example name="Event must be received and then a success event must be send back to topic specified in headers">
        <e:given>        
            <e:event-check topicName="test.consume.topic">
                <expected>
                {
                    "name": "Make something good",
                    "number": 7
                }
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