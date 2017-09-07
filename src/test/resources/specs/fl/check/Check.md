# Check directory content
## `<e:fl-check dir="dir"/>`

***!!! Only XML comparing are supported !!!***

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
    <e:example name="Happy-path">
        <e:then print="true">
            <e:fl-check dir="dir">
                <file name="empty_file"/>
                <file name="not_empty_file">
                    <data>some file content</data>
                </file>
            </e:fl-check>
        </e:then>
    </e:example>
    <e:example name="Surplus file" status="ExpectedToFail">
        <e:then print="true">
            <e:fl-check dir="dir">
                <file name="not_empty_file">
                    <data>some file content</data>
                </file>
            </e:fl-check>
        </e:then>
    </e:example>
    <e:example name="Missing file" status="ExpectedToFail">
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
    <e:example name="Wrong file content" status="ExpectedToFail">
        <e:then print="true">
            <e:fl-check dir="dir">
                <file name="empty_file"/>
                <file name="not_empty_file">
                    <data>another content was expected</data>
                </file>
            </e:fl-check>
        </e:then>
    </e:example>
    <e:example name="All previous checks together" status="ExpectedToFail">
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