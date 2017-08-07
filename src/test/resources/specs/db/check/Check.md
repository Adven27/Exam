#Проверка содержимого таблицы: `<e:db-check table="..." cols="..."/>`

Имеется таблица

<div>
    <e:given>
        <e:db-set table="PERSON" cols="NAME, AGE">
            <row>Andrew,30</row>
            <row>Carl,20</row>
        </e:db-set>
    </e:given>
</div>

### [**Успешный сценарий (порядок записей не важен)**](-)

Результат работы 

    <e:db-check table="PERSON" cols="NAME, AGE">
        <row>Carl,20</row>
        <row>Andrew,30</row>
    </e:db-check>

<div>
    <e:then>
        <e:db-check table="PERSON" cols="NAME, AGE">
            <row>Carl,20</row>
            <row>Andrew,30</row>
        </e:db-check>
    </e:then>
</div>

### [**Лишняя запись**](- "surplus c:status=ExpectedToFail")

    <e:db-check table="PERSON" cols="NAME, AGE">
        <row>Carl,20</row>
    </e:db-check>

<div>
    <e:then>
        <e:db-check table="PERSON" cols="NAME, AGE">
            <row>Carl,20</row>
        </e:db-check>
    </e:then>
</div>

### [**Недостающая запись**](- "missing c:status=ExpectedToFail")

    <e:db-check table="PERSON" cols="NAME, AGE">
        <row>Carl,20</row>
        <row>Andrew,30</row>
        <row>Missing Record,69</row>
    </e:db-check>

<div>
    <e:then>
        <e:db-check table="PERSON" cols="NAME, AGE">
            <row>Carl,20</row>
            <row>Andrew,30</row>
            <row>Missing Record,69</row>
        </e:db-check>
    </e:then>
</div>

### [**Не совпали поля**](- "wrong fields c:status=ExpectedToFail")

    <e:db-check table="PERSON" cols="NAME, AGE">
        <row>Carl,30</row>
        <row>Not Andrew,20</row>
    </e:db-check>

<div>
    <e:then>
        <e:db-check table="PERSON" cols="NAME, AGE">
            <row>Carl,30</row>
            <row>Not Andrew,30</row>
        </e:db-check>
    </e:then>
</div>