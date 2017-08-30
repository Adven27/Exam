# Проверка содержимого директории: `<e:fl-check dir="dir"/>`

***!!! Пока поддерживается только сравнение XML !!!*** 

<div>
    <e:summary/>
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
    <e:example name="Успешный сценарий">
        <e:then print="true">
            <e:fl-check dir="dir">
                <file name="empty_file"/>
                <file name="not_empty_file">
                    <data>some file content</data>
                </file>
            </e:fl-check>
        </e:then>
    </e:example>
    <e:example name="Лишний файл" status="ExpectedToFail">
        <e:then print="true">
            <e:fl-check dir="dir">
                <file name="not_empty_file">
                    <data>some file content</data>
                </file>
            </e:fl-check>
        </e:then>
    </e:example>
    <e:example name="Недостающий файл" status="ExpectedToFail">
        <e:then print="true">
            <e:fl-check dir="dir">
                <file name="empty_file"/>
                <file name="missing_file"/>
                <file name="not_empty_file">
                    <data>some file content</data>
                </file>
            </e:fl-check>
        </e:then>
    </e:example>
    <e:example name="Не совпал контент" status="ExpectedToFail">
        <e:then print="true">
            <e:fl-check dir="dir">
                <file name="empty_file"/>
                <file name="not_empty_file">
                    <data>another content was expected</data>
                </file>
            </e:fl-check>
        </e:then>
    </e:example>
    <e:example name="Все предыдущие проверки вместе" status="ExpectedToFail">
        <e:then print="true">
            <e:fl-check dir="dir">
                <file name="missing_file"/>
                <file name="not_empty_file">
                    <data>another content was expected</data>
                </file>
            </e:fl-check>
        </e:then>
    </e:example>
</div>