# Table setting
## `<e:db-set mode="<optional>" table="..." cols="..."/>`

<div>
    <e:summary/>
    <e:example name="DbUnit operations support">
        <e:given>
            Given record <b c:set="#id">1</b>
                         <b c:set="#name">Bob</b>
                         <b c:set="#age">50</b>
                         <b c:set="#bd">10.10.2000</b>
            <span c:assertTrue="addRecord(#id, #name, #age, #bd)">present</span> in PERSON
            <e:db-show table="PERSON"/>
        </e:given>
        <e:when print="true">
            <e:db-set operation="insert" caption="Append records" table="PERSON" cols="ID=10..20, NAME, AGE">
                <row>Andrew,30</row>
                <row>Carl,20</row>
            </e:db-set>
        </e:when>
        <e:then>            
            <e:db-check caption="Records was appended" table="PERSON" cols="NAME, AGE">
                <row>${#name}, ${#age}</row>
                <row>Andrew  , 30     </row>
                <row>Carl    , 20     </row>
            </e:db-check>
        </e:then>
        <e:when print="true">
            <e:db-set operation="update" caption="Update by primary key" table="PERSON" cols="ID=1, NAME, AGE">
                <row>not Bob, 500</row>
            </e:db-set>
        </e:when>
        <e:then>            
            <e:db-check caption="Record was updated" table="PERSON" cols="NAME, AGE">
                <row>not Bob , 500 </row>
                <row>Andrew  , 30  </row>
                <row>Carl    , 20  </row>
            </e:db-check>
        </e:then>
        <e:when print="true">
            <e:db-set caption="Clean insert by default" table="PERSON" cols="ID=10..20, NAME, AGE, BIRTHDAY">
                <row>' Andrew ', 30 , ${exam.date(10.10.1990)}</row>
                <row>Carl      , 20 , ${exam.date(01.02.1980)}</row>
            </e:db-set>
        </e:when>
        <e:then>
            <e:db-check caption="Table was cleaned and records was inserted" table="PERSON" cols="NAME, AGE, BIRTHDAY">
                <row>' Andrew ', 30, ${exam.date(10.10.1990)}</row>
                <row>Carl      , 20, ${exam.date(01.02.1980)}</row>
            </e:db-check>
        </e:then>
    </e:example>   
    <e:example name="Default values">
        <e:given>
            <e:db-set caption="Given empty table" table="PERSON"/>
        </e:given>
        <e:then print="true">
            <e:db-set caption="Records to add" table="PERSON" cols="NAME, AGE=20, BIRTHDAY=${exam.yesterday}, ID=1..10">
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
            <e:db-set caption="Records to add" table="PERSON" cols="*AGE=2..4, *IQ=4..2, BIRTHDAY=${exam.yesterday}, NAME, ID=1..10">
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
            <e:db-set separator="|" table="PERSON" cols="NAME, AGE, ID=1">
                <row>If you need commas , inside value, use custom separator -> | 20</row>
            </e:db-set>
            <e:db-check separator="|" table="PERSON" cols="NAME, AGE">
                <row>If you need commas , inside value, use custom separator -> | 20</row>
            </e:db-check>
        </e:then>
    </e:example>
</div>