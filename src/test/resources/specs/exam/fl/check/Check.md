# Проверка содержимого директории: `<e:fl-check dir="dir"/>`

***!!! Пока поддерживается только сравнение XML !!!*** 

Имеется директория

<div>
    <e:given>
        <e:fl-set dir="dir">
            <file name="empty_file"/>
            <file name="not_empty_file">
            <![CDATA[
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <data>some file content</data>
            ]]>
            </file>
        </e:fl-set>
    </e:given>
</div>

### [**Успешный сценарий**](-)

Результат работы 

    <e:fl-check dir="dir">
        <file name="empty_file"/>
        <file name="not_empty_file">
            <data>some file content</data>
        </file>
    </e:fl-check>

<div>
    <e:then>
        <e:fl-check dir="dir">
            <file name="empty_file"/>
            <file name="not_empty_file">
                <data>some file content</data>
            </file>
        </e:fl-check>
    </e:then>
</div>

### [**Лишний файл**](- "surplus c:status=ExpectedToFail")

    <e:fl-check dir="dir">
        <file name="not_empty_file">
            <data>some file content</data>
        </file>
    </e:fl-check>

<div>
    <e:then>
        <e:fl-check dir="dir">
            <file name="not_empty_file">
                <data>some file content</data>
            </file>
        </e:fl-check>
    </e:then>
</div>

### [**Недостающий файл**](- "missing c:status=ExpectedToFail")

    <e:fl-check dir="dir">
        <file name="empty_file"/>
        <file name="missing_file"/>
        <file name="not_empty_file">
            <data>some file content</data>
        </file>
    </e:fl-check>

<div>
    <e:then>
        <e:fl-check dir="dir">
            <file name="empty_file"/>
            <file name="missing_file"/>
            <file name="not_empty_file">
                <data>some file content</data>
            </file>
        </e:fl-check>
    </e:then>
</div>

### [**Не совпал контент**](- "wrong content c:status=ExpectedToFail")

    <e:fl-check dir="dir">
        <file name="empty_file"/>
        <file name="not_empty_file">
            <data>another content was expected</data>
        </file>
    </e:fl-check>

<div>
    <e:then>
        <e:fl-check dir="dir">
            <file name="empty_file"/>
            <file name="not_empty_file">
                <data>another content was expected</data>
            </file>
        </e:fl-check>
    </e:then>
</div>

### [**Все предыдущие проверки вместе**](- "all c:status=ExpectedToFail")

    <e:fl-check dir="dir">
        <file name="missing_file"/>
        <file name="not_empty_file">
            <data>another content was expected</data>
        </file>
    </e:fl-check>

<div>
    <e:then>
        <e:fl-check dir="dir">
            <file name="missing_file"/>
            <file name="not_empty_file">
                <data>another content was expected</data>
            </file>
        </e:fl-check>
    </e:then>
</div>