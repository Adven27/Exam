# Вывод содержимого директории: `<e:fl-show dir="dir"/>`

Например, имеется директория **[ ](- "c:echo=dir")**

### [**Для пустой директории**](-)
Резудьтат вывода `<e:fl-show dir="dir"/>`

<div>
    <e:then>
        <e:fl-show dir="dir"/>
    </e:then>
</div>

### [**Для не пустой директории**](-)
Допустим файл **[some_file](- "#name")** [добавлен в директорию.] (- "c:assert-true=addFile(#name)")

Резудьтат вывода `<e:fl-show dir="dir"/>`

<div>
    <e:then>
        <e:fl-show dir="dir"/>
    </e:then>
</div>
