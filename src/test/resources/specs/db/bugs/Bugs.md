# Bugs

<div>
    <e:summary/>
    <e:example name="Every db command has independent(from other commands) column order">
        <e:given>
            <e:db-set table="PERSON" cols="*NAME, *AGE, BIRTHDAY={{date '10.10.2010' format='dd.MM.yyyy'}}, ID=1..10">
                <row>Bob,20</row>
                <row>Ed,40</row>
            </e:db-set>
        </e:given>
        <e:when>
            <e:db-set operation="insert" table="PERSON" cols="BIRTHDAY, NAME, AGE=10, ID=11..20">
                <row>{{date '10.10.2010' format="dd.MM.yyyy"}},Adam</row>
            </e:db-set>
        </e:when>
        <e:then>
            <e:db-check table="PERSON" cols="NAME, AGE, BIRTHDAY">
                <row>Adam, 10, {{date '10.10.2010' format="dd.MM.yyyy"}}</row>
                <row>Bob,  20, {{date '10.10.2010' format="dd.MM.yyyy"}}</row>
                <row>Ed,   40, {{date '10.10.2010' format="dd.MM.yyyy"}}</row>
            </e:db-check>
        </e:then>
    </e:example>
    <e:example name="Order sign (*) shouldn't influence on how values are set (date(10.10.2010) shouldn't go to NAME column)">
        <e:when>
            <e:db-set table="PERSON" cols="BIRTHDAY, *NAME, AGE=10, ID=1..10">
                <row>{{date '10.10.2010' format="dd.MM.yyyy"}},Adam</row>
            </e:db-set>
        </e:when>
        <e:then>
            <e:db-check table="PERSON" cols="NAME, AGE, BIRTHDAY">
                <row>Adam, 10, {{date '10.10.2010' format="dd.MM.yyyy"}}</row>
            </e:db-check>
        </e:then>
    </e:example>
</div>