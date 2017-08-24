# Вывод содержимого таблицы: `<e:db-show table="..." cols="<optional>"/>`

### [**Пустая таблица**](-)
Результат вывода 
   
    <e:db-show table="PERSON" cols="NAME"/>
    <e:db-show table="PERSON"/>

<div>
    <e:then>
        <e:db-show table="PERSON" cols="NAME"/>
        <e:db-show table="PERSON"/>
    </e:then>
</div>

### [**Не пустая таблица**](-)
Допустим запись *[Bob](- "#name") [50](- "#age") [10.10.2000](- "#bd")* [добавлена](- "c:assert-true=addRecord(#name, #age, #bd)") в таблицу PERSON

Результат вывода 
   
    <e:db-show table="PERSON" caption="Caption" cols="NAME, *AGE, **BIRTHDAY"/>

<div>
    <e:then>
        <e:db-show table="PERSON" caption="Caption" cols="NAME, *AGE, **BIRTHDAY"/>
    </e:then>
</div>

### [**Фильтрация не пустой таблицы**](-)
Допустим в таблице PERSON находятся следующие записи:

<div>
    <e:given>
        <e:db-set table="PERSON" cols="NAME, AGE">
            <row>Bob,45</row>
            <row>Waldo,45</row>
            <row>Coby,50</row>
        </e:db-set>
    </e:given>
</div>

Результат вывода 
   
    <e:db-show table="PERSON" caption="Caption" cols="NAME, *AGE" where="AGE=45;NAME=Waldo"/>

<div>
    <e:then>
        <e:db-show table="PERSON" caption="Caption" cols="NAME, *AGE" where="AGE=45;NAME=Waldo"/>
    </e:then>
</div>