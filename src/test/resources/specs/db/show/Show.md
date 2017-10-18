# Show table content
## `<e:db-show table="..." cols="<optional>"/>`

<div>
    <e:summary/>
    <e:example name="Empty table">
        <e:then print="true">
            <e:db-show table="PERSON" cols="NAME"/>
            <e:db-show table="PERSON"/>
        </e:then>
    </e:example>
    <e:example name="Not empty table">
        <e:given>
            Given record <b c:set="#name">Bob</b>
                         <b c:set="#age">50</b>
                         <b c:set="#bd">10.10.2000</b>
            <span c:assertTrue="addRecord(#name, #age, #bd)">present</span> in PERSON
        </e:given>
        <e:then print="true">
            <e:db-show table="PERSON" cols="NAME, *AGE, **BIRTHDAY"/>
        </e:then>
    </e:example>
    <e:example name="Filtered table">
        <e:given>
            <e:db-set table="PERSON" cols="NAME, AGE">
                <row>Bob,45</row>
                <row>Waldo,45</row>
                <row>Coby,50</row>
            </e:db-set>
        </e:given>
        <e:then print="true">
            <e:db-show table="PERSON" caption="Filtering for equality" cols="NAME, *AGE" where="AGE=45;NAME=Waldo"/>
            <e:db-show table="PERSON" caption="Filtering on an occurrence" cols="NAME, *AGE" where="AGE=%5"/>
        </e:then>
    </e:example>
</div>