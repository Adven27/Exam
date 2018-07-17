# Check saved entity in key-value db

<div print="true">
    <e:summary/>
    <e:given>
        Having entity<br/>
        In cache: <pre><span c:set="#cacheName">test.cache</span></pre><br/>
        with key: <pre><span c:set="#key">shortKey</span></pre><br/>
        with value: <pre><span c:set="#value">living in db</span></pre>
        <span c:execute="insertValue(#cacheName, #key, #value)" />
    </e:given>
    <e:example name="Check that db contains specified entity">
        <e:then>
            <e:db-kv-check cache="test.cache">
                <key>shortKey</key>
                <value>living in db</value>
            </e:db-kv-check>
        </e:then>
    </e:example>
    <e:example name="Check that db doesn't contain entity with such value" status="ExpectedToFail">
        <e:then>
            <e:db-kv-check cache="test.cache">
                <key>shortKey</key>
                <value>not living in db</value>
            </e:db-kv-check>
        </e:then>
    </e:example>
    <e:example name="Check that db doesn't contain entity with such key and value" status="ExpectedToFail">
        <e:then>
            <e:db-kv-check cache="test.cache">
                <key>another</key>
                <value>living in db</value>
            </e:db-kv-check>
        </e:then>
    </e:example>
    <e:example name="Check that db doesn't contain value for specified key">
        <e:then>
            <e:db-kv-check cache="test.cache">
                <key>another_one</key>
            </e:db-kv-check>
        </e:then>
    </e:example>
</div>    