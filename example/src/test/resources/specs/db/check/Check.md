# Check table
## `<e:db-check table="..." cols="..."/>`

<div>
    <e:summary/>
    <e:given>
        <e:db-set table="PERSON" cols="NAME, AGE, ID=1..10">
            <row>Andrew,30</row>
            <row>Carl,20</row>
        </e:db-set>
    </e:given>
    <e:example name="Happy-path">
        <e:then print="true">
            <e:db-check table="PERSON" cols="NAME, AGE">
                <row>Carl,20</row>
                <row>Andrew,30</row>
            </e:db-check>
        </e:then>
    </e:example>
    <e:example name="Surplus" status="ExpectedToFail">
        <e:then print="true">
            <e:db-check table="PERSON" cols="NAME, AGE">
                <row>Carl,20</row>
            </e:db-check>
        </e:then>
    </e:example>
    <e:example name="Missing" status="ExpectedToFail">
        <e:then print="true">
            <e:db-check table="PERSON" cols="NAME, AGE">
                <row>Carl,20</row>
                <row>Andrew,30</row>
                <row>Missing Record,69</row>
            </e:db-check>
        </e:then>
    </e:example>
    <e:example name="Wrong fields" status="ExpectedToFail">
        <e:then print="true">
            <e:db-check table="PERSON" cols="NAME, AGE">
                <row>Carl,30</row>
                <row>Not Andrew,30</row>
            </e:db-check>
        </e:then>
    </e:example>
    <e:example name="Partial check">
        <e:given>
            Set <var>#someVar</var> = <code c:set="#someVar">3</code>
        </e:given>
        <e:then print="true">
            <e:db-check table="PERSON" cols="NAME, AGE" ignoreRowsBefore="2" ignoreRowsAfter="{{someVar}}">
                <row>Will be ignored</row>
                <row>Andrew,30</row>
                <row>Carl,20</row>
                <row>Will be ignored</row>
            </e:db-check>
        </e:then>
    </e:example>
    <e:example name="Subset check">
        <e:then print="true">
            <e:db-check table="PERSON" cols="NAME, AGE" where="NAME='Andrew'">
                <row>Andrew,30</row>
            </e:db-check>
        </e:then>
    </e:example>
    <e:example name="Check empty - fail" status="ExpectedToFail">
        <e:given>
            <e:db-set table="PERSON"/>
        </e:given>
        <e:then print="true">
            <e:db-check table="PERSON" cols="NAME, AGE" where="NAME='Andrew'">
                <row>Andrew, 30</row>
            </e:db-check>
        </e:then>
    </e:example>
    <e:example name="Check empty - success">
        <e:given>
            <e:db-set table="PERSON"/>
        </e:given>
        <e:then print="true">
            <e:db-check table="PERSON"/>
        </e:then>
    </e:example>
</div>