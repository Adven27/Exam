#Проверка содержимого таблицы: `<e:db-check table="..." cols="..."/>`

<div>
    <e:given>
        <e:db-set table="PERSON" cols="NAME, AGE">
            <row>Andrew,30</row>
            <row>Carl,20</row>
        </e:db-set>
    </e:given>
    <e:example name="Успешный сценарий (порядок записей не важен)">
        <e:then log="true">
            <e:db-check table="PERSON" cols="NAME, AGE">
                <row>Carl,20</row>
                <row>Andrew,30</row>
            </e:db-check>
        </e:then>
    </e:example>
    <e:example name="Лишняя запись" status="ExpectedToFail">
        <e:then log="true">
            <e:db-check table="PERSON" cols="NAME, AGE">
                <row>Carl,20</row>
            </e:db-check>
        </e:then>
    </e:example>
    <e:example name="Недостающая запись" status="ExpectedToFail">
        <e:then log="true">
            <e:db-check table="PERSON" cols="NAME, AGE">
                <row>Carl,20</row>
                <row>Andrew,30</row>
                <row>Missing Record,69</row>
            </e:db-check>
        </e:then>
    </e:example>
    <e:example name="Не совпали поля" status="ExpectedToFail">
        <e:then log="true">
            <e:db-check table="PERSON" cols="NAME, AGE">
                <row>Carl,30</row>
                <row>Not Andrew,30</row>
            </e:db-check>
        </e:then>
    </e:example>
    <e:example name="Partial check">
        <e:given>
            Set #someVar = <code c:set="#someVar">3</code>
        </e:given>
        <e:then log="true">
            <e:db-check table="PERSON" cols="NAME, AGE" ignoreRowsBefore="2" ignoreRowsAfter="${var.someVar}">
                <row>Will be, ignored</row>
                <row>Andrew,30</row>
                <row>Carl,20</row>
                <row>Will be, ignored</row>
            </e:db-check>
        </e:then>
    </e:example>
</div>