### Check event and reply
<div>
    <e:summary/>
    <e:example name="Event must be received and then a success event must be send back">
        <e:given>        
            <e:event-check>
                <expected topicName="test.consume.topic" protobufClass="com.adven.concordion.extensions.exam.kafka.protobuf.TestEntity$Entity">
                {
                    "name": "Make something good",
                    "number": 7
                }
                </expected>
                <reply protobufClass="com.adven.concordion.extensions.exam.kafka.protobuf.TestEntity$Entity">
                      <success>
                      {
                        "name": "OK",
                        "number": 42
                      }
                      </success>
                      <fail>
                      {
                        "name": "FAIL",
                        "number": 13
                      }
                      </fail>
                </reply>
            </e:event-check>
        </e:given>
        <e:when>
            <span c:assertTrue="isCorrectResult()">Received event was success</span>
        </e:when>
    </e:example>
</div>