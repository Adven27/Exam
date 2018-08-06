# Check saved entity in key-value db (protobuf format)

<div print="true">
    <e:summary/>
    <e:given>
        Having entity<br/>
        In cache: <pre><span c:set="#cacheName">test.cache</span></pre><br/>
        with key: <pre><span c:set="#key">shortKey</span></pre><br/>
        with value in protobuf format: 
        <pre>{ name: <span c:set="#name">living in db</span>, number: <span c:set="#number">48</span> }</pre>
        <span c:execute="insertValue(#cacheName, #key, #name, #number)" />
    </e:given>
    <e:example name="Check that db contains specified entity">
        <e:then>
            <e:db-kv-check cache="test.cache">
                <key>shortKey</key>
                <value>
                    <protobuf class="com.adven.concordion.extensions.exam.utils.protobuf.TestEntity$Entity">
                    {
                        "name": "living in db",
                        "number": 48
                    }
                    </protobuf>
                </value>
            </e:db-kv-check>
        </e:then>
    </e:example>
    <e:example name="Check that db doesn't contain entity with such value" status="ExpectedToFail">
        <e:then>
            <e:db-kv-check cache="test.cache">
                <key>shortKey</key>
                <value>
                    <protobuf class="com.adven.concordion.extensions.exam.utils.protobuf.TestEntity$Entity">
                    {
                        "name": "not living in db",
                        "number": 48
                    }
                    </protobuf>
                </value>
            </e:db-kv-check>
        </e:then>
    </e:example>
    <e:example name="Check that db doesn't contain entity" status="ExpectedToFail">
        <e:then>
            <e:db-kv-check cache="test.cache">
                <key>another</key>
                <value>
                    <protobuf class="com.adven.concordion.extensions.exam.utils.protobuf.TestEntity$Entity">
                    {
                        "name": "not living in db",
                        "number": 48
                    }
                    </protobuf>
                </value>
            </e:db-kv-check>
        </e:then>
    </e:example>
</div>    