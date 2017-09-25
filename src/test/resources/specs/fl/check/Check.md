# Check directory content
## `<e:fl-check dir="dir"/>`

***!!! Only XML comparing are supported !!!***

<div>
    <e:summary/>
    <e:given>
        <e:fl-set dir="dir">
            <file name="not_empty_file" from="data/actual.xml"/>
            <file name="empty_file"/>
        </e:fl-set>
    </e:given>
    <e:example name="Happy-path">
        <e:then print="true">
            <e:fl-check dir="dir">
                <file name="empty_file"/>
                <file name="not_empty_file"><![CDATA[
                    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                    <data>
                        <date>!{formattedAndWithinNow [yyyy-MM-dd'T'HH:mm:ss][1min]}</date>
                        <list>
                            <item>
                                <bool>!{bool}</bool>
                                <num>!{num}</num>
                                <float>!{num}</float>
                                <str>!{str}</str>
                                <regex>!{regex}\d\d\d</regex>
                                <ignore>!{ignore}</ignore>
                            </item>
                        </list>
                    </data>]]>
                </file>
            </e:fl-check>
        </e:then>
    </e:example>
    <e:example name="Surplus file" status="ExpectedToFail">
        <e:then print="true">
            <e:fl-check dir="dir">
                <file name="not_empty_file"/>
            </e:fl-check>
        </e:then>
    </e:example>
    <e:example name="Missing file" status="ExpectedToFail">
        <e:then print="true">
            <e:fl-check dir="dir">
                <file name="empty_file"/>
                <file name="missing_file"/>
                <file name="not_empty_file"/>
            </e:fl-check>
        </e:then>
    </e:example>
    <e:example name="Wrong file content" status="ExpectedToFail">
        <e:then print="true">
            <e:fl-check dir="dir">
                <file name="empty_file"/>
                <file name="not_empty_file">another content was expected</file>
            </e:fl-check>
        </e:then>
    </e:example>
    <e:example name="All previous checks together" status="ExpectedToFail">
        <e:then print="true">
            <e:fl-check dir="dir">
                <file name="missing_file"/>
                <file name="not_empty_file">another content was expected</file>
            </e:fl-check>
        </e:then>
    </e:example>
</div>