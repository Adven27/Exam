# Вывод содержимого директории: `<e:fl-show dir="dir"/>`

Например, имеется директория **[ ](- "c:echo=dir")**

<div>
    <e:summary/>
    <e:example name="Пустая директория" print="true">
        <e:fl-show dir="dir"/>
    </e:example>
    <e:example name="Не пустая директория">
        <e:given>
            Допустим файл <code c:set="#name">some_file</code> <span c:assertTrue="addFile(#name)">добавлен в директорию.</span>
        </e:given>
        <e:then print="true">
            <e:fl-show dir="dir"/>
        </e:then>
    </e:example>
</div>