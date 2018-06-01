# Save entity in key-value db

<div print="true">
    <e:summary/>
    <e:example name="Check that db contains specified entity">
        <e:given>
            <e:db-kv-set cache="test.cache">
                <key>shortKey</key>
                <value>someValue</value>
            </e:db-kv-set>
        </e:given>
        <e:then>
            <span c:set="#expected">someValue</span>
            <span c:assertTrue="check(#expected)">is equal to saved entity</span>
        </e:then>
    </e:example>
</div>    