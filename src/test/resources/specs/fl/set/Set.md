# Установка содержимого директории: `<e:fl-set dir="dir"/>`

Имеется директория **[ ](- "c:echo=dir")**

### [**Предыдущее содержимое директории будет удалено**](-)
Допустим файл **[some_file](- "#name")** [добавлен в директорию.] (- "c:assert-true=addFile(#name)")

<div>
    <e:given>
        <e:fl-show dir="dir"/>
    </e:given>
</div>

Результат работы 

    <e:fl-set dir="dir">
        <file name="empty_file"/>
        <file name="not_empty_file">
            содержимое файла
        </file>
        <file name="not_empty_file2">${exam.now} or formated ${exam.now:dd.MM.yyyy'T'HH:mm:ss}</file>
    </e:fl-set>

<div>
    <e:then>
        <e:fl-set dir="dir">
            <file name="empty_file"/>
            <file name="not_empty_file">
                содержимое файла
            </file>
            <file name="not_empty_file2">${exam.now} or formated ${exam.now:dd.MM.yyyy'T'HH:mm:ss}</file>
        </e:fl-set>
    </e:then>
</div>

### [**Очистка директории с помощью пустой команды**](-)
Очистим директорию заполененную предыдущим примером

<div>
    <e:given>
        <e:fl-show dir="dir"/>
    </e:given>
</div>

Результат работы 

    <e:fl-set dir="dir"/>

<div>
   <e:then>
        <e:fl-set dir="dir"/>
    </e:then>
</div>