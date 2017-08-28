# Bugs

<div>
    <e:example name="Every db command has independent(from other commands) column order">
        <e:given>
            <e:db-set table="PERSON" cols="*NAME, *AGE, BIRTHDAY=${exam.date(10.10.2010)}">
                <row>Bob,20</row>
                <row>Ed,40</row>
            </e:db-set>
        </e:given>
        <e:when>
            <e:db-set mode="add" table="PERSON" cols="BIRTHDAY, NAME, AGE=10">
                <row>${exam.date(10.10.2010)},Adam</row>
            </e:db-set>
        </e:when>
        <e:then>
            <e:db-check table="PERSON" cols="NAME, AGE, BIRTHDAY">
                <row>Adam, 10, ${exam.date(10.10.2010)}</row>
                <row>Bob,  20, ${exam.date(10.10.2010)}</row>
                <row>Ed,   40, ${exam.date(10.10.2010)}</row>
            </e:db-check>
        </e:then>
    </e:example>
    <e:example name="Order sign (*) shouldn't influence on how values are set (date(10.10.2010) shouldn't go to NAME column)">
        <e:when>
            <e:db-set table="PERSON" cols="BIRTHDAY, *NAME, AGE=10">
                <row>${exam.date(10.10.2010)},Adam</row>
            </e:db-set>
        </e:when>
        <e:then>
            <e:db-check table="PERSON" cols="NAME, AGE, BIRTHDAY">
                <row>Adam, 10, ${exam.date(10.10.2010)}</row>
            </e:db-check>
        </e:then>
    </e:example>
</div>