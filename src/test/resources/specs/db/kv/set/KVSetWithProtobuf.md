# Save entity in key-value db (protobuf format)

<div print="true">
    <e:summary/>
    <e:example name="Check that db contains specified entity">
        <e:given>
            <e:db-kv-set cache="test.cache">
                <key>shortKey</key>
                <value>
                    <protobuf class="com.adven.concordion.extensions.exam.kafka.protobuf.TestEntity$Entity">
                    {
                        "name": "living in db",
                        "number": 48
                    }
                    </protobuf>
                </value>
            </e:db-kv-set>
        </e:given>
        <e:then>
            <span c:assertTrue="check()">Successfuly saved entity</span>
        </e:then>
    </e:example>
</div>    