# Вывод содержимого таблицы: `<e:db-show table="..." cols="<optional>"/>`

<div>
<e:example name="Пустая таблица">
    <e:then log="true">
        <e:db-show table="PERSON" cols="NAME"/>
        <e:db-show table="PERSON"/>
    </e:then>
</e:example>
</div>

Допустим запись *[Bob](- "#name") [50](- "#age") [10.10.2000](- "#bd")* [добавлена](- "c:assert-true=addRecord(#name, #age, #bd)") в таблицу PERSON

<div>
<e:example name="Не пустая таблица">
    <e:then log="true">
        <e:db-show table="PERSON" caption="Caption" cols="NAME, *AGE, **BIRTHDAY"/>
    </e:then>
</e:example>
<e:example name="Фильтрация не пустой таблицы">
    <e:given>
        Допустим в таблице PERSON находятся следующие записи:
        <e:db-set table="PERSON" cols="NAME, AGE">
            <row>Bob,45</row>
            <row>Waldo,45</row>
            <row>Coby,50</row>
        </e:db-set>
    </e:given>
    <e:then log="true">
        <e:db-show table="PERSON" caption="Caption" cols="NAME, *AGE" where="AGE=45;NAME=Waldo"/>
    </e:then>
</e:example>
</div>