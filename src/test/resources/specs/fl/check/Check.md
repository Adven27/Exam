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
                <file name="not_empty_file" from="data/actual.xml"/>
            </e:fl-check>
        </e:then>
    </e:example>
     <e:example name="Happy-path with parametrized file name">
        <e:then print="true">
            <e:fl-check dir="dir">
                <file name="${#emptyFileNameFromTemplate}"/>
                <file name="not_empty_file" from="data/actual.xml"/>
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
    <e:example name="Details">
        <ol>
            <li><a c:run="concordion" href="details/Details.html">File comparing details</a></li>
        </ol>
    </e:example>
</div>