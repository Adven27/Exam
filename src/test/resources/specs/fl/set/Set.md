# Установка содержимого директории: `<e:fl-set dir="dir"/>`

Имеется директория **[ ](- "c:echo=dir")**

<div>
    <e:summary/>
    <e:example name="Предыдущее содержимое директории будет удалено">
        <e:given>
            Допустим файл <code c:set="#name">some_file</code> <span c:assertTrue="addFile(#name)">добавлен в директорию.</span>
            <e:fl-show dir="dir"/>
        </e:given>
        <e:then print="true">
            <e:fl-set dir="dir">
                <file name="empty_file"/>
                <file name="content_from_external_file" from="data/test.xml"/>
                <file name="inline_content">${exam.now} or formated ${exam.now:dd.MM.yyyy'T'HH:mm:ss}</file>
            </e:fl-set>
        </e:then>
    </e:example>
    <e:example name="Очистка директории с помощью пустой команды">
        <e:given>
            Очистим директорию заполененную предыдущим примером
            <e:fl-show dir="dir"/>
        </e:given>
        <e:then print="true">
            <e:fl-set dir="dir"/>
        </e:then>
    </e:example>
</div>