# Table setting
## `<e:db-set mode="<optional>" table="..." cols="..."/>`

<div>
    <e:summary/>
    <e:example name="INSERT">
        <e:given>
            Given record <b c:set="#name">Bob</b>
                         <b c:set="#age">50</b>
                         <b c:set="#bd">10.10.2000</b>
            <span c:assertTrue="addRecord(#name, #age, #bd)">present</span> in PERSON
            <e:db-show table="PERSON"/>
        </e:given>
        <e:then print="true">
            <e:db-set mode="add" caption="Records to add" table="PERSON" cols="NAME, AGE">
                <row>Andrew,30</row>
                <row>Carl,20</row>
            </e:db-set>
            <e:db-check caption="New table content" table="PERSON" cols="NAME, AGE">
                <row>${#name}, ${#age}</row>
                <row>Andrew  , 30     </row>
                <row>Carl    , 20     </row>
            </e:db-check>
        </e:then>
    </e:example>
    <e:example name="CLEAN INSERT">
        <e:given>
            Given record <b c:set="#name">Bob</b>
                            <b c:set="#age">50</b>
                            <b c:set="#bd">10.10.2000</b>
            <span c:assertTrue="addRecord(#name, #age, #bd)">present</span> in PERSON
            <e:db-show table="PERSON"/>
        </e:given>
        <e:then print="true">
            <e:db-set caption="Records to add" table="PERSON" cols="NAME, AGE, BIRTHDAY">
                <row>' Andrew ', 30 , ${exam.date(10.10.1990)}</row>
                <row>Carl      , 20 , ${exam.date(01.02.1980)}</row>
            </e:db-set>
            <e:db-check caption="New content" table="PERSON" cols="NAME, AGE, BIRTHDAY">
                <row>' Andrew ', 30, ${exam.date(10.10.1990)}</row>
                <row>Carl, 20, ${exam.date(01.02.1980)}</row>
            </e:db-check>
        </e:then>
    </e:example>
    <e:example name="Default values">
        <e:given>
            <e:db-set caption="Given empty table" table="PERSON"/>
        </e:given>
        <e:then print="true">
            <e:db-set caption="Records to add" table="PERSON" cols="NAME, AGE=20, BIRTHDAY=${exam.yesterday}">
                <row>Andrew</row>
                <row>Carl</row>
            </e:db-set>
            <e:db-show caption="New table content" table="PERSON"/>
        </e:then>
    </e:example>
    <e:example name="Ranges">
        <e:given>
            <e:db-set caption="Given empty table" table="PERSON"/>
        </e:given>
        <e:then print="true">
            <e:db-set caption="Records to add" table="PERSON" cols="*AGE=2..4, *IQ=4..2, BIRTHDAY=${exam.yesterday}, NAME">
                <row>age 2 iq 4</row>
                <row>age 3 iq 3</row>
                <row>age 4 iq 2</row>
                <row>again 2 4</row>
                <row>again 3 3</row>
            </e:db-set>
            <e:db-check caption="New table content" table="PERSON" cols="*AGE=2..4, NAME, *IQ=4..2">
                <row>age 2 iq 4</row>
                <row>age 3 iq 3</row>
                <row>age 4 iq 2</row>
                <row>again 2 4</row>
                <row>again 3 3</row>
            </e:db-check>
        </e:then>
    </e:example>
    <e:example name="Custom cell values separator">
        <e:given>
            <e:db-set caption="Given empty table" table="PERSON"/>
        </e:given>
        <e:then print="true">
            <e:db-set separator="|" table="PERSON" cols="NAME, AGE">
                <row>If you need commas , inside value, use custom separator -> | 20</row>
            </e:db-set>
            <e:db-check separator="|" table="PERSON" cols="NAME, AGE">
                <row>If you need commas , inside value, use custom separator -> | 20</row>
            </e:db-check>
        </e:then>
    </e:example>
</div>