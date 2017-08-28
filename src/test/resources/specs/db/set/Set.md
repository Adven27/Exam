# Вставка в таблицу: `<e:db-set mode="<optional>" table="..." cols="..."/>`

<div>
    <e:example name="Добавление записи (INSERT)">
        <e:given>
            Допустим запись <b c:set="#name">Bob</b>
                            <b c:set="#age">50</b>
                            <b c:set="#bd">10.10.2000</b>
            <span c:assertTrue="addRecord(#name, #age, #bd)">добавлена</span> в таблицу PERSON
            <e:db-show table="PERSON"/>
        </e:given>
        <e:then log="true">
            <e:db-set mode="add" caption="Добавляемые записи" table="PERSON" cols="NAME, AGE">
                <row>Andrew,30</row>
                <row>Carl,20</row>
            </e:db-set>
            <e:db-show caption="Новое содержимое таблицы" table="PERSON"/>
        </e:then>
    </e:example>
    <e:example name="Вставка с удалением предыдущих записей (CLEAN INSERT)">
        <e:given>
            Допустим запись <b c:set="#name">Bob</b>
                            <b c:set="#age">50</b>
                            <b c:set="#bd">10.10.2000</b>
            <span c:assertTrue="addRecord(#name, #age, #bd)">добавлена</span> в таблицу PERSON
            <e:db-show table="PERSON"/>
        </e:given>
        <e:then log="true">
            <e:db-set caption="Добавляемые записи" table="PERSON" cols="NAME, AGE, BIRTHDAY">
                <row>' Andrew ', 30 , ${exam.date(10.10.1990)}</row>
                <row>Carl      , 20 , ${exam.date(01.02.1980)}</row>
            </e:db-set>
            <e:db-show caption="Новое содержимое таблицы" table="PERSON"/>
        </e:then>
    </e:example>
    <e:example name="Значения по умолчанию">
        <e:given>
            Дана пустая таблица:
            <e:db-set table="PERSON"/>
            <e:db-show table="PERSON"/>
        </e:given>
        <e:then log="true">
            <e:db-set caption="Добавляемые записи" table="PERSON" cols="NAME, AGE=20, BIRTHDAY=${exam.yesterday}">
                <row>Andrew</row>
                <row>Carl</row>
            </e:db-set>
            <e:db-show caption="Новое содержимое таблицы" table="PERSON"/>
        </e:then>
    </e:example>
</div>