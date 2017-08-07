# Вставка в таблицу: `<e:db-set mode="<optional>" table="..." cols="..."/>`

### [**Добавление записи (INSERT)**](-)
Допустим запись *[Bob](- "#name") [50](- "#age") [10.10.2000](- "#bd")* [добавлена](- "c:assert-true=addRecord(#name, #age, #bd)") в таблицу PERSON

<div>
    <e:given>
        <e:db-show table="PERSON" cols="NAME, AGE"/>
    </e:given>
</div>

Результат работы 
   
    <div>Добавляемые записи</div>
    <e:db-set mode="add" table="PERSON" cols="NAME, AGE">
        <row>Andrew,30</row>
        <row>Carl,20</row>
    </e:db-set>
    <div>Новое содержимое таблицы</div>
    <e:db-show table="PERSON" cols="NAME, AGE"/>

<div>
    <e:then>
        <div>Добавляемые записи</div>
        <e:db-set mode="add" table="PERSON" cols="NAME, AGE">
            <row>Andrew,30</row>
            <row>Carl,20</row>
        </e:db-set>
        <div>Новое содержимое таблицы</div>
        <e:db-show table="PERSON" cols="NAME, AGE"/>
    </e:then>
</div>

### [**Вставка с удалением предыдущих записей (CLEAN INSERT)**](-)
Допустим запись *[Bob](- "#name") [50](- "#age") [10.10.2000](- "#bd")* [добавлена](- "c:assert-true=addRecord(#name, #age, #bd)") в таблицу PERSON

<div>
    <e:given>
        <e:db-show table="PERSON"/>
    </e:given>
</div>

Результат вывода 
   
    <div>Добавляемые записи</div>
    <e:db-set table="PERSON" cols="NAME, AGE, BIRTHDAY">
        <row>' Andrew ', 30 , ${exam.date(10.10.1990)}</row>
        <row>Carl      , 20 , ${exam.date(01.02.1980)}</row>
    </e:db-set>
    <div>Новое содержимое таблицы</div>
    <e:db-show table="PERSON"/>

<div>
    <e:then>
        <div>Добавляемые записи</div>
        <e:db-set table="PERSON" cols="NAME, AGE, BIRTHDAY">
            <row>' Andrew ', 30 , ${exam.date(10.10.1990)}</row>
            <row>Carl      , 20 , ${exam.date(01.02.1980)}</row>
        </e:db-set>
        <div>Новое содержимое таблицы</div>
        <e:db-show table="PERSON"/>
    </e:then>
</div>

### [**Значения по умолчанию**](-)
<div>
    <e:given>
        Дана пустая таблица:
        <e:db-set table="PERSON"/>
        <e:db-show table="PERSON" cols="NAME, AGE"/>
    </e:given>
</div>

Результат вывода 
   
    <div>Добавляемые записи</div>
    <e:db-set table="PERSON" cols="NAME, AGE=20, BIRTHDAY=${exam.yesterday}">
        <row>Andrew</row>
        <row>Carl</row>
    </e:db-set>
    <div>Новое содержимое таблицы</div>
    <e:db-show table="PERSON"/>

<div>
    <e:then>
        <div>Добавляемые записи</div>
        <e:db-set table="PERSON" cols="NAME, AGE=20, BIRTHDAY=${exam.yesterday}">
            <row>Andrew</row>
            <row>Carl</row>
        </e:db-set>
        <div>Новое содержимое таблицы</div>
        <e:db-show table="PERSON"/>
    </e:then>
</div>