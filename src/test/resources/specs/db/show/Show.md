# Вывод содержимого таблицы: `<e:db-show table="..." cols="<optional>"/>`

<div>
    <e:summary/>
    <e:example name="Пустая таблица">
        <e:then print="true">
            <e:db-show table="PERSON" cols="NAME"/>
            <e:db-show table="PERSON"/>
        </e:then>
    </e:example>
    <e:example name="Не пустая таблица">
        <e:given>
            Допустим запись <b c:set="#name">Bob</b>
                            <b c:set="#age">50</b>
                            <b c:set="#bd">10.10.2000</b>
            <span c:assertTrue="addRecord(#name, #age, #bd)">добавлена</span> в таблицу PERSON
        </e:given>
        <e:then print="true">
            <e:db-show table="PERSON" cols="NAME, *AGE, **BIRTHDAY"/>
        </e:then>
    </e:example>
    <e:example name="Фильтрация таблицы">
        <e:given>
            <e:db-set caption="Содержимое таблицы PERSON" table="PERSON" cols="NAME, AGE">
                <row>Bob,45</row>
                <row>Waldo,45</row>
                <row>Coby,50</row>
            </e:db-set>
        </e:given>
        <e:then print="true">
            <e:db-show table="PERSON" caption="Caption" cols="NAME, *AGE" where="AGE=45;NAME=Waldo"/>
        </e:then>
    </e:example>
</div>